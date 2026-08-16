package com.discepolo.clansystem.command.sub;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClanMember;
import com.discepolo.clansystem.clan.ClanRole;
import com.discepolo.clansystem.command.SubCommand;
import com.discepolo.clansystem.manager.ClanManager;
import com.discepolo.clansystem.manager.InviteManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class InviteCommand implements SubCommand {

    private final ClanManager clanManager;
    private final InviteManager inviteManager;

    public InviteCommand(ClanManager clanManager, InviteManager inviteManager) {
        this.clanManager = clanManager;
        this.inviteManager = inviteManager;
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getUsage() {
        return "/clan invite <player>";
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

        ClanMember member = clan.getMember(player.getUniqueId());
        if (!member.getRole().isAtLeast(ClanRole.OFFICER)) {
            player.sendMessage("§cSolo officer e leader possono invitare.");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage("§cGiocatore §e" + args[0] + " §cnon trovato (deve essere online).");
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§cNon puoi invitare te stesso.");
            return;
        }

        if (clanManager.getClanByPlayer(target.getUniqueId()) != null) {
            player.sendMessage("§c" + target.getName() + " è già in un clan.");
            return;
        }

        inviteManager.addInvite(target.getUniqueId(), clan.getName());

        player.sendMessage("§aInvito inviato a §e" + target.getName() + "§a.");
        target.sendMessage("§aSei stato invitato nel clan §e" + clan.getName()
                + " §a[§e" + clan.getTag() + "§a]!");
        target.sendMessage("§aScrivi §e/clan join " + clan.getName()
                + " §aentro 60 secondi per accettare.");

        target.sendMessage(
                Component.text("[O licca qui per accettare]", NamedTextColor.YELLOW, TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/clan join " + clan.getName()))
                        .hoverEvent(HoverEvent.showText(
                                Component.text("Entra in " + clan.getName(), NamedTextColor.GRAY)))
        );
    }
}