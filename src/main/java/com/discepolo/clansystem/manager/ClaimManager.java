package com.discepolo.clansystem.manager;

import com.discepolo.clansystem.ClanSystem;
import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClaimedChunk;
import com.discepolo.clansystem.database.ClanRepository;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class ClaimManager {

    private final ClanSystem plugin;
    private final ClanRepository repository;
    private final Map<ClaimedChunk, Clan> claims = new HashMap<>();
    private final int maxClaimsPerClan;

    public ClaimManager(ClanSystem plugin, ClanRepository repository, int maxClaimsPerClan) {
        this.plugin = plugin;
        this.repository = repository;
        this.maxClaimsPerClan = maxClaimsPerClan;
    }

    public Clan getOwner(ClaimedChunk chunk) {
        return claims.get(chunk);
    }

    public boolean isClaimed(ClaimedChunk chunk) {
        return claims.containsKey(chunk);
    }

    public int getMaxClaimsPerClan() {
        return maxClaimsPerClan;
    }

    public void claim(Clan clan, ClaimedChunk chunk) {
        claims.put(chunk, clan);
        clan.addClaim(chunk);

        int id = clan.getId();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> repository.insertClaim(id, chunk));
    }

    public void unclaim(Clan clan, ClaimedChunk chunk) {
        claims.remove(chunk);
        clan.removeClaim(chunk);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> repository.deleteClaim(chunk));
    }

    public void unclaimAll(Clan clan) {
        for (ClaimedChunk chunk : clan.getClaims()) {
            claims.remove(chunk);
        }
        clan.getClaims().clear();
    }

    public void loadFromDatabase(Map<Integer, Clan> clansById) {
        repository.loadClaims(clansById, claims);
    }

    public int getClaimCount() {
        return claims.size();
    }
}