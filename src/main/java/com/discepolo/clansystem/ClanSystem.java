package com.discepolo.clansystem;

import com.discepolo.clansystem.command.ClanCommand;
import com.discepolo.clansystem.manager.ClanManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClanSystem extends JavaPlugin {

    private ClanManager clanManager;

    @Override
    public void onEnable() {
        clanManager = new ClanManager();

        ClanCommand router = new ClanCommand();
        getCommand("clan").setExecutor(router);

        getLogger().info("ClanSystem abilitato!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ClanSystem disabilitato.");
    }

    public ClanManager getClanManager() {
        return clanManager;
    }
}