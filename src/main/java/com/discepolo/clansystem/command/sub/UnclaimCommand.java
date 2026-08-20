package com.discepolo.clansystem.command.sub;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClaimedChunk;
import com.discepolo.clansystem.clan.ClanMember;
import com.discepolo.clansystem.clan.ClanRole;
import com.discepolo.clansystem.command.SubCommand;
import com.discepolo.clansystem.manager.ClaimManager;
import com.discepolo.clansystem.manager.ClanManager;
import org.bukkit.entity.Player;

public class UnclaimCommand implements SubCommand {

    private final ClanManager clanManager;
    private final ClaimManager claimManager;

    public UnclaimCommand(ClanManager clanManager, ClaimManager claimManager) {
        this.clanManager = clanManager;
        this.claimManager = claimManager;
    }

    @Override
    public String getName() {
        return "unclaim";
    }

    @Override
    public String getUsage() {
        return "/clan unclaim";
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
            player.sendMessage("§cSolo officer e leader possono unclaimare territori.");
            return;
        }

        ClaimedChunk chunk = ClaimedChunk.fromChunk(player.getLocation().getChunk());

        Clan owner = claimManager.getOwner(chunk);
        if (owner == null) {
            player.sendMessage("§cQuesto territorio non è claimato.");
            return;
        }
        if (owner != clan) {
            player.sendMessage("§cQuesto territorio è già claimato da un altro clan.");
            return;
        }

        claimManager.unclaim(clan, chunk);
        player.sendMessage("§aTerritorio unclaimato. §7(" + clan.getClaimCount()
                + "/" + claimManager.getMaxClaimsPerClan() + ")");
        clan.broadcastMessage("§e" + player.getName() + " §7ha unclaimato un territorio.");
        if (clan.hasHome() && ClaimedChunk.fromChunk(clan.getHome().getChunk()).equals(chunk)) {
            clan.setHome(null);
            player.sendMessage("§eLa home del clan era in questo territorio ed è stata rimossa.");
            clan.broadcastMessage("§7La home del clan è stata rimossa (territorio unclaimato).");
        }
    }
}