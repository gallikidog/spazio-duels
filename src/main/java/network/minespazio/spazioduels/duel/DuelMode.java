/*
 * Decompiled with CFR 0.152.
 */
package network.minespazio.spazioduels.duel;

public enum DuelMode {
    SOLO_1V1("1v1", 1, 1, false),
    TEAM_2V2("2v2", 2, 2, false),
    TEAM_3V3("3v3", 3, 3, false),
    TEAM_4V4("4v4", 4, 4, false),
    HANDICAP_2V1("2v1 Handicap", 2, 1, true),
    HANDICAP_3V2("3v2 Handicap", 3, 2, true),
    HANDICAP_4V3("4v3 Handicap", 4, 3, true);

    private final String displayName;
    private final int team1Size;
    private final int team2Size;
    private final boolean handicap;

    private DuelMode(String displayName, int team1Size, int team2Size, boolean handicap) {
        this.displayName = displayName;
        this.team1Size = team1Size;
        this.team2Size = team2Size;
        this.handicap = handicap;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getTeam1Size() {
        return this.team1Size;
    }

    public int getTeam2Size() {
        return this.team2Size;
    }

    public boolean isHandicap() {
        return this.handicap;
    }

    public static DuelMode fromString(String str) {
        if (str == null) {
            return SOLO_1V1;
        }
        for (DuelMode mode : DuelMode.values()) {
            if (!mode.name().equalsIgnoreCase(str) && !mode.getDisplayName().equalsIgnoreCase(str) && !mode.name().replace("_", "").equalsIgnoreCase(str)) continue;
            return mode;
        }
        return SOLO_1V1;
    }
}

