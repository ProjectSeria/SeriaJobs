package id.seria.jobs.service;

import id.seria.core.utils.MathExpression;
import id.seria.jobs.model.Job;
import id.seria.jobs.service.ProgressionService.Kind;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class ProgressionServiceTest {

    private static final String GLOBAL = """
            progression:
              income-progression-equation: "baseincome * (1 + (joblevel - 1) * 0.01)"
              experience-progression-equation: "baseexperience"
              points-progression-equation: "basepoints * (1 - (numjobs - 1) * 0.05)"
              income-rounding:
                mode: "CEIL"
                step: 250
              points-rounding:
                mode: "ROUND"
                step: 1
              experience-rounding:
                mode: "ROUND"
                step: 1
            """;

    private final List<LogRecord> logs = new ArrayList<>();
    private Logger logger;

    @BeforeEach
    void setUp() {
        logs.clear();
        logger = Logger.getLogger("ProgressionServiceTest-" + System.nanoTime());
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override public void publish(LogRecord r) { logs.add(r); }
            @Override public void flush() {}
            @Override public void close() {}
        });
    }

    private static Job job(String id, String income, String points, String exp) {
        return new Job(id, id, Material.STONE, List.of(), 100, 100, 1.25, null, income, points, exp,
                3, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    private ProgressionService service(String yaml, Job... jobs) throws Exception {
        ProgressionService s = new ProgressionService(null, logger, MathExpression::evaluate);
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.loadFromString(yaml);
        s.load(cfg.getConfigurationSection("progression"), List.of(jobs));
        return s;
    }

    @Test
    void incomeWithTierAndCeil250() throws Exception {
        ProgressionService s = service(GLOBAL, job("farmer", null, null, null));
        double[][] cases = {
                // level, tier, expected
                {1, 1.00, 1000}, {4, 1.05, 1250}, {10, 1.10, 1250}, {14, 1.20, 1500},
                {22, 1.30, 1750}, {50, 1.30, 2000}, {100, 1.30, 2750}
        };
        for (double[] c : cases) {
            assertEquals(c[2], s.compute(Kind.INCOME, "farmer", 1000, (int) c[0], 1, c[1]), 0.0001, "level " + c[0]);
        }
        assertTrue(logs.isEmpty());
    }

    @Test
    void pointsRoundedAndPenalisedByNumJobs() throws Exception {
        ProgressionService s = service(GLOBAL, job("farmer", null, null, null));
        assertEquals(3, s.compute(Kind.POINTS, "farmer", 3, 10, 1, 1.0), 0.0001);
        assertEquals(3, s.compute(Kind.POINTS, "farmer", 3, 10, 2, 1.0), 0.0001); // 2,85 -> 3
    }

    @Test
    void experienceFlatAcrossLevelsAndIgnoresTier() throws Exception {
        ProgressionService s = service(GLOBAL, job("farmer", null, null, null));
        for (int level : new int[]{1, 25, 99}) {
            assertEquals(160, s.compute(Kind.EXPERIENCE, "farmer", 160, level, 1, 1.30), 0.0001);
        }
    }

    @Test
    void jobFileFormulaOverridesGlobal() throws Exception {
        ProgressionService s = service(GLOBAL, job("miner", "baseincome * 2", null, null));
        assertEquals(2000, s.compute(Kind.INCOME, "miner", 1000, 50, 1, 1.0), 0.0001);
    }

    @Test
    void brokenFormulaFallsBackToBaseAndLogsError() throws Exception {
        ProgressionService s = service(GLOBAL, job("miner", "baseincome * (", null, null),
                job("hunter", "baseincome * unknownvar", null, null));
        assertEquals(1000, s.compute(Kind.INCOME, "miner", 1000, 50, 1, 1.0), 0.0001);
        assertEquals(1000, s.compute(Kind.INCOME, "hunter", 1000, 50, 1, 1.0), 0.0001);
        assertEquals(2, logs.stream().filter(r -> r.getLevel().intValue() >= java.util.logging.Level.SEVERE.intValue()).count());
    }

    @Test
    void negativeResultForcedToZeroAndLoggedOnce() throws Exception {
        ProgressionService s = service(GLOBAL, job("miner", "baseincome - 5000", null, null));
        assertEquals(0, s.compute(Kind.INCOME, "miner", 1000, 1, 1, 1.0), 0.0001);
        assertEquals(0, s.compute(Kind.INCOME, "miner", 1000, 2, 1, 1.0), 0.0001);
        assertEquals(1, logs.stream().filter(r -> r.getLevel() == java.util.logging.Level.WARNING).count());
    }

    @Test
    void missingConfigUsesBase() throws Exception {
        ProgressionService s = service("other: 1\n", job("farmer", null, null, null));
        assertEquals(1000, s.compute(Kind.INCOME, "farmer", 1000, 50, 1, 1.0), 0.0001);
        assertEquals(1500, s.compute(Kind.INCOME, "farmer", 1000, 50, 1, 1.5), 0.0001);
    }

    @Test
    void formatNumberUsesDotThousands() {
        assertEquals("1.250", ProgressionService.formatNumber(1250));
        assertEquals("99.000", ProgressionService.formatNumber(99000));
        assertEquals("160", ProgressionService.formatNumber(160));
    }
}
