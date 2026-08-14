package com.discepolo.clansystem.manager;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClanMember;
import com.discepolo.clansystem.clan.ClanRole;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClanManager {

    private final Map<String, Clan> clansByName = new HashMap<>();
    private final Map<UUID, Clan> clansByPlayer = new HashMap<>();

    public Clan getClanByName(String name) {
        return clansByName.get(name.toLowerCase());
    }

    public Clan getClanByPlayer(UUID uuid) {
        return clansByPlayer.get(uuid);
    }

    public boolean isTagTaken(String tag) {
        return clansByName.values().stream()
                .anyMatch(c -> c.getTag().equalsIgnoreCase(tag));
    }

    public Collection<Clan> getClans() {
        return clansByName.values();
    }

    public Clan createClan(String name, String tag, UUID founderUuid, String founderName) {
        Clan clan = new Clan(name, tag);
        clan.addMember(new ClanMember(founderUuid, founderName, ClanRole.LEADER));

        clansByName.put(name.toLowerCase(), clan);
        clansByPlayer.put(founderUuid, clan);
        return clan;
    }

    public void disbandClan(Clan clan) {
        for (ClanMember member : clan.getMembers()) {
            clansByPlayer.remove(member.getUuid());
        }
        clansByName.remove(clan.getName().toLowerCase());
    }

    public void addPlayerToClan(Clan clan, UUID uuid, String name) {
        clan.addMember(new ClanMember(uuid, name, ClanRole.MEMBER));
        clansByPlayer.put(uuid, clan);
    }

    public void removePlayerFromClan(Clan clan, UUID uuid) {
        clan.removeMember(uuid);
        clansByPlayer.remove(uuid);
    }
}