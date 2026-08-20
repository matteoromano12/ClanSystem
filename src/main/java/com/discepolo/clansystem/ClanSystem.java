package com.discepolo.clansystem;

import com.discepolo.clansystem.clan.ClanRole;
import com.discepolo.clansystem.command.ClanCommand;
import com.discepolo.clansystem.command.sub.*;
import com.discepolo.clansystem.database.DatabaseManager;
import com.discepolo.clansystem.listener.ClaimProtectionListener;
import com.discepolo.clansystem.listener.ClanChatListener;
import com.discepolo.clansystem.listener.TeleportCancelListener;
import com.discepolo.clansystem.manager.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClanSystem extends JavaPlugin {

    private ClanManager clanManager;
    private InviteManager inviteManager;
    private ChatManager chatManager;
    private DatabaseManager databaseManager;
    private ClaimManager claimManager;
    private TeleportManager teleportManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        databaseManager = new DatabaseManager(this);
        if (!databaseManager.connect()) {
            getLogger().severe("Plugin disabilitato: impossibile raggiungere il database.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }


        clanManager = new ClanManager();
        inviteManager = new InviteManager();
        chatManager = new ChatManager();
        claimManager = new ClaimManager(getConfig().getInt("claims.max-per-clan", 6));
        teleportManager = new TeleportManager();

        ClanRole buildRole = parseRole(getConfig().getString("claims.build-role", "LEADER"), ClanRole.LEADER);
        ClanRole interactRole = parseRole(getConfig().getString("claims.interact-role", "MEMBER"), ClanRole.MEMBER);

        ClanCommand router = new ClanCommand();
        router.register(new CreateCommand(clanManager));
        router.register(new DisbandCommand(clanManager, claimManager));
        router.register(new InviteCommand(clanManager, inviteManager));
        router.register(new JoinCommand(clanManager, inviteManager));
        router.register(new KickCommand(clanManager));
        router.register(new PromoteCommand(clanManager));
        router.register(new DemoteCommand(clanManager));
        router.register(new LeaveCommand(clanManager));
        router.register(new TransferCommand(clanManager));
        router.register(new InfoCommand(clanManager));
        router.register(new ClaimCommand(clanManager, claimManager));
        router.register(new UnclaimCommand(clanManager, claimManager));
        router.register(new SetHomeCommand(clanManager, claimManager));
        router.register(new HomeCommand(this, clanManager, teleportManager));

        ChatCommand chatCommand = new ChatCommand(clanManager, chatManager);
        router.register(chatCommand);

        getCommand("clan").setExecutor(router);

        getCommand("clanchat").setExecutor((sender, cmd, label, args) -> {
            if (sender instanceof Player p) {
                chatCommand.execute(p, args);
            } else {
                sender.sendMessage("Solo i giocatori possono usare la chat clan.");
            }
            return true;
        });

        getServer().getPluginManager().registerEvents(
                new ClanChatListener(clanManager, chatManager), this);
        getServer().getPluginManager().registerEvents(
                new ClaimProtectionListener(claimManager, buildRole, interactRole), this);
        getServer().getPluginManager().registerEvents(
                new TeleportCancelListener(teleportManager), this);

        getLogger().info("ClanSystem abilitato!");
    }

    private ClanRole parseRole(String value, ClanRole fallback) {
        try {
            return ClanRole.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Ruolo non valido nel config: " + value + ". Uso " + fallback + ".");
            return fallback;
        }
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) databaseManager.close();
        getLogger().info("ClanSystem disabilitato.");
    }

    public ClanManager getClanManager() {
        return clanManager;
    }
}