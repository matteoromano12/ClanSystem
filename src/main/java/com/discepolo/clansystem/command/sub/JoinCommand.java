package com.discepolo.clansystem.command.sub;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.command.SubCommand;
import com.discepolo.clansystem.manager.ClanManager;
import com.discepolo.clansystem.manager.InviteManager;
import org.bukkit.entity.Player;

public class JoinCommand implements SubCommand {

    private final ClanManager clanManager;
    private final InviteManager inviteManager;

    public JoinCommand(ClanManager clanManager, InviteManager inviteManager) {
        this.clanManager = clanManager;
        this.inviteManager = inviteManager;
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getUsage() {
        return "/clan join <clan>";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage("§cUso corretto: " + getUsage());
            return;
        }

        if (clanManager.getClanByPlayer(player.getUniqueId()) != null) {
            player.sendMessage("§cSei già in un clan.");
            return;
        }

        Clan clan = clanManager.getClanByName(args[0]);
        if (clan == null) {
            player.sendMessage("§cIl clan §e" + args[0] + " §cnon esiste.");
            return;
        }

        if (!inviteManager.hasValidInvite(player.getUniqueId(), clan.getName())) {
            player.sendMessage("§cNon hai un invito valido da questo clan (forse è scaduto).");
            return;
        }

        clanManager.addPlayerToClan(clan, player.getUniqueId(), player.getName());
        inviteManager.clearInvites(player.getUniqueId());

        player.sendMessage("§aSei entrato nel clan §e" + clan.getName()
                + " §a[§e" + clan.getTag() + "§a]!");
        clan.broadcastMessage("§e" + player.getName() + " §asi è unito al clan!");
    }
}