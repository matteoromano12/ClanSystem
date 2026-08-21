package com.discepolo.clansystem;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClanRole;
import com.discepolo.clansystem.command.ClanCommand;
import com.discepolo.clansystem.command.sub.*;
import com.discepolo.clansystem.database.ClanRepository;
import com.discepolo.clansystem.database.DatabaseManager;
import com.discepolo.clansystem.listener.ClaimProtectionListener;
import com.discepolo.clansystem.listener.ClanChatListener;
import com.discepolo.clansystem.listener.TeleportCancelListener;
import com.discepolo.clansystem.manager.*;
import com.discepolo.clansystem.placeholder.ClanPlaceholders;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class ClanSystem extends JavaPlugin {

    private ClanManager clanManager;
    private InviteManager inviteManager;
    private ChatManager chatManager;
    private DatabaseManager databaseManager;
    private ClaimManager claimManager;
    private TeleportManager teleportManager;
    private ClanRepository clanRepository;


    @Override
    public void onEnable() {
        saveDefaultConfig();

        databaseManager = new DatabaseManager(this);
        if (!databaseManager.connect()) {
            getLogger().severe("Plugin disabilitato: impossibile raggiungere il database.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }



        clanRepository = new ClanRepository(databaseManager, getLogger());

        clanManager = new ClanManager(this, clanRepository);
        claimManager = new ClaimManager(this, clanRepository, getConfig().getInt("claims.max-per-clan", 6));
        inviteManager = new InviteManager();
        chatManager = new ChatManager();
        teleportManager = new TeleportManager();

        Map<Integer, Clan> clansById = clanManager.loadFromDatabase();
        claimManager.loadFromDatabase(clansById);
        getLogger().info("Caricati " + clanManager.getClanCount() + " clan e "
                + claimManager.getClaimCount() + " territori.");

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
        router.register(new ChunksCommand(clanManager, claimManager));

        ChatCommand chatCommand = new ChatCommand(clanManager, chatManager);
        router.register(chatCommand);
        
        getCommand("clan").setExecutor(router);
        getCommand("clan").setTabCompleter(router);

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
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ClanPlaceholders(this, clanManager).register();
            getLogger().info("Placeholder registrati (PlaceholderAPI trovato).");
        } else {
            getLogger().info("PlaceholderAPI non trovato: placeholder non disponibili.");
        }
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