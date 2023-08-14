package com.jessy1237.dwarfcraft;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import org.bukkit.ChatColor;

import com.jessy1237.dwarfcraft.data.RaceReader;
import com.jessy1237.dwarfcraft.events.DwarfLoadRacesEvent;
import com.jessy1237.dwarfcraft.models.DwarfRace;

public
class RaceManager
{
    private final DwarfCraft plugin;
    private HashMap<String, DwarfRace> races = new HashMap<>();

    public RaceManager( DwarfCraft plugin ) {
        this.plugin = plugin;
    }

    @SuppressWarnings( "unchecked" )
    public void init() {
        new RaceReader( plugin, this );
        DwarfLoadRacesEvent e = new DwarfLoadRacesEvent( ( HashMap<String, DwarfRace> ) races.clone() );
        plugin.getServer().getPluginManager().callEvent( e );
        races = getAllRaces();
        plugin.getUtil().consoleLog( "Loaded " + ChatColor.AQUA + races.values().size() + ChatColor.WHITE + " Races(s)" );
    }

    public void addRace( DwarfRace race ) {
        races.put( race.getId().toLowerCase(), race );
    }

    public DwarfRace getRace( String race_id )
    {
        if ( race_id.isEmpty() )
            return new DwarfRace("", "");
        else
            return races.get( race_id.toLowerCase() );
    }

    public HashMap<String, DwarfRace> getAllRaces()
    {
        HashMap<String, DwarfRace> newRacesArray = new HashMap<>();
        for ( DwarfRace r : races.values() )
        {
            if ( newRacesArray.containsKey( r.getId() ) ) continue;
            newRacesArray.put( r.getId(), r.clone() );
        }
        return newRacesArray;
    }

    public DwarfRace getDefaultRace()
    {
        String defaultRace = plugin.getConfigManager().defaultRace;
        if ( defaultRace.isEmpty() )
            return new DwarfRace("", "");
        else
            return getRace( defaultRace );
    }

    public boolean raceExists( String race_id ) {
        return races.containsKey( race_id );
    }

    public int count() {
        return races.size();
    }
}
