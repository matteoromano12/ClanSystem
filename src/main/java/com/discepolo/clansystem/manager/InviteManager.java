package com.discepolo.clansystem.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InviteManager {

    private final long inviteTimeoutMs;

    private final Map<UUID, Map<String, Long>> invites = new HashMap<>();

    public InviteManager(long inviteTimeoutMs) {
        this.inviteTimeoutMs = inviteTimeoutMs;
    }

    public void addInvite(UUID invited, String clanName) {
        invites.computeIfAbsent(invited, k -> new HashMap<>())
                .put(clanName.toLowerCase(), System.currentTimeMillis());
    }

    public void consumeInvite(UUID invited, String clanName) {
        Map<String, Long> playerInvites = invites.get(invited);
        if (playerInvites == null) return;

        playerInvites.remove(clanName.toLowerCase());
        if (playerInvites.isEmpty()) {
            invites.remove(invited);
        }
    }

    public boolean hasValidInvite(UUID invited, String clanName) {
        Map<String, Long> playerInvites = invites.get(invited);
        if (playerInvites == null) return false;

        Long timestamp = playerInvites.get(clanName.toLowerCase());
        if (timestamp == null) return false;

        return System.currentTimeMillis() - timestamp <= inviteTimeoutMs;
    }

    public void clearInvites(UUID invited) {
        invites.remove(invited);
    }
}