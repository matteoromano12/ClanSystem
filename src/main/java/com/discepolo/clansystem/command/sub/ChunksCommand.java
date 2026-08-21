package com.discepolo.clansystem.command.sub;

import com.discepolo.clansystem.clan.ClaimedChunk;
import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClanMember;
import com.discepolo.clansystem.clan.ClanRole;
import com.discepolo.clansystem.command.SubCommand;
import com.discepolo.clansystem.manager.ClaimManager;
import com.discepolo.clansystem.manager.ClanManager;
import org.bukkit.entity.Player;

import java.util.List;

public class ChunksCommand implements SubCommand {

    private final ClanManager clanManager;
    private final ClaimManager claimManager;

    public ChunksCommand (ClanManager clanManager, ClaimManager claimManager) {this.clanManager = clanManager;this.claimManager = claimManager;}

    @Override
    public String getName(){return "chunks";}

    @Override
    public String getUsage(){return"/clan chunks - Informazioni sui chunk del clan";}

    @Override
    public void execute(Player player, String[] args) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cNon sei in nessun clan.");
            return;
        }

        ClanMember member = clan.getMember(player.getUniqueId());
        if (member.getRole() != ClanRole.LEADER) {
            player.sendMessage("§cSolo il leader può vedere i territori del clan.");
            return;
        }

        inviaChunks(player, clan);
    }

    private void inviaChunks(Player player, Clan clan) {
        if (clan.getClaimCount() == 0) {
            player.sendMessage("§cIl tuo clan non ha territori reclamati.");
            return;
        }

        player.sendMessage("§6▬▬▬▬ §eTerritori di " + clan.getName()
                + " §7(" + clan.getClaimCount() + "/" + claimManager.getMaxClaimsPerClan() + ") §6▬▬▬▬");

        for (ClaimedChunk chunk : clan.getClaims()) {
            player.sendMessage("§7" + chunk.getWorld() + " §8→ §fchunk " + chunk.getX() + ", " + chunk.getZ()
                    + " §7(x" + (chunk.getX() * 16) + ", z" + (chunk.getZ() * 16) + ")");
        }
    }
}
