package com.discepolo.clansystem.command.sub;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.command.SubCommand;
import com.discepolo.clansystem.manager.ChatManager;
import com.discepolo.clansystem.manager.ClanManager;
import org.bukkit.entity.Player;

public class ChatCommand implements SubCommand {

    private final ClanManager clanManager;
    private final ChatManager chatManager;

    public ChatCommand(ClanManager clanManager, ChatManager chatManager) {
        this.clanManager = clanManager;
        this.chatManager = chatManager;
    }

    @Override
    public String getName() {
        return "chat";
    }

    @Override
    public String getUsage() {
        return "/clan chat [messaggio]";
    }

    @Override
    public void execute(Player player, String[] args) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cNon sei in nessun clan.");
            return;
        }

        if (args.length == 0) {
            boolean active = chatManager.toggle(player.getUniqueId());
            player.sendMessage(active
                    ? "§aModalità chat clan §2attivata§a: ora scrivi direttamente al clan."
                    : "§aModalità chat clan §cdisattivata§a: torni in chat globale.");
            return;
        }

        String message = String.join(" ", args);
        chatManager.sendClanMessage(clan, clan.getMember(player.getUniqueId()), message);
    }
}