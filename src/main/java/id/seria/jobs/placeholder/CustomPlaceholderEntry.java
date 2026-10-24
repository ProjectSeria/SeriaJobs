package id.seria.jobs.placeholder;

import id.seria.core.gui.menu.MenuRequirementEvaluator;
import id.seria.jobs.SeriaJobs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class CustomPlaceholderEntry {

    private final String id;
    private final String group; // File or category name (e.g. "playerlevel", "math")
    private final String type;
    private final String function;
    private final String input;
    private final Map<String, String> tracks;

    // Progress bar fields
    private final String valueExpr;
    private final String maxExpr;
    private final int length;
    private final String firstCompleting;
    private final String firstRemaining;
    private final String completing;
    private final String remaining;
    private final String lastCompleting;
    private final String lastRemaining;

    public CustomPlaceholderEntry(@NotNull String id,
                                  @NotNull String group,
                                  @Nullable String type,
                                  @Nullable String function,
                                  @Nullable String input,
                                  @Nullable Map<String, String> tracks) {
        this(id, group, type, function, input, tracks, null);
    }

    public CustomPlaceholderEntry(@NotNull String id,
                                  @NotNull String group,
                                  @Nullable String type,
                                  @Nullable String function,
                                  @Nullable String input,
                                  @Nullable Map<String, String> tracks,
                                  @Nullable org.bukkit.configuration.ConfigurationSection sec) {
        this.id = id;
        this.group = group;
        this.type = type != null ? type : "function";
        this.function = function != null ? function : "short-track";
        this.input = input != null ? input : "";
        this.tracks = tracks != null ? new LinkedHashMap<>(tracks) : Collections.emptyMap();

        if (sec != null) {
            this.valueExpr = sec.getString("value", sec.getString("input", "0"));
            this.maxExpr = sec.getString("max", "100");
            this.length = sec.getInt("length", 20);
            this.firstCompleting = sec.getString("first-completing", sec.getString("completing", "<green>|"));
            this.firstRemaining = sec.getString("first-remaining", sec.getString("remaining", "<gray>|"));
            this.completing = sec.getString("completing", "<green>|");
            this.remaining = sec.getString("remaining", "<gray>|");
            this.lastCompleting = sec.getString("last-completing", sec.getString("completing", "<green>|"));
            this.lastRemaining = sec.getString("last-remaining", sec.getString("remaining", "<gray>|"));
        } else {
            this.valueExpr = this.input;
            this.maxExpr = "100";
            this.length = 20;
            this.firstCompleting = "<green>|";
            this.firstRemaining = "<gray>|";
            this.completing = "<green>|";
            this.remaining = "<gray>|";
            this.lastCompleting = "<green>|";
            this.lastRemaining = "<gray>|";
        }
    }

    public String getId() {
        return id;
    }

    public String getGroup() {
        return group;
    }

    public String getType() {
        return type;
    }

    public String getFunction() {
        return function;
    }

    public String getInput() {
        return input;
    }

    public Map<String, String> getTracks() {
        return Collections.unmodifiableMap(tracks);
    }

    @NotNull
    public String evaluate(@Nullable Player player) {
        return evaluate(player, null);
    }

    @NotNull
    public String evaluate(@Nullable Player player, @Nullable Map<String, String> context) {
        if ("progress".equalsIgnoreCase(function) || "progressbar".equalsIgnoreCase(function)) {
            return evaluateProgressBar(player, context);
        }

        String evaluatedInput = input != null ? input : "";
        evaluatedInput = resolveTextRecursively(player, evaluatedInput, context);
        evaluatedInput = evaluatedInput.trim();

        if ("function".equalsIgnoreCase(type) || "track".equalsIgnoreCase(type)) {
            if ("short-track".equalsIgnoreCase(function) || "track".equalsIgnoreCase(function)) {
                return evaluateTrack(player, evaluatedInput, context);
            } else if ("math".equalsIgnoreCase(function)) {
                return evaluateMath(player, evaluatedInput);
            }
        } else if ("math".equalsIgnoreCase(type)) {
            return evaluateMath(player, evaluatedInput);
        }

        // Fallback: evaluate tracks or return evaluatedInput
        if (!tracks.isEmpty()) {
            return evaluateTrack(player, evaluatedInput, context);
        }

        return resolveOutput(player, evaluatedInput, context);
    }

    private String evaluateProgressBar(@Nullable Player player, @Nullable Map<String, String> context) {
        String valRaw = valueExpr != null && !valueExpr.isBlank() ? valueExpr : (input != null && !input.isBlank() ? input : "0");
        String maxRaw = maxExpr != null && !maxExpr.isBlank() ? maxExpr : "100";

        String valStr = resolveTextRecursively(player, valRaw, context);
        String maxStr = resolveTextRecursively(player, maxRaw, context);

        double val = 0;
        double max = 100;
        try { val = Double.parseDouble(valStr.replace(",", "").trim()); } catch (NumberFormatException ignored) {}
        try { max = Double.parseDouble(maxStr.replace(",", "").trim()); } catch (NumberFormatException ignored) {}
        if (max <= 0) max = 1;

        int barLength = length > 0 ? length : 20;
        double percent = Math.min(1.0, Math.max(0.0, val / max));
        int completedBars = (int) Math.round(percent * barLength);

        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < barLength; i++) {
            boolean isCompleted = i < completedBars;
            if (i == 0) {
                bar.append(isCompleted ? firstCompleting : firstRemaining);
            } else if (i == barLength - 1) {
                bar.append(isCompleted ? lastCompleting : lastRemaining);
            } else {
                bar.append(isCompleted ? completing : remaining);
            }
        }

        return bar.toString();
    }

    private String evaluateTrack(@Nullable Player player, @NotNull String rawInput, @Nullable Map<String, String> context) {
        Double numericInput = null;
        try {
            numericInput = Double.parseDouble(rawInput.replace(",", ""));
        } catch (NumberFormatException ignored) {}

        String defaultResult = null;

        for (Map.Entry<String, String> entry : tracks.entrySet()) {
            String cond = entry.getKey().trim();
            String output = entry.getValue();

            if (cond.equalsIgnoreCase("default") || cond.equalsIgnoreCase("else")) {
                defaultResult = output;
                continue;
            }

            if (cond.startsWith("==") || cond.startsWith("=")) {
                String valStr = cond.startsWith("==") ? cond.substring(2).trim() : cond.substring(1).trim();
                if (matchesEquality(numericInput, rawInput, valStr)) {
                    return resolveOutput(player, output, context);
                }
            } else if (cond.startsWith("!=")) {
                String valStr = cond.substring(2).trim();
                if (!matchesEquality(numericInput, rawInput, valStr)) {
                    return resolveOutput(player, output, context);
                }
            } else if (cond.startsWith("<=")) {
                if (numericInput != null) {
                    try {
                        double val = Double.parseDouble(cond.substring(2).trim());
                        if (numericInput <= val) return resolveOutput(player, output, context);
                    } catch (NumberFormatException ignored) {}
                }
            } else if (cond.startsWith("<")) {
                if (numericInput != null) {
                    try {
                        double val = Double.parseDouble(cond.substring(1).trim());
                        if (numericInput < val) return resolveOutput(player, output, context);
                    } catch (NumberFormatException ignored) {}
                }
            } else if (cond.startsWith(">=")) {
                if (numericInput != null) {
                    try {
                        double val = Double.parseDouble(cond.substring(2).trim());
                        if (numericInput >= val) return resolveOutput(player, output, context);
                    } catch (NumberFormatException ignored) {}
                }
            } else if (cond.startsWith(">")) {
                if (numericInput != null) {
                    try {
                        double val = Double.parseDouble(cond.substring(1).trim());
                        if (numericInput > val) return resolveOutput(player, output, context);
                    } catch (NumberFormatException ignored) {}
                }
            } else if (cond.contains("..")) {
                String[] range = cond.split("\\.\\.");
                if (range.length == 2 && numericInput != null) {
                    try {
                        double min = Double.parseDouble(range[0].trim());
                        double max = Double.parseDouble(range[1].trim());
                        if (numericInput >= min && numericInput <= max) {
                            return resolveOutput(player, output, context);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            } else {
                // Direct value match (e.g. "0": "...")
                if (matchesEquality(numericInput, rawInput, cond)) {
                    return resolveOutput(player, output, context);
                }
            }
        }

        return defaultResult != null ? resolveOutput(player, defaultResult, context) : "";
    }

    private boolean matchesEquality(@Nullable Double numericInput, @NotNull String rawInput, @NotNull String target) {
        if (numericInput != null) {
            try {
                double targetNum = Double.parseDouble(target.replace(",", ""));
                return Math.abs(numericInput - targetNum) < 0.00001;
            } catch (NumberFormatException ignored) {}
        }
        return rawInput.equalsIgnoreCase(target);
    }

    private String evaluateMath(@Nullable Player player, @NotNull String formula) {
        String cleanExpr = formula.replace("$e(", "").replace("%e(", "").replace(")", "").replace(",", "").trim();
        try {
            // Check if SeriaCore MenuRequirementEvaluator is available
            if (Bukkit.getPluginManager().isPluginEnabled("SeriaCore")) {
                try {
                    String formatted = "$e(" + cleanExpr + ")";
                    String res = MenuRequirementEvaluator.evaluateMathExpressions(formatted);
                    if (res != null && !res.equals(formatted)) {
                        return res;
                    }
                } catch (Throwable ignored) {}
            }

            // Fallback simple arithmetic evaluation: +, -, *, /
            if (cleanExpr.contains("+")) {
                String[] p = cleanExpr.split("\\+", 2);
                return formatNumber(Double.parseDouble(p[0].trim()) + Double.parseDouble(p[1].trim()));
            } else if (cleanExpr.contains("-") && cleanExpr.lastIndexOf('-') > 0) {
                int idx = cleanExpr.lastIndexOf('-');
                return formatNumber(Double.parseDouble(cleanExpr.substring(0, idx).trim()) - Double.parseDouble(cleanExpr.substring(idx + 1).trim()));
            } else if (cleanExpr.contains("*")) {
                String[] p = cleanExpr.split("\\*", 2);
                return formatNumber(Double.parseDouble(p[0].trim()) * Double.parseDouble(p[1].trim()));
            } else if (cleanExpr.contains("/")) {
                String[] p = cleanExpr.split("/", 2);
                double denom = Double.parseDouble(p[1].trim());
                if (denom != 0) return formatNumber(Double.parseDouble(p[0].trim()) / denom);
            }
        } catch (Throwable ignored) {}
        return cleanExpr;
    }

    private String formatNumber(double val) {
        if (val == (long) val) {
            return String.valueOf((long) val);
        }
        return String.format(java.util.Locale.US, "%.2f", val);
    }

    private String resolveTextRecursively(@Nullable Player player, @Nullable String text, @Nullable Map<String, String> context) {
        if (text == null || text.isEmpty()) return "";
        if (context != null && !context.isEmpty()) {
            for (Map.Entry<String, String> entry : context.entrySet()) {
                text = text.replace(entry.getKey(), entry.getValue());
            }
        }
        int depth = 0;
        while (text != null && text.contains("%") && depth < 6) {
            String prev = text;
            if (player != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                try {
                    text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
                } catch (Throwable ignored) {}
            }
            if (context != null && !context.isEmpty()) {
                for (Map.Entry<String, String> entry : context.entrySet()) {
                    text = text.replace(entry.getKey(), entry.getValue());
                }
            }
            if (text != null && text.equals(prev)) {
                var mgr = SeriaJobs.getInstance().getCustomPlaceholderManager();
                if (mgr != null && (text.contains("%custom_") || text.contains("%seriajobs_custom_"))) {
                    text = resolveCustomPlaceholdersDirectly(player, text, mgr, context);
                }
                if (text != null && text.contains("%jobsorder_")) {
                    text = resolveJobsOrderPlaceholdersDirectly(player, text);
                }
            }
            if (text == null || text.equals(prev)) break;
            depth++;
        }
        if (player != null && text != null) {
            text = text.replace("%player_name%", player.getName())
                    .replace("{player_name}", player.getName())
                    .replace("%player%", player.getName())
                    .replace("{player}", player.getName());
        }
        return text != null ? text : "";
    }

    private String resolveOutput(@Nullable Player player, @Nullable String text, @Nullable Map<String, String> context) {
        return resolveTextRecursively(player, text, context);
    }

    private String resolveJobsOrderPlaceholdersDirectly(Player player, String text) {
        if (text == null || text.isEmpty()) return text;
        id.seria.jobs.hook.JobsPlaceholderExpansion expansion = SeriaJobs.getInstance().getHookManager() != null
                ? SeriaJobs.getInstance().getHookManager().getJobsOrderExpansion()
                : null;
        if (expansion == null) expansion = new id.seria.jobs.hook.JobsPlaceholderExpansion(SeriaJobs.getInstance());
        int start;
        while ((start = text.indexOf("%jobsorder_")) != -1) {
            int end = text.indexOf('%', start + 11);
            if (end == -1) break;
            String token = text.substring(start, end + 1);
            String param = text.substring(start + 11, end);
            String resolved = expansion.onRequest(player, param);
            if (resolved != null) {
                text = text.replace(token, resolved);
            } else {
                break;
            }
        }
        return text;
    }

    private String resolveCustomPlaceholdersDirectly(Player player, String text, CustomPlaceholderManager mgr, @Nullable Map<String, String> context) {
        int start;
        while ((start = text.indexOf("%custom_")) != -1) {
            int end = text.indexOf('%', start + 8);
            if (end == -1) break;
            String token = text.substring(start, end + 1);
            String param = text.substring(start + 8, end);
            String resolved = mgr.evaluate(player, param, context);
            if (resolved != null) {
                text = text.replace(token, resolved);
            } else {
                break;
            }
        }
        while ((start = text.indexOf("%seriajobs_custom_")) != -1) {
            int end = text.indexOf('%', start + 18);
            if (end == -1) break;
            String token = text.substring(start, end + 1);
            String param = text.substring(start + 18, end);
            String resolved = mgr.evaluate(player, param, context);
            if (resolved != null) {
                text = text.replace(token, resolved);
            } else {
                break;
            }
        }
        return text;
    }
}
