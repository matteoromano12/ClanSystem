package com.discepolo.clansystem.clan;

public enum ClanRole {
    MEMBER(0, "Membro"),
    OFFICER(1, "Officer"),
    LEADER(2, "Leader");

    private final int weight;
    private final String displayName;

    ClanRole(int weight, String displayName) {
        this.weight = weight;
        this.displayName = displayName;
    }

    public int getWeight() {
        return weight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAtLeast(ClanRole other) {
        return this.weight >= other.weight;
    }

    public ClanRole next() {
        return switch (this) {
            case MEMBER -> OFFICER;
            case OFFICER, LEADER -> LEADER;
        };
    }

    public ClanRole previous() {
        return switch (this) {
            case LEADER -> OFFICER;
            case OFFICER, MEMBER -> MEMBER;
        };
    }
}