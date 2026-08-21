package com.discepolo.clansystem.command.sub;

import com.discepolo.clansystem.command.SubCommand;
import com.discepolo.clansystem.manager.ClanManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class CreateCommand implements SubCommand {

    private final ClanManager clanManager;

    private final int nameMinLength;
    private final int nameMaxLength;
    private final int tagMinLength;
    private final int tagMaxLength;

    public CreateCommand(ClanManager clanManager, FileConfiguration config) {
        this.clanManager = clanManager;
        this.nameMinLength = config.getInt("clan.name-min-length", 3);
        this.nameMaxLength = config.getInt("clan.name-max-length", 16);
        this.tagMinLength = config.getInt("clan.tag-min-length", 2);
        this.tagMaxLength = config.getInt("clan.tag-max-length", 5);
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getUsage() {
        return "/clan create <nome> <tag> - Crea nuovo clan";
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

        if (!name.matches("[a-zA-Z0-9]{" + nameMinLength + "," + nameMaxLength + "}")) {
            player.sendMessage("§cNome non valido: " + nameMinLength + "-" + nameMaxLength
                    + " caratteri, solo lettere e numeri.");
            return;
        }
        if (!tag.matches("[a-zA-Z0-9]{" + tagMinLength + "," + tagMaxLength + "}")) {
            player.sendMessage("§cTag non valido: " + tagMinLength + "-" + tagMaxLength
                    + " caratteri, solo lettere e numeri.");
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