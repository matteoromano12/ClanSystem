package com.discepolo.clansystem.command.sub;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClanMember;
import com.discepolo.clansystem.clan.ClanRole;
import com.discepolo.clansystem.command.SubCommand;
import com.discepolo.clansystem.manager.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class PromoteCommand implements SubCommand {

    private final ClanManager clanManager;

    public PromoteCommand(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    @Override
    public String getName() {
        return "promote";
    }

    @Override
    public String getUsage() {
        return "/clan promote/demote <player> - Gestisci i ruoli";
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
            player.sendMessage("§cSolo il leader può promuovere.");
            return;
        }

        ClanMember target = clan.getMemberByName(args[0]);
        if (target == null) {
            player.sendMessage("§c" + args[0] + " non è un membro del tuo clan.");
            return;
        }

        if (target.getUuid().equals(player.getUniqueId())) {
            player.sendMessage("§cSei il leader, non puoi promuovere te stesso.");
            return;
        }

        if (target.getRole() == ClanRole.OFFICER) {
            player.sendMessage("§c" + target.getLastKnownName()
                    + " è già Officer. Per promouvero a leader, usa /clan transfer.");
            return;
        }

        ClanRole newRole = target.getRole().next();
        clanManager.updateRole(target, newRole);

        String targetName = target.getLastKnownName();
        clan.broadcastMessage("§e" + targetName + " §aè stato promosso a §6"
                + newRole.getDisplayName() + "§a!");

        Player targetPlayer = Bukkit.getPlayer(target.getUuid());
        if (targetPlayer != null) {
            targetPlayer.sendMessage("§aSei stato promosso a §6" + newRole.getDisplayName() + "§a!");
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length != 1) return List.of();

        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) return List.of();

        return clan.getMembers().stream()
                .map(ClanMember::getLastKnownName)
                .filter(name -> !name.equals(player.getName()))
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .sorted()
                .toList();
    }
}