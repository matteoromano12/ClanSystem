package com.discepolo.clansystem.clan;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class Clan {

    private final String name;
    private final String tag;
    private final Map<UUID, ClanMember> members = new HashMap<>();

    public Clan(String name, String tag) {
        this.name = name;
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }


    public Collection<ClanMember> getMembers() {
        return members.values();
    }

    public ClanMember getMember(UUID uuid) {
        return members.get(uuid);
    }

    public boolean isMember(UUID uuid) {
        return members.containsKey(uuid);
    }

    public void addMember(ClanMember member) {
        members.put(member.getUuid(), member);
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public List<Player> getOnlineMembers() {
        return members.keySet().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .toList();
    }
}