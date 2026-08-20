package com.discepolo.clansystem.command.sub;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClaimedChunk;
import com.discepolo.clansystem.clan.ClanMember;
import com.discepolo.clansystem.clan.ClanRole;
import com.discepolo.clansystem.command.SubCommand;
import com.discepolo.clansystem.manager.ClaimManager;
import com.discepolo.clansystem.manager.ClanManager;
import org.bukkit.entity.Player;

public class ClaimCommand implements SubCommand {

    private final ClanManager clanManager;
    private final ClaimManager claimManager;

    public ClaimCommand(ClanManager clanManager, ClaimManager claimManager) {
        this.clanManager = clanManager;
        this.claimManager = claimManager;
    }

    @Override
    public String getName() {
        return "claim";
    }

    @Override
    public String getUsage() {
        return "/clan claim/unclaim - Gestisci i territori";
    }

    @Override
    public void execute(Player player, String[] args) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cNon sei in nessun clan.");
            return;
        }

        ClanMember member = clan.getMember(player.getUniqueId());
        if (!member.getRole().isAtLeast(ClanRole.OFFICER)) {
            player.sendMessage("§cSolo officer e leader possono reclamare territori.");
            return;
        }

        ClaimedChunk chunk = ClaimedChunk.fromChunk(player.getLocation().getChunk());

        Clan owner = claimManager.getOwner(chunk);
        if (owner != null) {
            if (owner == clan) {
                player.sendMessage("§cQuesto territorio è già del tuo clan.");
            } else {
                player.sendMessage("§cQuesto territorio appartiene al clan §e" + owner.getName() + "§c.");
            }
            return;
        }

        int max = claimManager.getMaxClaimsPerClan();
        if (clan.getClaimCount() >= max) {
            player.sendMessage("§cHai raggiunto il limite di §e" + max + " §cterritori.");
            return;
        }

        claimManager.claim(clan, chunk);
        player.sendMessage("§aTerritorio claimato! §7(chunk {" + (chunk.getX() * 16) + ", " + (chunk.getZ() * 16)
                + "} — §e" + clan.getClaimCount() + "§7/§e" + max + "§7)");
        clan.broadcastMessage("§e" + player.getName() + " §aha claimato un nuovo territorio.");
    }
}