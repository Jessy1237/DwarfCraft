package com.jessy1237.dwarfcraft;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

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
        createRaceFiles();
        new RaceReader( plugin, this );
        DwarfLoadRacesEvent e = new DwarfLoadRacesEvent( ( HashMap<String, DwarfRace> ) races.clone() );
        plugin.getServer().getPluginManager().callEvent( e );
        races = getAllRaces();
        plugin.getUtil().consoleLog( "Loaded " + ChatColor.AQUA + races.values().size() + ChatColor.WHITE + " Races(s)" );
    }

    private
    void createRaceFiles() {
        File root = new File( plugin.getDataFolder().getAbsolutePath() );

        if ( !root.exists() )
        {
            if ( !root.mkdirs() )
            {
                return;
            }
        }

        for ( String file_name : Registration.getRaceFiles() )
        {
            String path = "data/dwarfcraft/races/" + file_name;
            File destFile = new File( plugin.getDataFolder() + File.separator + path );
            InputStream source = plugin.getResource( path );
            if ( source != null && file_name.endsWith( ".json" ) && !destFile.exists() )
            {
                plugin.saveResource( path, true );
                plugin.getUtil().consoleLog( "Writing data file: " + ChatColor.AQUA + path );
            }
        }

        File customDir = new File( plugin.getDataFolder().getAbsolutePath() + "/data/custom/races/" );
        if ( !customDir.exists() ) customDir.mkdirs();
    }

    public void addRace( DwarfRace race ) {
        races.put( race.getId().toLowerCase(), race );
    }

    public
    DwarfRace getRace( String race_id )
    {
        if ( race_id.isEmpty() )
            return new DwarfRace("", "");
        else
            return races.get( race_id.toLowerCase() );
    }

    public
    HashMap<String, DwarfRace> getAllRaces()
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
