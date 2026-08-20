package com.discepolo.clansystem.command.sub;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClaimedChunk;
import com.discepolo.clansystem.clan.ClanMember;
import com.discepolo.clansystem.clan.ClanRole;
import com.discepolo.clansystem.command.SubCommand;
import com.discepolo.clansystem.manager.ClaimManager;
import com.discepolo.clansystem.manager.ClanManager;
import org.bukkit.entity.Player;

public class SetHomeCommand implements SubCommand {

    private final ClanManager clanManager;
    private final ClaimManager claimManager;

    public SetHomeCommand(ClanManager clanManager, ClaimManager claimManager) {
        this.clanManager = clanManager;
        this.claimManager = claimManager;
    }

    @Override
    public String getName() { return "sethome"; }

    @Override
    public String getUsage() { return "/clan sethome - Imposta una base per il clan"; }

    @Override
    public void execute(Player player, String[] args) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cNon sei in nessun clan.");
            return;
        }

        ClanMember member = clan.getMember(player.getUniqueId());
        if (member.getRole() != ClanRole.LEADER) {
            player.sendMessage("§cSolo il leader può impostare la home del clan.");
            return;
        }

        ClaimedChunk chunk = ClaimedChunk.fromChunk(player.getLocation().getChunk());
        Clan owner = claimManager.getOwner(chunk);
        if (owner != clan) {
            player.sendMessage("§cPuoi impostare la home solo in un territorio del tuo clan.");
            return;
        }

        clan.setHome(player.getLocation());
        player.sendMessage("§aHome del clan impostata qui!");
        clan.broadcastMessage("§7La home del clan è stata aggiornata da §e" + player.getName() + "§7.");
    }
}