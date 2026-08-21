package com.discepolo.clansystem.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class ClanCommand implements CommandExecutor, TabCompleter {

    private final Map<String, SubCommand> subCommands = new HashMap<>();

    public void register(SubCommand sub) {
        subCommands.put(sub.getName().toLowerCase(), sub);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo i giocatori possono usare i comandi clan.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) {
            player.sendMessage("§cComando sconosciuto. Scrivi /clan per la lista.");
            return true;
        }

        sub.execute(player, Arrays.copyOfRange(args, 1, args.length));
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6=== ClanSystem ===");
        for (SubCommand sub : subCommands.values()) {
            if (!Objects.equals(sub.getName(), "join") && !Objects.equals(sub.getName(), "demote") && !Objects.equals(sub.getName(), "unclaim") ) {
                player.sendMessage("§e" + sub.getUsage());
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return List.of();

        if (args.length == 1) {
            return subCommands.keySet().stream()
                    .filter(name -> name.startsWith(args[0].toLowerCase()))
                    .sorted()
                    .toList();
        }


        SubCommand sub = subCommands.get(args[0].toLowerCase());
        if (sub == null) return List.of();

        return sub.tabComplete(player, Arrays.copyOfRange(args, 1, args.length));
    }
}