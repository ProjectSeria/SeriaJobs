package id.seria.jobs.model;

public enum LeaderboardPeriod {
    WEEKLY,
    MONTHLY,
    ALL_TIME;

    public static LeaderboardPeriod fromString(String str) {
        if (str == null) return ALL_TIME;
        String s = str.trim().toUpperCase();
        if (s.startsWith("WEEK")) return WEEKLY;
        if (s.startsWith("MONTH")) return MONTHLY;
        return ALL_TIME;
    }
}
