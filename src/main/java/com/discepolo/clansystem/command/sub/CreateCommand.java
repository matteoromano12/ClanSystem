package com.discepolo.clansystem.command.sub;

import com.discepolo.clansystem.command.SubCommand;
import com.discepolo.clansystem.manager.ClanManager;
import org.bukkit.entity.Player;

public class CreateCommand implements SubCommand {

    private final ClanManager clanManager;

    public CreateCommand(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getUsage() {
        return "/clan create <nome> <tag>";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 2) {
            player.sendMessage("§cUso corretto: " + getUsage());
            return;
        }

        String name = args[0];
        String tag = args[1];

        if (clanManager.getClanByPlayer(player.getUniqueId()) != null) {
            player.sendMessage("§cSei già in un clan!");
            return;
        }

        if (!name.matches("[a-zA-Z0-9]{3,16}")) {
            player.sendMessage("§cNome non valido: 3-16 caratteri, solo lettere e numeri.");
            return;
        }
        if (!tag.matches("[a-zA-Z0-9]{2,5}")) {
            player.sendMessage("§cTag non valido: 2-5 caratteri, solo lettere e numeri.");
            return;
        }

        if (clanManager.getClanByName(name) != null) {
            player.sendMessage("§cEsiste già un clan con questo nome.");
            return;
        }
        if (clanManager.isTagTaken(tag)) {
            player.sendMessage("§cQuesto tag è già in uso.");
            return;
        }

        clanManager.createClan(name, tag, player.getUniqueId(), player.getName());
        player.sendMessage("§aClan §e" + name + " §a[§e" + tag + "§a] creato! Ne sei il leader.");
    }
}