/*
 * Copyright (c) 2018.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft.events;

import java.util.HashMap;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.jessy1237.dwarfcraft.models.DwarfRace;

public class DwarfLoadRacesEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private final HashMap<String, DwarfRace> races;

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    /**
     * The event for when DwarfCraft loads all the races from the config file but before it is set into the plugins memory. So you can inject, remove or edit races into the plugin via this event
     * 
     * @param races The races that are going to be loaded into the DwarfCraft memory
     */
    public DwarfLoadRacesEvent( HashMap<String, DwarfRace> races )
    {
        this.races = races;
    }

    /**
     * Gets the races ArrayList
     * 
     * @return DwarfPlayer
     */
    @SuppressWarnings( "unchecked" )
    public HashMap<String, DwarfRace> getRaces()
    {
        return ( HashMap<String, DwarfRace> ) races.clone();
    }
}
