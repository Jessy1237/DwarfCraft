/*
 * Copyright (c) 2023.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft.data;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.models.DwarfPlayer;

public class DwarfManager 
{
    private final DwarfCraft plugin;
    private HashMap<UUID, DwarfPlayer> dwarves = new HashMap<>();
    private DwarfReader reader;

    public DwarfManager( DwarfCraft plugin ) {
        this.plugin = plugin;
        reader = new DwarfReader( this.plugin, this );
    }

    public void init() {
        dwarves = reader.parseDwarves();
        plugin.getUtil().consoleLog( "Loaded " + ChatColor.AQUA + dwarves.values().size() + ChatColor.WHITE + " Players(s)" );
    }

    public void createDwarf( OfflinePlayer player ) {
        DwarfPlayer dwarf = reader.createDwarf( player );
        addDwarf(dwarf);
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

    public boolean saveDwarf( DwarfPlayer player ) {
        return reader.saveDwarf(player);
    }

    public HashMap<UUID, DwarfPlayer> getDwarves()
    {
        return dwarves;
    }
   
}
