package com.discepolo.clansystem.manager;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClaimedChunk;

import java.util.HashMap;
import java.util.Map;

public class ClaimManager {

    private final Map<ClaimedChunk, Clan> claims = new HashMap<>();

    private final int maxClaimsPerClan;

    public ClaimManager(int maxClaimsPerClan) {
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
    }

    public void unclaim(Clan clan, ClaimedChunk chunk) {
        claims.remove(chunk);
        clan.removeClaim(chunk);
    }

    public void unclaimAll(Clan clan) {
        for (ClaimedChunk chunk : clan.getClaims()) {
            claims.remove(chunk);
        }
        clan.getClaims().clear();
    }
}