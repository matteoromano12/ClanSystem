package com.discepolo.clansystem.command.sub;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClanMember;
import com.discepolo.clansystem.clan.ClanRole;
import com.discepolo.clansystem.command.SubCommand;
import com.discepolo.clansystem.manager.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class DemoteCommand implements SubCommand {

    private final ClanManager clanManager;

    public DemoteCommand(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    @Override
    public String getName() {
        return "demote";
    }

    @Override
    public String getUsage() {
        return "/clan demote <player>";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage("§cUso corretto: " + getUsage());
            return;
        }

        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cNon sei in nessun clan.");
            return;
        }

        ClanMember self = clan.getMember(player.getUniqueId());
        if (self.getRole() != ClanRole.LEADER) {
            player.sendMessage("§cSolo il leader può degradare.");
            return;
        }

        ClanMember target = clan.getMemberByName(args[0]);
        if (target == null) {
            player.sendMessage("§c" + args[0] + " non è un membro del tuo clan.");
            return;
        }

        if (target.getUuid().equals(player.getUniqueId())) {
            player.sendMessage("§cNon puoi degradare te stesso.");
            return;
        }

        if (target.getRole() == ClanRole.MEMBER) {
            player.sendMessage("§c" + target.getLastKnownName()
                    + " è già Membro. Per espellerlo usa /clan kick.");
            return;
        }

        ClanRole newRole = target.getRole().previous();
        target.setRole(newRole);

        String targetName = target.getLastKnownName();
        clan.broadcastMessage("§e" + targetName + " §cè stato degradato a §7"
                + newRole.getDisplayName() + "§c.");

        Player targetPlayer = Bukkit.getPlayer(target.getUuid());
        if (targetPlayer != null) {
            targetPlayer.sendMessage("§cSei stato degradato a §7" + newRole.getDisplayName() + "§c.");
        }
    }
}