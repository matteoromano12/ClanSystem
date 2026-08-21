package com.discepolo.clansystem.command;

import org.bukkit.entity.Player;

import java.util.List;

public interface SubCommand {

    String getName();

    String getUsage();

    void execute(Player player, String[] args);

    default List<String> tabComplete(Player player, String[] args) {
        return List.of();
    }

    default boolean isHidden() {
        return false;
    }
}