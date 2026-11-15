package id.seria.jobs.service;

import id.seria.core.utils.MathExpression;
import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import id.seria.jobs.model.JobOrder;
import id.seria.jobs.model.JobProfile;
import id.seria.jobs.model.WorkOrder;
import id.seria.jobs.model.reward.JobXPReward;
import id.seria.jobs.model.reward.Reward;
import id.seria.jobs.model.reward.VaultReward;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import java.util.logging.Logger;

/**
 * Rumus progresi reward order (Gins / EXP / reputasi) yang dihitung saat klaim.
 *
 * <p>Prioritas rumus: key di file job ({@code jobs/<job>.yml}) > key global {@code progression} di
 * {@code config.yml} > nilai base apa adanya. Variabel rumus: {@code joblevel}, {@code baseincome},
 * {@code baseexperience}, {@code basepoints}, {@code numjobs}.
 *
 * <p>Urutan hitung: base -> rumus -> (hanya Gins) x multiplier tier reputasi -> pembulatan -> minimal 0.
 * Evaluasi memakai {@link MathExpression} milik SeriaCore. Rumus divalidasi satu kali saat load; rumus
 * yang rusak dicatat di log lalu jatuh ke nilai base, plugin tidak gagal load.
 */
public class ProgressionService {

    public enum Kind {
        INCOME("income", "baseincome"),
        EXPERIENCE("experience", "baseexperience"),
        POINTS("points", "basepoints");

        final String key;
        final String baseVariable;

        Kind(String key, String baseVariable) {
            this.key = key;
            this.baseVariable = baseVariable;
        }
    }

    public enum RoundMode { CEIL, FLOOR, ROUND, NONE }

    public record Rounding(@NotNull RoundMode mode, double step) {
        public double apply(double value) {
            if (mode == RoundMode.NONE || step <= 0) return value;
            double q = value / step;
            // toleransi kecil supaya 1000.0000000000002 / 250 tidak naik ke kelipatan berikutnya
            double rounded = switch (mode) {
                case CEIL -> Math.ceil(q - 1e-9);
                case FLOOR -> Math.floor(q + 1e-9);
                case ROUND -> Math.floor(q + 0.5);
                default -> q;
            };
            return rounded * step;
        }
    }

    /** Nilai yang ditampilkan di GUI untuk satu order. */
    public record Display(@NotNull String gins, @NotNull String xp, @NotNull String reputation) {}

    private static final Rounding DEFAULT_INCOME = new Rounding(RoundMode.NONE, 1);
    private static final DecimalFormatSymbols ID_SYMBOLS = new DecimalFormatSymbols(Locale.ROOT);

    static {
        ID_SYMBOLS.setGroupingSeparator('.');
        ID_SYMBOLS.setDecimalSeparator(',');
    }

    private final SeriaJobs plugin;
    private final Logger logger;
    private final ToDoubleFunction<String> evaluator;

    /** Rumus aktif per job id (lowercase) dan jenisnya; tidak ada entry = pakai base. */
    private final Map<String, Map<Kind, String>> formulas = new HashMap<>();
    private final Map<Kind, Rounding> rounding = new EnumMap<>(Kind.class);
    private final Set<String> warned = new HashSet<>();

    public ProgressionService(@NotNull SeriaJobs plugin) {
        this(plugin, plugin.getLogger(), MathExpression::evaluate);
    }

    /** Konstruktor untuk unit test (tanpa server). */
    public ProgressionService(@Nullable SeriaJobs plugin, @NotNull Logger logger, @NotNull ToDoubleFunction<String> evaluator) {
        this.plugin = plugin;
        this.logger = logger;
        this.evaluator = evaluator;
    }

    // ------------------------------------------------------------------ load

    /**
     * Baca ulang konfigurasi. Dipanggil saat enable dan {@code /jobs reload}, setelah job registry dimuat.
     *
     * @param global section {@code progression} di config.yml (boleh null)
     */
    public void load(@Nullable ConfigurationSection global, @NotNull Collection<Job> jobs) {
        formulas.clear();
        rounding.clear();
        warned.clear();

        rounding.put(Kind.INCOME, readRounding(global, Kind.INCOME, new Rounding(RoundMode.NONE, 1)));
        rounding.put(Kind.EXPERIENCE, readRounding(global, Kind.EXPERIENCE, new Rounding(RoundMode.ROUND, 1)));
        rounding.put(Kind.POINTS, readRounding(global, Kind.POINTS, new Rounding(RoundMode.ROUND, 1)));

        for (Job job : jobs) {
            Map<Kind, String> perJob = new EnumMap<>(Kind.class);
            for (Kind kind : Kind.values()) {
                String fileFormula = switch (kind) {
                    case INCOME -> job.getIncomeProgressionEquation();
                    case EXPERIENCE -> job.getExperienceProgressionEquation();
                    case POINTS -> job.getPointsProgressionEquation();
                };
                String source;
                String formula;
                if (fileFormula != null && !fileFormula.isBlank()) {
                    formula = fileFormula;
                    source = "jobs/" + job.getId() + ".yml -> " + kind.key + "-progression-equation";
                } else {
                    formula = global != null ? global.getString(kind.key + "-progression-equation") : null;
                    source = "config.yml -> progression." + kind.key + "-progression-equation";
                }
                if (formula == null || formula.isBlank()) continue;

                if (validate(formula, kind, source)) {
                    perJob.put(kind, formula);
                }
            }
            formulas.put(job.getId().toLowerCase(Locale.ROOT), perJob);
        }
    }

    private Rounding readRounding(@Nullable ConfigurationSection global, Kind kind, Rounding fallback) {
        if (global == null) return fallback;
        ConfigurationSection sec = global.getConfigurationSection(kind.key + "-rounding");
        if (sec == null) return fallback;
        RoundMode mode;
        try {
            mode = RoundMode.valueOf(sec.getString("mode", fallback.mode().name()).trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            logger.warning("[Progression] config.yml -> progression." + kind.key + "-rounding.mode tidak valid, pakai "
                    + fallback.mode());
            mode = fallback.mode();
        }
        double step = sec.getDouble("step", fallback.step());
        if (step <= 0) step = 1;
        return new Rounding(mode, step);
    }

    /** Uji rumus sekali dengan nilai contoh; false (dan log error) bila syntax salah atau variabel tidak dikenal. */
    private boolean validate(String formula, Kind kind, String source) {
        try {
            double v = evaluator.applyAsDouble(substitute(formula, kind, 1000, 1, 1));
            if (Double.isNaN(v) || Double.isInfinite(v)) {
                throw new IllegalArgumentException("hasil uji NaN/Infinity");
            }
            return true;
        } catch (RuntimeException e) {
            logger.severe("[Progression] Rumus tidak valid di " + source + ": \"" + formula + "\" ("
                    + e.getMessage() + "). Memakai nilai base.");
            return false;
        }
    }

    // --------------------------------------------------------------- compute

    /**
     * Hitung nilai progresi satu jenis reward.
     *
     * @param tierMultiplier hanya dipakai untuk {@link Kind#INCOME}; jenis lain mengabaikannya
     */
    public double compute(@NotNull Kind kind, @Nullable String jobId, double base, int jobLevel, int numJobs, double tierMultiplier) {
        double value = base;
        String formula = jobId == null ? null : formulas.getOrDefault(jobId.toLowerCase(Locale.ROOT), Map.of()).get(kind);
        if (formula != null) {
            try {
                value = evaluator.applyAsDouble(substitute(formula, kind, base, Math.max(1, jobLevel), Math.max(1, numJobs)));
            } catch (RuntimeException e) {
                warnOnce(kind, jobId, "gagal evaluasi: " + e.getMessage());
                value = base;
            }
        }
        if (kind == Kind.INCOME && tierMultiplier > 0.0) {
            value *= tierMultiplier;
        }
        if (Double.isNaN(value) || Double.isInfinite(value) || value < 0) {
            warnOnce(kind, jobId, "hasil " + value + " dipaksa 0");
            return 0;
        }
        Rounding r = rounding.getOrDefault(kind, DEFAULT_INCOME);
        return Math.max(0, r.apply(value));
    }

    private void warnOnce(Kind kind, @Nullable String jobId, String message) {
        if (warned.add(kind.key + ":" + jobId + ":" + message)) {
            logger.warning("[Progression] " + kind.key + " job '" + jobId + "': " + message);
        }
    }

    private static String substitute(String formula, Kind kind, double base, int jobLevel, int numJobs) {
        return formula
                .replace("joblevel", "(" + jobLevel + ")")
                .replace("numjobs", "(" + numJobs + ")")
                .replace(kind.baseVariable, "(" + BigDecimal.valueOf(base).toPlainString() + ")");
    }

    // ------------------------------------------------------- player helpers

    public int jobLevelOf(@NotNull Player player, @NotNull Job job) {
        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
        return profile != null ? profile.getJobLevel(job.getId()) : 1;
    }

    public int numJobsOf(@NotNull Player player) {
        JobProfile profile = plugin.getPlayerDataManager().getProfile(player.getUniqueId());
        return profile != null ? Math.max(1, profile.getActiveJobs().size()) : 1;
    }

    private double tierMultiplier(@NotNull Player player) {
        return plugin.getReputationBridgeService() != null
                ? plugin.getReputationBridgeService().getRewardMultiplier(player)
                : 1.0;
    }

    /** Gins yang dibayar untuk base tertentu, pada level dan tier pemain saat ini. */
    public double income(@NotNull Player player, @NotNull Job job, double baseIncome) {
        return compute(Kind.INCOME, job.getId(), baseIncome, jobLevelOf(player, job), numJobsOf(player), tierMultiplier(player));
    }

    public int experience(@NotNull Player player, @NotNull Job job, double baseExperience) {
        return (int) Math.round(compute(Kind.EXPERIENCE, job.getId(), baseExperience, jobLevelOf(player, job), numJobsOf(player), 1.0));
    }

    public int points(@NotNull Player player, @NotNull Job job, double basePoints) {
        return (int) Math.round(compute(Kind.POINTS, job.getId(), basePoints, jobLevelOf(player, job), numJobsOf(player), 1.0));
    }

    // ---------------------------------------------------------- order claim

    /**
     * Bayar semua reward order. VAULT dan JOB_XP memakai rumus progresi; reward lain
     * tetap lewat {@link Reward#apply(Player, Job, double)} dengan multiplier tier seperti sebelumnya.
     */
    public void applyRewards(@NotNull Player player, @NotNull Job job, @NotNull JobOrder order, double rewardMult) {
        for (Reward reward : order.getRewards()) {
            if (reward instanceof VaultReward vault) {
                double gins = income(player, job, vault.getAmount());
                if (gins > 0) plugin.getEconomyService().deposit(player, gins);
            } else if (reward instanceof JobXPReward xpReward) {
                int xp = experience(player, job, xpReward.getXp());
                if (xp > 0) plugin.getPlayerDataManager().addJobXp(player, job.getId(), xp);
            } else {
                reward.apply(player, job, rewardMult);
            }
        }
    }

    // -------------------------------------------------------------- display

    /** Baris reward untuk GUI (tanpa custom display-rewards); angka sama dengan yang dibayar. */
    @NotNull
    public String formatReward(@NotNull Player player, @NotNull Job job, @NotNull Reward reward, double rewardMult) {
        if (reward instanceof VaultReward vault) {
            return new VaultReward(income(player, job, vault.getAmount()), vault.getCustomDisplay()).getFormattedDisplay(1.0);
        }
        if (reward instanceof JobXPReward xpReward) {
            return new JobXPReward(experience(player, job, xpReward.getXp()), xpReward.getCustomDisplay()).getFormattedDisplay(1.0);
        }
        return reward.getFormattedDisplay(rewardMult);
    }

    /**
     * Nilai {gins}, {xp}, {reputation} untuk pemain yang melihat kartu order.
     *
     * @param wo work order yang sudah di-generate (boleh null); reputasinya dipakai bila sudah diacak
     */
    @NotNull
    public Display display(@NotNull Player player, @NotNull Job job, @NotNull JobOrder order, @Nullable WorkOrder wo) {
        double baseGins = 0;
        double baseXp = 0;
        for (Reward r : order.getRewards()) {
            if (r instanceof VaultReward v) baseGins += v.getAmount();
            else if (r instanceof JobXPReward x) baseXp += x.getXp();
        }
        String gins = formatNumber(income(player, job, baseGins));
        String xp = formatNumber(experience(player, job, baseXp));

        String reputation;
        if (wo != null && wo.getGeneratedReputationAmount() > 0) {
            reputation = String.valueOf(points(player, job, wo.getGeneratedReputationAmount()));
        } else {
            int min = points(player, job, order.getMinReputationAmount());
            int max = points(player, job, order.getMaxReputationAmount());
            reputation = (order.getMinReputationAmount() > 0 && max > min) ? (min + "-" + max) : String.valueOf(min);
        }
        return new Display(gins, xp, reputation);
    }

    /** Titik ribuan, tanpa desimal: 1250 -> "1.250". */
    @NotNull
    public static String formatNumber(double value) {
        return new DecimalFormat("#,##0", ID_SYMBOLS).format(Math.round(value));
    }
}
