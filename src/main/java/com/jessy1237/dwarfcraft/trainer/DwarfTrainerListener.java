package com.jessy1237.dwarfcraft.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.jessy1237.dwarfcraft.DwarfCraft;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.CitizensEnableEvent;
import net.citizensnpcs.api.npc.NPCRegistry;

public class DwarfTrainerListener implements Listener
{
    private DwarfCraft plugin;
    
    public DwarfTrainerListener( DwarfCraft plugin )
    {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCitizensEnable(CitizensEnableEvent ev) {
        plugin.getUtil().consoleLog( "Success! Hooked into Citizens!", ChatColor.GREEN );
        NPCRegistry npc_registry = CitizensAPI.getNPCRegistry();
    }
}
