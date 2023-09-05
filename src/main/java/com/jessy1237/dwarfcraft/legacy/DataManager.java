/*
 * Copyright (c) 2023.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft.legacy;

import java.util.*;
import java.util.logging.Level;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

import net.citizensnpcs.api.npc.NPC;

import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.models.*;

public class DataManager
{
    protected List<DwarfPlayer> dwarves = new ArrayList<>();
    private HashMap<Integer, DwarfVehicle> vehicleMap = new HashMap<>();
    public HashMap<Integer, DwarfTrainer> trainerList = new HashMap<>();
    private final DwarfCraft plugin;
    private final SQLiteReader dbReader;

    public DataManager( DwarfCraft plugin )
    {
        this.plugin = plugin;
        this.dbReader = new SQLiteReader(plugin, plugin.getConfigManager());
    }

    public void dbInitialize()
    {
        dbReader.dbInitialize();
    }

    public void dbFinalize()
    {
        dbReader.dbFinalize();
    }

    @Deprecated
    public boolean checkDwarfData( DwarfPlayer player )
    {
        return dbReader.checkDwarfData( player, player.getUuid() );
    }

    // @Deprecated
    // public boolean saveDwarfData( DwarfPlayer dwarfPlayer, DwarfSkill[] skills )
    // {
    //     return dbWrapper.saveDwarfData( dwarfPlayer, skills );
    // }

    public void addVehicle( DwarfVehicle v )
    {
        vehicleMap.put( v.getVehicle().getEntityId(), v );
    }

    public boolean checkTrainersInChunk( Chunk chunk )
    {
        for ( Map.Entry<Integer, DwarfTrainer> pairs : trainerList.entrySet() )
        {
            DwarfTrainer d = ( pairs.getValue() );
            if ( Math.abs( chunk.getX() - d.getLocation().getBlock().getChunk().getX() ) > 1 )
            {
                continue;
            }
            if ( Math.abs( chunk.getZ() - d.getLocation().getBlock().getChunk().getZ() ) > 1 )
            {
                continue;
            }
            return true;
        }
        return false;
    }

    /**
     * Finds a DwarfPlayer from the server's static list based on player's name
     * 
     * @param player
     * @return DwarfPlayer or null
     */
    @Deprecated
    public DwarfPlayer find( Player player )
    {
        for ( DwarfPlayer d : dwarves )
        {
            if ( d != null )
            {
                if ( d.getPlayer() != null )
                {
                    if ( d.getPlayer().getUniqueId().equals( player.getUniqueId() ) )
                    {
                        d.setPlayer( player );
                        return d;
                    }
                }
            }
        }
        return null;
    }

    @Deprecated
    public DwarfPlayer findOffline( UUID uuid )
    {
        DwarfPlayer dCPlayer = new DwarfPlayer(plugin, uuid);
        if ( dbReader.checkDwarfData( dCPlayer, uuid ) )
            return dCPlayer;
        else
        {
            // No DwarfPlayer or data found
            return null;
        }
    }

    public DwarfTrainer getTrainer( NPC npc )
    {
        if ( !npc.hasTrait( DwarfTrainerTrait.class ) ) return null;
        for ( Iterator<Map.Entry<Integer, DwarfTrainer>> i = trainerList.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry<Integer, DwarfTrainer> pairs = i.next();
            DwarfTrainer trainer = ( pairs.getValue() );
            if ( trainer.getEntity().getId() == npc.getId() )
                return trainer;
        }
        return null;
    }

    public boolean isTrainer( Entity entity )
    {
        for ( Iterator<Map.Entry<Integer, DwarfTrainer>> i = trainerList.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry<Integer, DwarfTrainer> pairs = i.next();
            DwarfTrainer trainer = ( pairs.getValue() );
            if ( trainer.getEntity().getId() == entity.getEntityId() )
                return true;
        }
        return false;
    }

    @SuppressWarnings( "unlikely-arg-type" )
    public DwarfTrainer getTrainer( String str )
    {
        return ( trainerList.get( str ) ); // can return null
    }

    public DwarfVehicle getVehicle( Vehicle v )
    {
        for ( Integer i : vehicleMap.keySet() )
        {
            if ( i == v.getEntityId() )
            {
                return vehicleMap.get( i );
            }
        }
        return null;
    }

    public DwarfTrainer getTrainerByName( String name )
    {
        for ( DwarfTrainer trainer : trainerList.values() )
        {
            if ( trainer.getName().equalsIgnoreCase( name ) )
            {
                return trainer;
            }
        }
        return null;
    }

    public void removeVehicle( Vehicle v )
    {
        int id = -1;
        for ( Integer i : vehicleMap.keySet() )
        {
            if ( i == v.getEntityId() )
            {
                id = i;
                plugin.getUtil().debugLog( 5, Level.FINE, "Removed DwarfVehicle from vehicleList" );
            }
        }
        if ( id != -1 )
            vehicleMap.remove( id );
    }
}
