package com.discepolo.clansystem.manager;

import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {

    private final Map<UUID, BukkitTask> pending = new HashMap<>();

    public boolean isTeleporting(UUID uuid) {
        return pending.containsKey(uuid);
    }

    public void register(UUID uuid, BukkitTask task) {
        pending.put(uuid, task);
    }

    public boolean cancel(UUID uuid) {
        BukkitTask task = pending.remove(uuid);
        if (task == null) return false;
        task.cancel();
        return true;
    }

    public void finish(UUID uuid) {
        pending.remove(uuid);
    }
}