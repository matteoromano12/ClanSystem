package com.discepolo.clansystem.clan;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class Clan {

    private int id = -1;
    private final String name;
    private final String tag;
    private final Map<UUID, ClanMember> members = new HashMap<>();
    private final Set<ClaimedChunk> claims = new HashSet<>();
    private Location home;
    private final long createdAt;

    public Clan(String name, String tag, long createdAt) {
        this.name = name;
        this.tag = tag;
        this.createdAt = createdAt;
    }

    public Clan(String name, String tag) {
        this(name, tag, System.currentTimeMillis());
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

    public void broadcastMessage(String message) {
        getOnlineMembers().forEach(p -> p.sendMessage("§a§l["+tag+"]§e§r "+message));
    }

    public ClanMember getMemberByName(String name) {
        return members.values().stream()
                .filter(m -> m.getLastKnownName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public Set<ClaimedChunk> getClaims() {
        return claims;
    }

    public void addClaim(ClaimedChunk chunk) {
        claims.add(chunk);
    }

    public void removeClaim(ClaimedChunk chunk) {
        claims.remove(chunk);
    }

    public int getClaimCount() {
        return claims.size();
    }

    public Location getHome() { return home; }

    public void setHome(Location home) { this.home = home; }

    public boolean hasHome() { return home != null; }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public long getCreatedAt() { return createdAt; }
}