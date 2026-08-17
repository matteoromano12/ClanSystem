package com.discepolo.clansystem.command.sub;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClanMember;
import com.discepolo.clansystem.clan.ClanRole;
import com.discepolo.clansystem.command.SubCommand;
import com.discepolo.clansystem.manager.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class KickCommand implements SubCommand {

    private final ClanManager clanManager;

    public KickCommand(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    @Override
    public String getName() {
        return "kick";
    }

    @Override
    public String getUsage() {
        return "/clan kick <player>";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage("§cUso corretto: " + getUsage());
            return;
        }

        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cNon sei in nessun clan.");
            return;
        }

        ClanMember target = clan.getMemberByName(args[0]);
        if (target == null) {
            player.sendMessage("§c" + args[0] + " non è un membro del tuo clan.");
            return;
        }

        if (target.getUuid().equals(player.getUniqueId())) {
            player.sendMessage("§cNon puoi espellere te stesso. Usa /clan leave.");
            return;
        }

        ClanMember self = clan.getMember(player.getUniqueId());
        if (self.getRole().getWeight() <= target.getRole().getWeight()) {
            player.sendMessage("§cNon puoi espellere §e" + target.getLastKnownName()
                    + "§c: il suo rango (§e" + target.getRole().getDisplayName()
                    + "§c) non è inferiore al tuo.");
            return;
        }

        String targetName = target.getLastKnownName();
        clanManager.removePlayerFromClan(clan, target.getUuid());
        clan.broadcastMessage("§e" + targetName + " §cè stato espulso dal clan da §e"
                + player.getName() + "§c.");

        Player targetPlayer = Bukkit.getPlayer(target.getUuid());
        if (targetPlayer != null) {
            targetPlayer.sendMessage("§cSei stato espulso dal clan §e" + clan.getName() + "§c.");
        }
    }
}