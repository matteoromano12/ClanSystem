package com.discepolo.clansystem;

import com.discepolo.clansystem.command.ClanCommand;
import com.discepolo.clansystem.command.sub.*;
import com.discepolo.clansystem.manager.ClanManager;
import com.discepolo.clansystem.manager.InviteManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClanSystem extends JavaPlugin {

    private ClanManager clanManager;
    private InviteManager inviteManager;

    @Override
    public void onEnable() {
        clanManager = new ClanManager();
        inviteManager = new InviteManager();

        ClanCommand router = new ClanCommand();
        router.register(new CreateCommand(clanManager));
        router.register(new InfoCommand(clanManager));
        router.register(new DisbandCommand(clanManager));
        router.register(new InviteCommand(clanManager, inviteManager));
        router.register(new JoinCommand(clanManager, inviteManager));
        router.register(new LeaveCommand(clanManager));
        router.register(new TransferCommand(clanManager));
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