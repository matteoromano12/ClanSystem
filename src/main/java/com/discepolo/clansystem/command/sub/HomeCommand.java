package com.discepolo.clansystem.command.sub;

import com.discepolo.clansystem.ClanSystem;
import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.command.SubCommand;
import com.discepolo.clansystem.manager.ClanManager;
import com.discepolo.clansystem.manager.TeleportManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class HomeCommand implements SubCommand {

    private static final int COUNTDOWN_SECONDS = 5;

    private final ClanSystem plugin;
    private final ClanManager clanManager;
    private final TeleportManager teleportManager;

    public HomeCommand(ClanSystem plugin, ClanManager clanManager, TeleportManager teleportManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
        this.teleportManager = teleportManager;
    }

    @Override
    public String getName() { return "home"; }

    @Override
    public String getUsage() { return "/clan home - Teletrasportati alla home del clan"; }

    @Override
    public void execute(Player player, String[] args) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cNon sei in nessun clan.");
            return;
        }

        if (!clan.hasHome()) {
            player.sendMessage("§cIl tuo clan non ha una home impostata.");
            return;
        }

        if (teleportManager.isTeleporting(player.getUniqueId())) {
            player.sendMessage("§cHai già un teletrasporto in corso.");
            return;
        }

        Location destination = clan.getHome();

        BukkitTask task = new BukkitRunnable() {
            int remaining = COUNTDOWN_SECONDS;

            @Override
            public void run() {
                if (remaining > 0) {
                    player.sendActionBar(Component.text(
                            "Teletrasporto tra " + remaining + "s. Non muoverti!",
                            NamedTextColor.YELLOW));
                    remaining--;
                    return;
                }

                teleportManager.finish(player.getUniqueId());
                player.teleport(destination);
                player.sendActionBar(Component.text("Teletrasportato!", NamedTextColor.GREEN));
                cancel();
            }
        }.runTaskTimer(plugin, 0L, 20L);

        teleportManager.register(player.getUniqueId(), task);
    }
}