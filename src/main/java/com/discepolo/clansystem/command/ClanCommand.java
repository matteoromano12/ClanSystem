package com.discepolo.clansystem.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ClanCommand implements CommandExecutor {

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
            if (!Objects.equals(sub.getName(), "join") || !Objects.equals(sub.getName(), "demote") || !Objects.equals(sub.getName(), "unclaim") ) {
                player.sendMessage("§e" + sub.getUsage());
            }
        }
    }
}