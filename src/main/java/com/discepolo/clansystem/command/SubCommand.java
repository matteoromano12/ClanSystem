package com.discepolo.clansystem.command;

import org.bukkit.entity.Player;

public interface SubCommand {

    String getName();

    String getUsage();

    void execute(Player player, String[] args);
}