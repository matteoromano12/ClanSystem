package com.discepolo.clansystem.listener;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClaimedChunk;
import com.discepolo.clansystem.clan.ClanMember;
import com.discepolo.clansystem.clan.ClanRole;
import com.discepolo.clansystem.config.ProtectionSettings;
import com.discepolo.clansystem.manager.ClaimManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.*;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class ClaimProtectionListener implements Listener {

    private final ClaimManager claimManager;
    private final ClanRole buildRole;
    private final ClanRole interactRole;
    private final ProtectionSettings settings;

    public ClaimProtectionListener(ClaimManager claimManager,
                                   ClanRole buildRole,
                                   ClanRole interactRole,
                                   ProtectionSettings settings) {
        this.claimManager = claimManager;
        this.buildRole = buildRole;
        this.interactRole = interactRole;
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!settings.isBuild()) return;

        if (!canBuild(event.getPlayer(), event.getBlock().getChunk())) {
            event.setCancelled(true);
            deny(event.getPlayer(), "Non puoi distruggere blocchi in questo territorio");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!settings.isBuild()) return;

        if (!canBuild(event.getPlayer(), event.getBlock().getChunk())) {
            event.setCancelled(true);
            deny(event.getPlayer(), "Non puoi piazzare blocchi in questo territorio");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;

        if (event.getAction() == Action.PHYSICAL) {
            if (!settings.isTrampling()) return;
            if (block.getType() != Material.FARMLAND) return;

            if (!canBuild(event.getPlayer(), block.getChunk())) {
                event.setCancelled(true);
            }
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!settings.isInteract()) return;

        if (!canInteract(event.getPlayer(), block.getChunk())) {
            event.setCancelled(true);
            deny(event.getPlayer(), "Non puoi interagire in questo territorio");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!settings.isBuckets()) return;

        if (!canBuild(event.getPlayer(), event.getBlock().getChunk())) {
            event.setCancelled(true);
            deny(event.getPlayer(), "Non puoi usare secchi in questo territorio");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (!settings.isBuckets()) return;

        if (!canBuild(event.getPlayer(), event.getBlock().getChunk())) {
            event.setCancelled(true);
            deny(event.getPlayer(), "Non puoi usare secchi in questo territorio");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!settings.isEntities()) return;

        Player remover = resolvePlayer(event.getRemover());
        if (remover == null) {
            if (isClaimed(event.getEntity().getLocation().getChunk())) {
                event.setCancelled(true);
            }
            return;
        }

        if (!canBuild(remover, event.getEntity().getLocation().getChunk())) {
            event.setCancelled(true);
            deny(remover, "Non puoi rompere questo in questo territorio");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (!settings.isEntities()) return;
        if (event.getPlayer() == null) return;

        if (!canBuild(event.getPlayer(), event.getEntity().getLocation().getChunk())) {
            event.setCancelled(true);
            deny(event.getPlayer(), "Non puoi piazzare questo in questo territorio");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!settings.isEntities()) return;

        Entity target = event.getRightClicked();
        if (!(target instanceof Hanging) && !(target instanceof ArmorStand)) return;

        if (!canInteract(event.getPlayer(), target.getLocation().getChunk())) {
            event.setCancelled(true);
            deny(event.getPlayer(), "Non puoi interagire in questo territorio");
        }
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Player attacker = resolvePlayer(event.getDamager());
        if (attacker == null) return;

        Entity victim = event.getEntity();

        if (settings.isEntities() && (victim instanceof Hanging || victim instanceof ArmorStand)) {
            if (!canBuild(attacker, victim.getLocation().getChunk())) {
                event.setCancelled(true);
                deny(attacker, "Non puoi distruggere questo in questo territorio");
            }
            return;
        }

        if (!settings.isPvp()) return;
        if (!(victim instanceof Player)) return;
        if (attacker.getUniqueId().equals(victim.getUniqueId())) return;

        if (isClaimed(victim.getLocation().getChunk())) {
            event.setCancelled(true);
            deny(attacker, "PvP disabilitato nei territori dei clan");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!settings.isExplosions()) return;
        filterExplosion(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!settings.isExplosions()) return;
        filterExplosion(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFireSpread(BlockSpreadEvent event) {
        if (!settings.isFireSpread()) return;
        if (event.getSource().getType() != Material.FIRE) return;

        if (isClaimed(event.getBlock().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (!settings.isFireSpread()) return;

        if (isClaimed(event.getBlock().getChunk())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!settings.isMobSpawning()) return;
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.NATURAL) return;

        if (isClaimed(event.getLocation().getChunk())) {
            event.setCancelled(true);
        }
    }

    private void filterExplosion(List<Block> blocks) {
        blocks.removeIf(block -> isClaimed(block.getChunk()));
    }

    private Player resolvePlayer(Entity source) {
        if (source instanceof Player player) {
            return player;
        }
        if (source instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player player) {
                return player;
            }
        }
        return null;
    }

    private boolean isClaimed(Chunk chunk) {
        return claimManager.isClaimed(ClaimedChunk.fromChunk(chunk));
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

    private void deny(Player player, String message) {
        player.sendActionBar(Component.text(message, NamedTextColor.RED));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFluidFlow(BlockFromToEvent event) {
        if (!settings.isBuckets()) return;

        Chunk fromChunk = event.getBlock().getChunk();
        Chunk toChunk = event.getToBlock().getChunk();

        if (fromChunk.getX() == toChunk.getX()
                && fromChunk.getZ() == toChunk.getZ()
                && fromChunk.getWorld().equals(toChunk.getWorld())) {
            return;
        }

        Clan to = claimManager.getOwner(ClaimedChunk.fromChunk(toChunk));
        if (to == null) return;

        Clan from = claimManager.getOwner(ClaimedChunk.fromChunk(fromChunk));
        if (to != from) {
            event.setCancelled(true);
        }
    }
}