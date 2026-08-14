package com.discepolo.clansystem.clan;

import java.util.UUID;

public class ClanMember {

    private final UUID uuid;
    private String lastKnownName;
    private ClanRole role;

    public ClanMember(UUID uuid, String lastKnownName, ClanRole role) {
        this.uuid = uuid;
        this.lastKnownName = lastKnownName;
        this.role = role;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getLastKnownName() {
        return lastKnownName;
    }

    public void setLastKnownName(String lastKnownName) {
        this.lastKnownName = lastKnownName;
    }

    public ClanRole getRole() {
        return role;
    }

    public void setRole(ClanRole role) {
        this.role = role;
    }
}