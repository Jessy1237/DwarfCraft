/*
 * Copyright (c) 2023.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.jessy1237.dwarfcraft.data.DwarfReader;
import com.jessy1237.dwarfcraft.models.DwarfPlayer;

public class DwarfManager 
{
    private final DwarfCraft plugin;
    private HashMap<UUID, DwarfPlayer> dwarves = new HashMap<>();

    public DwarfManager( DwarfCraft plugin ) {
        this.plugin = plugin;
    }

    public void init() {
        new DwarfReader( plugin, this );
    }

    public void addDwarf(UUID uuid, DwarfPlayer dwarf) {
        dwarves.put( uuid, dwarf );
    }

    public void addDwarf(DwarfPlayer dwarf) {
        dwarves.put( dwarf.getPlayer().getUniqueId(), dwarf );
    }

    public DwarfPlayer getDwarf(Player player)
    {
        UUID uuid = player.getUniqueId();
        return dwarves.get( uuid );
    }

    public DwarfPlayer getDwarf(UUID dwarf)
    {
        return dwarves.get( dwarf );
    }

    public HashMap<UUID, DwarfPlayer> getDwarves()
    {
        return dwarves;
    }
   
}
