package com.discepolo.clansystem.placeholder;

import com.discepolo.clansystem.ClanSystem;
import com.discepolo.clansystem.clan.Clan;
import com.discepolo.clansystem.clan.ClanMember;
import com.discepolo.clansystem.manager.ClanManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class ClanPlaceholders extends PlaceholderExpansion {

    private final ClanSystem plugin;
    private final ClanManager clanManager;

    public ClanPlaceholders(ClanSystem plugin, ClanManager clanManager) {
        this.plugin = plugin;
        this.clanManager = clanManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "clans";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        Clan clan = clanManager.getClanByPlayer(player.getUniqueId());

        return switch (params.toLowerCase()) {
            case "player_clan" -> clan == null ? "Nessuno" : clan.getName();
            case "player_tag"  -> clan == null ? "" : clan.getTag();
            case "player_role" -> {
                if (clan == null) yield "";
                ClanMember member = clan.getMember(player.getUniqueId());
                yield member == null ? "" : member.getRole().getDisplayName();
            }
            case "clan_members_online" -> clan == null ? "0"
                    : String.valueOf(clan.getOnlineMembers().size());
            default -> null;
        };
    }
}