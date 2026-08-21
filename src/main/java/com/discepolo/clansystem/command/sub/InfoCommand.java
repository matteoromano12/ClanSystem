package com.discepolo.clansystem.command.sub;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClanMember;
import com.discepolo.clansystem.clan.ClanRole;
import com.discepolo.clansystem.command.SubCommand;
import com.discepolo.clansystem.manager.ClanManager;
import org.bukkit.entity.Player;

import java.util.List;

public class InfoCommand implements SubCommand {

    private final ClanManager clanManager;

    public InfoCommand(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getUsage() {
        return "/clan info [clan] - Info clan";
    }

    @Override
    public void execute(Player player, String[] args) {
        Clan clan;

        if (args.length == 0) {
            clan = clanManager.getClanByPlayer(player.getUniqueId());
            if (clan == null) {
                player.sendMessage("§cNon sei in nessun clan. Usa /clan info <nome> per vederne un altro.");
                return;
            }
        } else {
            clan = clanManager.getClanByName(args[0]);
            if (clan == null) {
                player.sendMessage("§cNessun clan trovato con il nome §e" + args[0] + "§c.");
                return;
            }
        }

        sendClanInfo(player, clan);
    }

    private void sendClanInfo(Player player, Clan clan) {
        int online = clan.getOnlineMembers().size();
        int total = clan.getMembers().size();

        player.sendMessage("§6§m        §r §e" + clan.getName() + " §7[§e" + clan.getTag() + "§7] §6§m        ");
        player.sendMessage("§7Membri online: §a" + online + "§7/§f" + total);

        for (ClanRole role : List.of(ClanRole.LEADER, ClanRole.OFFICER, ClanRole.MEMBER)) {
            List<String> names = clan.getMembers().stream()
                    .filter(m -> m.getRole() == role)
                    .map(ClanMember::getLastKnownName)
                    .sorted()
                    .toList();

            if (!names.isEmpty()) {
                player.sendMessage("§6" + role.getDisplayName() + ": §f" + String.join("§7, §f", names));
            }
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length != 1) return List.of();

        return clanManager.getClans().stream()
                .map(Clan::getName)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .sorted()
                .toList();
    }
}