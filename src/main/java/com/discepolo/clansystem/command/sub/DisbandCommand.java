package com.discepolo.clansystem.command.sub;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClanMember;
import com.discepolo.clansystem.clan.ClanRole;
import com.discepolo.clansystem.command.SubCommand;
import com.discepolo.clansystem.manager.ClanManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DisbandCommand implements SubCommand {

    private static final long CONFIRM_TIMEOUT_MS = 30_000;

    private final ClanManager clanManager;

    private final Map<UUID, Long> pendingConfirmations = new HashMap<>();

    public DisbandCommand(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    @Override
    public String getName() {
        return "disband";
    }

    @Override
    public String getUsage() {
        return "/clan disband";
    }

    @Override
    public void execute(Player player, String[] args) {
        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());
        if (clan == null) {
            player.sendMessage("§cNon sei in nessun clan.");
            return;
        }

        ClanMember member = clan.getMember(player.getUniqueId());
        if (member.getRole() != ClanRole.LEADER) {
            player.sendMessage("§cSolo il leader può disbandare il clan.");
            return;
        }

        Long firstAsk = pendingConfirmations.get(player.getUniqueId());
        long now = System.currentTimeMillis();

        if (firstAsk == null || now - firstAsk > CONFIRM_TIMEOUT_MS) {
            pendingConfirmations.put(player.getUniqueId(), now);
            player.sendMessage("§c⚠ Stai per disbandare §e" + clan.getName()
                    + "§c! L'azione è irreversibile.");
            player.sendMessage("§cRiscrivi §e/clan disband §centro 30 secondi per confermare.");
            return;
        }

        pendingConfirmations.remove(player.getUniqueId());
        String name = clan.getName();
        clan.broadcastMessage("§7Il clan §e" + name + " §7è stato disbandato.");
        clanManager.disbandClan(clan);
    }
}