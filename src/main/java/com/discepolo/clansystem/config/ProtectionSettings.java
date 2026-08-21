package com.discepolo.clansystem.config;

import org.bukkit.configuration.file.FileConfiguration;

public class ProtectionSettings {

    private final boolean build;
    private final boolean interact;
    private final boolean pvp;
    private final boolean mobSpawning;
    private final boolean explosions;
    private final boolean buckets;
    private final boolean entities;
    private final boolean fireSpread;
    private final boolean trampling;

    public ProtectionSettings(FileConfiguration config) {
        this.build       = config.getBoolean("protections.build", true);
        this.interact    = config.getBoolean("protections.interact", true);
        this.pvp         = config.getBoolean("protections.pvp", true);
        this.mobSpawning = config.getBoolean("protections.mob-spawning", true);
        this.explosions  = config.getBoolean("protections.explosions", true);
        this.buckets     = config.getBoolean("protections.buckets", true);
        this.entities    = config.getBoolean("protections.entities", true);
        this.fireSpread  = config.getBoolean("protections.fire-spread", true);
        this.trampling   = config.getBoolean("protections.trampling", true);
    }

    public boolean isBuild()       { return build; }
    public boolean isInteract()    { return interact; }
    public boolean isPvp()         { return pvp; }
    public boolean isMobSpawning() { return mobSpawning; }
    public boolean isExplosions()  { return explosions; }
    public boolean isBuckets()     { return buckets; }
    public boolean isEntities()    { return entities; }
    public boolean isFireSpread()  { return fireSpread; }
    public boolean isTrampling()   { return trampling; }
}