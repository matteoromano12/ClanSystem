package com.discepolo.clansystem.listener;

import com.discepolo.clansystem.manager.TeleportManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class TeleportCancelListener implements Listener {

    private final TeleportManager teleportManager;

    public TeleportCancelListener(TeleportManager teleportManager) {
        this.teleportManager = teleportManager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!teleportManager.isTeleporting(event.getPlayer().getUniqueId())) return;

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        if (teleportManager.cancel(event.getPlayer().getUniqueId())) {
            event.getPlayer().sendActionBar(
                    Component.text("Teletrasporto annullato", NamedTextColor.RED));
        }
    }
}