package com.discepolo.clansystem.manager;

import com.discepolo.clansystem.ClanSystem;
import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClanMember;
import com.discepolo.clansystem.clan.ClanRole;
import com.discepolo.clansystem.database.ClanRepository;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClanManager {

    private final ClanSystem plugin;
    private final ClanRepository repository;

    private final Map<String, Clan> clansByName = new HashMap<>();
    private final Map<UUID, Clan> clansByPlayer = new HashMap<>();

    public ClanManager(ClanSystem plugin, ClanRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

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
        ClanMember founder = new ClanMember(founderUuid, founderName, ClanRole.LEADER);
        clan.addMember(founder);

        clansByName.put(name.toLowerCase(), clan);
        clansByPlayer.put(founderUuid, clan);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            repository.insertClan(clan);
            repository.insertMember(clan.getId(), founder);
        });

        return clan;
    }

    public void disbandClan(Clan clan) {
        int id = clan.getId();
        for (ClanMember member : clan.getMembers()) {
            clansByPlayer.remove(member.getUuid());
        }
        clansByName.remove(clan.getName().toLowerCase());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> repository.deleteClan(id));
    }

    public void addPlayerToClan(Clan clan, UUID uuid, String name) {
        ClanMember member = new ClanMember(uuid, name, ClanRole.MEMBER);
        clan.addMember(member);
        clansByPlayer.put(uuid, clan);

        int id = clan.getId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> repository.insertMember(id, member));
    }

    public void removePlayerFromClan(Clan clan, UUID uuid) {
        clan.removeMember(uuid);
        clansByPlayer.remove(uuid);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> repository.deleteMember(uuid));
    }

    public void updateRole(ClanMember member, ClanRole newRole) {
        member.setRole(newRole);

        UUID uuid = member.getUuid();
        String role = newRole.name();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> repository.updateMemberRole(uuid, role));
    }

    public void setHome(Clan clan, org.bukkit.Location home) {
        clan.setHome(home);

        int id = clan.getId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> repository.updateHome(id, home));
    }
}