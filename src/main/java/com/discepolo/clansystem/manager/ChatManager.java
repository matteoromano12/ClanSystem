package com.discepolo.clansystem.manager;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClanMember;

public class ChatManager {

    private final Set<UUID> toggled = new HashSet<>();

    public boolean toggle(UUID uuid) {
        if (toggled.remove(uuid)) {
            return false;
        }
        toggled.add(uuid);
        return true;
    }

    public boolean isToggled(UUID uuid) {
        return toggled.contains(uuid);
    }

    public void disable(UUID uuid) {
        toggled.remove(uuid);
    }

    public void sendClanMessage(Clan clan, ClanMember sender, String message) {
        clan.broadcastMessage("§7" + sender.getRole() + "§f "+sender.getLastKnownName()
                + " §7» §f" + message);
    }
}