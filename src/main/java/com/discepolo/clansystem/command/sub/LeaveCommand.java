package com.discepolo.clansystem.command.sub;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClanMember;
import com.discepolo.clansystem.clan.ClanRole;
import com.discepolo.clansystem.command.SubCommand;
import com.discepolo.clansystem.manager.ClanManager;
import org.bukkit.entity.Player;

public class LeaveCommand implements SubCommand {

    private final ClanManager clanManager;

    public LeaveCommand(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public String getUsage() {
        return "/clan leave";
    }

    @Override
    public void execute(Player player, String[] args) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cNon sei in nessun clan.");
            return;
        }

        ClanMember member = clan.getMember(player.getUniqueId());
        if (member.getRole() == ClanRole.LEADER) {
            player.sendMessage("§cSei il leader: non puoi lasciare il clan.");
            player.sendMessage("§7Sciogli il clan con §e/clan disband §7oppure passa la leadership con §e/clan transfer <player>§7.");
            return;
        }

        clanManager.removePlayerFromClan(clan, player.getUniqueId());
        clan.broadcastMessage("§e" + player.getName() + " §cha lasciato il clan.");
        player.sendMessage("§aHai lasciato il clan §e" + clan.getName() + "§a.");
    }
}