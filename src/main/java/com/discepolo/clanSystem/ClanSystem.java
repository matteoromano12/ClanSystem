package com.discepolo.clanSystem;

import org.bukkit.plugin.java.JavaPlugin;

public final class ClanSystem extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("ClanSystem abilitato");
    }

    @Override
    public void onDisable() {
        getLogger().info("ClanSystem disabilitato");
    }
}
