package com.discepolo.clansystem.listener;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.manager.ChatManager;
import com.discepolo.clansystem.manager.ClanManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ClanChatListener implements Listener {

    private final ClanManager clanManager;
    private final ChatManager chatManager;

    public ClanChatListener(ClanManager clanManager, ChatManager chatManager) {
        this.clanManager = clanManager;
        this.chatManager = chatManager;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (!chatManager.isToggled(player.getUniqueId())) {
            return;
        }

        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            chatManager.disable(player.getUniqueId());
            return;
        }

        event.setCancelled(true);

        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        chatManager.sendClanMessage(clan, clan.getMember(player.getUniqueId()), message);
    }
}