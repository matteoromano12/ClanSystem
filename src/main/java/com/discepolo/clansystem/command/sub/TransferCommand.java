package com.discepolo.clansystem.command.sub;

import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClanMember;
import com.discepolo.clansystem.clan.ClanRole;
import com.discepolo.clansystem.command.SubCommand;
import com.discepolo.clansystem.manager.ClanManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TransferCommand implements SubCommand {

    private static final long CONFIRM_TIMEOUT_MS = 30_000;

    private final ClanManager clanManager;

    private final Map<UUID, PendingTransfer> pending = new HashMap<>();

    private record PendingTransfer(UUID target, long timestamp) {}

    public TransferCommand(ClanManager clanManager) {
        this.clanManager = clanManager;
    }

    @Override
    public String getName() {
        return "transfer";
    }

    @Override
    public String getUsage() {
        return "/clan transfer <player>";
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

        ClanMember self = clan.getMember(player.getUniqueId());
        if (self.getRole() != ClanRole.LEADER) {
            player.sendMessage("§cSolo il leader può trasferire la leadership.");
            return;
        }

        ClanMember target = clan.getMemberByName(args[0]);
        if (target == null) {
            player.sendMessage("§c" + args[0] + " non è un membro del tuo clan.");
            return;
        }

        if (target.getUuid().equals(player.getUniqueId())) {
            player.sendMessage("§cSei già il leader.");
            return;
        }

        PendingTransfer request = pending.get(player.getUniqueId());
        long now = System.currentTimeMillis();

        boolean confirmed = request != null
                && request.target().equals(target.getUuid())
                && now - request.timestamp() <= CONFIRM_TIMEOUT_MS;

        if (!confirmed) {
            pending.put(player.getUniqueId(), new PendingTransfer(target.getUuid(), now));
            player.sendMessage("§c⚠ Stai per trasferire la leadership di §e" + clan.getName()
                    + " §ca §e" + target.getLastKnownName() + "§c!");
            player.sendMessage("§cDiventerai Officer. Riscrivi §e/clan transfer "
                    + target.getLastKnownName() + " §centro 30 secondi per confermare.");
            return;
        }

        pending.remove(player.getUniqueId());
        self.setRole(ClanRole.OFFICER);
        target.setRole(ClanRole.LEADER);

        String targetName = target.getLastKnownName();
        clan.broadcastMessage("§6Il clan è stato trasferito da §e" + player.getName()
                + " §6a §e" + targetName + "§6.");
        player.sendMessage("§aHai trasferito il clan a §e" + targetName + "§a. Ora sei Officer.");

        Player targetPlayer = Bukkit.getPlayer(target.getUuid());
        if (targetPlayer != null) {
            targetPlayer.sendMessage("§6" + player.getName()
                    + " §eti ha trasferito il clan. Ora sei il §6Leader§e!");
        }
    }
}