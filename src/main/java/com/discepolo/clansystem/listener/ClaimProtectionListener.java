package com.discepolo.clansystem.listener;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClaimedChunk;
import com.discepolo.clansystem.clan.ClanMember;
import com.discepolo.clansystem.clan.ClanRole;
import com.discepolo.clansystem.manager.ClaimManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ClaimProtectionListener implements Listener {

    private final ClaimManager claimManager;
    private final ClanRole buildRole;
    private final ClanRole interactRole;

    public ClaimProtectionListener(ClaimManager claimManager, ClanRole buildRole, ClanRole interactRole) {
        this.claimManager = claimManager;
        this.buildRole = buildRole;
        this.interactRole = interactRole;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!canBuild(event.getPlayer(), event.getBlock().getChunk())) {
            event.setCancelled(true);
            event.getPlayer().sendActionBar(
                    Component.text("Non puoi distruggere blocchi in questo territorio", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!canBuild(event.getPlayer(), event.getBlock().getChunk())) {
            event.setCancelled(true);
            event.getPlayer().sendActionBar(
                    Component.text("Non puoi piazzare blocchi in questo territorio", NamedTextColor.RED));
        }
    }


    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        if (!canInteract(event.getPlayer(), event.getClickedBlock().getChunk())) {
            event.setCancelled(true);
            event.getPlayer().sendActionBar(
                    Component.text("Non puoi interagire in questo territorio", NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onPvp(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        if (claimManager.isClaimed(ClaimedChunk.fromChunk(victim.getLocation().getChunk()))) {
            event.setCancelled(true);
            attacker.sendActionBar(
                    Component.text("PvP disabilitato nei territori dei clan", NamedTextColor.RED));
        }
    }


    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) return;

        if (claimManager.isClaimed(ClaimedChunk.fromChunk(event.getLocation().getChunk()))) {
            event.setCancelled(true);
        }
    }

    private boolean canBuild(Player player, Chunk chunk) {
        return hasAccess(player, chunk, buildRole);
    }

    private boolean canInteract(Player player, Chunk chunk) {
        return hasAccess(player, chunk, interactRole);
    }

    private boolean hasAccess(Player player, Chunk chunk, ClanRole requiredRole) {
        Clan owner = claimManager.getOwner(ClaimedChunk.fromChunk(chunk));

        if (owner == null) return true;

        ClanMember member = owner.getMember(player.getUniqueId());
        if (member == null) return false;

        return member.getRole().isAtLeast(requiredRole);
    }
}