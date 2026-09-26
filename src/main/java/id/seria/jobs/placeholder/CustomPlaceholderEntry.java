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

    public CustomPlaceholderEntry(@NotNull String id,
                                  @NotNull String group,
                                  @Nullable String type,
                                  @Nullable String function,
                                  @Nullable String input,
                                  @Nullable Map<String, String> tracks) {
        this.id = id;
        this.group = group;
        this.type = type != null ? type : "function";
        this.function = function != null ? function : "short-track";
        this.input = input != null ? input : "";
        this.tracks = tracks != null ? new LinkedHashMap<>(tracks) : Collections.emptyMap();
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
        String evaluatedInput = input != null ? input : "";
        int depth = 0;
        while (evaluatedInput.contains("%") && depth < 6) {
            String prev = evaluatedInput;
            if (player != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                try {
                    evaluatedInput = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, evaluatedInput);
                } catch (Throwable ignored) {}
            }
            if (evaluatedInput.equals(prev)) {
                var mgr = SeriaJobs.getInstance().getCustomPlaceholderManager();
                if (mgr != null && (evaluatedInput.contains("%custom_") || evaluatedInput.contains("%seriajobs_custom_"))) {
                    evaluatedInput = resolveCustomPlaceholdersDirectly(player, evaluatedInput, mgr);
                }
            }
            if (evaluatedInput.equals(prev)) break;
            depth++;
        }
        evaluatedInput = evaluatedInput.trim();

        if ("function".equalsIgnoreCase(type) || "track".equalsIgnoreCase(type)) {
            if ("short-track".equalsIgnoreCase(function) || "track".equalsIgnoreCase(function)) {
                return evaluateTrack(player, evaluatedInput);
            } else if ("math".equalsIgnoreCase(function)) {
                return evaluateMath(player, evaluatedInput);
            }
        } else if ("math".equalsIgnoreCase(type)) {
            return evaluateMath(player, evaluatedInput);
        }

        // Fallback: evaluate tracks or return evaluatedInput
        if (!tracks.isEmpty()) {
            return evaluateTrack(player, evaluatedInput);
        }

        return resolveOutput(player, evaluatedInput);
    }

    private String evaluateTrack(@Nullable Player player, @NotNull String rawInput) {
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
                    return resolveOutput(player, output);
                }
            } else if (cond.startsWith("!=")) {
                String valStr = cond.substring(2).trim();
                if (!matchesEquality(numericInput, rawInput, valStr)) {
                    return resolveOutput(player, output);
                }
            } else if (cond.startsWith("<=")) {
                if (numericInput != null) {
                    try {
                        double val = Double.parseDouble(cond.substring(2).trim());
                        if (numericInput <= val) return resolveOutput(player, output);
                    } catch (NumberFormatException ignored) {}
                }
            } else if (cond.startsWith("<")) {
                if (numericInput != null) {
                    try {
                        double val = Double.parseDouble(cond.substring(1).trim());
                        if (numericInput < val) return resolveOutput(player, output);
                    } catch (NumberFormatException ignored) {}
                }
            } else if (cond.startsWith(">=")) {
                if (numericInput != null) {
                    try {
                        double val = Double.parseDouble(cond.substring(2).trim());
                        if (numericInput >= val) return resolveOutput(player, output);
                    } catch (NumberFormatException ignored) {}
                }
            } else if (cond.startsWith(">")) {
                if (numericInput != null) {
                    try {
                        double val = Double.parseDouble(cond.substring(1).trim());
                        if (numericInput > val) return resolveOutput(player, output);
                    } catch (NumberFormatException ignored) {}
                }
            } else if (cond.contains("..")) {
                String[] range = cond.split("\\.\\.");
                if (range.length == 2 && numericInput != null) {
                    try {
                        double min = Double.parseDouble(range[0].trim());
                        double max = Double.parseDouble(range[1].trim());
                        if (numericInput >= min && numericInput <= max) {
                            return resolveOutput(player, output);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            } else {
                // Direct value match (e.g. "0": "...")
                if (matchesEquality(numericInput, rawInput, cond)) {
                    return resolveOutput(player, output);
                }
            }
        }

        return defaultResult != null ? resolveOutput(player, defaultResult) : "";
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
        String expr = formula.trim();
        try {
            // Check if SeriaCore MenuRequirementEvaluator is available
            if (Bukkit.getPluginManager().isPluginEnabled("SeriaCore")) {
                try {
                    String formatted = expr.startsWith("$e(") ? expr : "$e(" + expr + ")";
                    String res = MenuRequirementEvaluator.evaluateMathExpressions(formatted);
                    if (res != null && !res.equals(formatted)) {
                        return res;
                    }
                } catch (Throwable ignored) {}
            }

            // Fallback simple arithmetic evaluation: +, -, *, /
            if (expr.contains("+")) {
                String[] p = expr.split("\\+", 2);
                return formatNumber(Double.parseDouble(p[0].trim()) + Double.parseDouble(p[1].trim()));
            } else if (expr.contains("-") && expr.lastIndexOf('-') > 0) {
                int idx = expr.lastIndexOf('-');
                return formatNumber(Double.parseDouble(expr.substring(0, idx).trim()) - Double.parseDouble(expr.substring(idx + 1).trim()));
            } else if (expr.contains("*")) {
                String[] p = expr.split("\\*", 2);
                return formatNumber(Double.parseDouble(p[0].trim()) * Double.parseDouble(p[1].trim()));
            } else if (expr.contains("/")) {
                String[] p = expr.split("/", 2);
                double denom = Double.parseDouble(p[1].trim());
                if (denom != 0) return formatNumber(Double.parseDouble(p[0].trim()) / denom);
            }
        } catch (Throwable ignored) {}
        return expr;
    }

    private String formatNumber(double val) {
        if (val == (long) val) {
            return String.valueOf((long) val);
        }
        return String.format(java.util.Locale.US, "%.2f", val);
    }

    private String resolveOutput(@Nullable Player player, @Nullable String text) {
        if (text == null) return "";
        int depth = 0;
        while (text != null && text.contains("%") && depth < 6) {
            String prev = text;
            if (player != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                try {
                    text = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, text);
                } catch (Throwable ignored) {}
            }
            if (text != null && text.equals(prev)) {
                var mgr = SeriaJobs.getInstance().getCustomPlaceholderManager();
                if (mgr != null && (text.contains("%custom_") || text.contains("%seriajobs_custom_"))) {
                    text = resolveCustomPlaceholdersDirectly(player, text, mgr);
                }
            }
            if (text == null || text.equals(prev)) break;
            depth++;
        }
        return text != null ? text : "";
    }

    private String resolveCustomPlaceholdersDirectly(Player player, String text, CustomPlaceholderManager mgr) {
        int start;
        while ((start = text.indexOf("%custom_")) != -1) {
            int end = text.indexOf('%', start + 8);
            if (end == -1) break;
            String token = text.substring(start, end + 1);
            String param = text.substring(start + 8, end);
            String resolved = mgr.evaluate(player, param);
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
            String resolved = mgr.evaluate(player, param);
            if (resolved != null) {
                text = text.replace(token, resolved);
            } else {
                break;
            }
        }
        return text;
    }
}
