/*
 * Copyright (c) 2018.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import com.jessy1237.dwarfcraft.ConfigManager;
import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.models.DwarfPlayer;
import com.jessy1237.dwarfcraft.models.DwarfSkill;
import com.jessy1237.dwarfcraft.models.DwarfTrainerTrait;

import net.citizensnpcs.api.npc.AbstractNPC;

class DBWrapperSQLite implements DBWrapper
{
    private final ConfigManager configManager;
    private final DwarfCraft plugin;
    private Connection mDBCon;

    DBWrapperSQLite( DwarfCraft plugin, ConfigManager cm )
    {
        this.plugin = plugin;
        this.configManager = cm;
    }

    @SuppressWarnings( "deprecation" )
    public void dbInitialize()
    {
        try
        {
            Class.forName( "org.sqlite.JDBC" );
            mDBCon = DriverManager.getConnection( "jdbc:sqlite:" + configManager.getDatabasePath() );
            Statement statement = mDBCon.createStatement();
            ResultSet rs = statement.executeQuery( "select * from sqlite_master WHERE name = 'players';" );
            if ( !rs.next() )
            {
                buildDB();
            }

            // check for update to skill deposits
            try
            {
                rs = statement.executeQuery( "SELECT deposit1 FROM skills;" );
            }
            catch ( SQLException ex )
            {
                statement.executeUpdate( "ALTER TABLE skills ADD COLUMN deposit1 NUMERIC DEFAULT 0;" );
                statement.executeUpdate( "ALTER TABLE skills ADD COLUMN deposit2 NUMERIC DEFAULT 0;" );
                statement.executeUpdate( "ALTER TABLE skills ADD COLUMN deposit3 NUMERIC DEFAULT 0;" );
            }

            // Adds the uuid arg to the player table
            try
            {
                rs = statement.executeQuery( "select uuid from players" );
            }
            catch ( Exception e )
            {
                plugin.getLogger().log( Level.INFO, "Converting Player DB (may lag a little wait for completion message)." );
                mDBCon.setAutoCommit( false );
                HashMap<UUID, String> dcplayers = new HashMap<>();
                HashMap<UUID, Integer> ids = new HashMap<>();

                try
                {
                    PreparedStatement prep = mDBCon.prepareStatement( "SELECT * FROM players" );
                    rs = prep.executeQuery();

                    while ( rs.next() )
                    {
                        dcplayers.put( plugin.getServer().getOfflinePlayer( rs.getString( "name" ) ).getUniqueId(), rs.getString( "race" ) );
                        ids.put( plugin.getServer().getOfflinePlayer( rs.getString( "name" ) ).getUniqueId(), rs.getInt( "id" ) );
                    }

                }
                catch ( Exception e1 )
                {
                    e1.printStackTrace();
                }
                statement.executeUpdate( "DROP TABLE players" );
                statement.executeUpdate( "create table players ( id INTEGER PRIMARY KEY, uuid, race, raceMaster );" );
                for ( UUID uuid : dcplayers.keySet() )
                {
                    if ( uuid != null )
                    {
                        PreparedStatement prep = mDBCon.prepareStatement( "insert into players(id, uuid, race, raceMaster) values(?,?,?,?);" );
                        prep.setInt( 1, ids.get( uuid ) );
                        prep.setString( 2, uuid.toString() );
                        prep.setString( 3, dcplayers.get( uuid ) );
                        prep.setBoolean( 4, false );
                        prep.execute();
                        prep.close();
                    }
                }
                plugin.getLogger().log( Level.INFO, "Finished Converting the Players DB." );
            }

            // Adds raceMaster arg to the player table
            try
            {
                rs = statement.executeQuery( "select raceMaster from players" );
            }
            catch ( Exception e )
            {
                plugin.getLogger().log( Level.INFO, "Converting Player DB (may lag a little wait for completion message)." );
                mDBCon.setAutoCommit( false );
                HashMap<UUID, String> dcplayers = new HashMap<>();
                HashMap<UUID, Integer> ids = new HashMap<>();

                try
                {
                    PreparedStatement prep = mDBCon.prepareStatement( "SELECT * FROM players" );
                    rs = prep.executeQuery();

                    while ( rs.next() )
                    {
                        dcplayers.put( UUID.fromString( rs.getString( "uuid" ) ), rs.getString( "race" ) );
                        ids.put( UUID.fromString( rs.getString( "uuid" ) ), rs.getInt( "id" ) );
                    }

                }
                catch ( Exception e1 )
                {
                    e1.printStackTrace();
                }
                statement.executeUpdate( "DROP TABLE players" );
                statement.executeUpdate( "create table players ( id INTEGER PRIMARY KEY, uuid, race, raceMaster );" );
                for ( UUID uuid : dcplayers.keySet() )
                {
                    if ( uuid != null )
                    {
                        PreparedStatement prep = mDBCon.prepareStatement( "insert into players(id, uuid, race, raceMaster) values(?,?,?,?);" );
                        prep.setInt( 1, ids.get( uuid ) );
                        prep.setString( 2, uuid.toString() );
                        prep.setString( 3, dcplayers.get( uuid ) );
                        prep.setBoolean( 4, false );
                        prep.execute();
                        prep.close();
                    }
                }
                plugin.getLogger().log( Level.INFO, "Finished Converting the Players DB." );
            }

            try
            {
                rs = statement.executeQuery( "select * from sqlite_master WHERE name = 'trainers';" );
                if ( rs.next() )
                {
                    plugin.getLogger().log( Level.INFO, "Transferring Trainer DB to citizens  (may lag a little wait for completion message)." );

                    rs = statement.executeQuery( "select * from trainers;" );

                    while ( rs.next() )
                    {
                        AbstractNPC npc1;
                        if ( rs.getString( "type" ).equalsIgnoreCase( "PLAYER" ) )
                        {
                            npc1 = ( AbstractNPC ) plugin.getNPCRegistry().createNPC( EntityType.PLAYER, UUID.randomUUID(), Integer.parseInt( rs.getString( "uniqueId" ) ), rs.getString( "name" ) );
                        }
                        else
                        {
                            npc1 = ( AbstractNPC ) plugin.getNPCRegistry().createNPC( EntityType.valueOf( rs.getString( "type" ) ), UUID.randomUUID(), Integer.parseInt( rs.getString( "uniqueId" ) ), rs.getString( "name" ) );
                        }
                        npc1.spawn( new Location( plugin.getServer().getWorld( rs.getString( "world" ) ), rs.getDouble( "x" ), rs.getDouble( "y" ), rs.getDouble( "z" ), rs.getFloat( "yaw" ), rs.getFloat( "pitch" ) ) );
                        npc1.addTrait( new DwarfTrainerTrait( plugin, rs.getString( "skill" ), rs.getInt( "maxSkill" ), rs.getInt( "minSkill" ) ) );
                        npc1.setProtected( true );
                    }
                }
                statement.execute( "DROP TABLE trainers" );
                plugin.getLogger().log( Level.INFO, "Finished Transferring the Trainers DB." );
            }
            catch ( Exception e )
            {
                // NOOP
            }
            rs.close();
            mDBCon.setAutoCommit( true );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    private void buildDB()
    {
        try
        {
            Statement statement = mDBCon.createStatement();
            ResultSet rs = statement.executeQuery( "select * from sqlite_master WHERE name = 'players';" );
            if ( !rs.next() )
            {
                statement.executeUpdate( "create table players ( id INTEGER PRIMARY KEY, uuid, race, raceMaster );" );
            }
            rs.close();

            rs = statement.executeQuery( "select * from sqlite_master WHERE name = 'skills';" );
            if ( !rs.next() )
            {
                statement.executeUpdate( "CREATE TABLE 'skills' " + "  ( " + "    'player' INT, " + "    'id' TEXT, " + "    'level' INT DEFAULT 0, " + "    'deposit1' INT DEFAULT 0, " + "    'deposit2' INT DEFAULT 0, " + "    'deposit3' INT DEFAULT 0, " + "    PRIMARY KEY ('player','id') " + " "
                        + " );" );
            }
            rs.close();

        }
        catch ( SQLException e )
        {
            plugin.getLogger().log( Level.SEVERE, "DB not built successfully" );
            e.printStackTrace();
            plugin.getServer().getPluginManager().disablePlugin( plugin );
        }
    }

    public void dbFinalize()
    {
        try
        {
            mDBCon.close();
            mDBCon = null;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    public void createDwarfData( DwarfPlayer dCPlayer )
    {
        try
        {
            PreparedStatement prep = mDBCon.prepareStatement( "insert into players(uuid, race) values(?,?);" );
            prep.setString( 1, dCPlayer.getPlayer().getUniqueId().toString() );
            prep.setString( 2, plugin.getRaceManager().getDefaultRace().getId() );
            prep.execute();
            prep.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    public boolean checkDwarfData( DwarfPlayer player )
    {
        return checkDwarfData( player, player.getPlayer().getUniqueId() );
    }

    /**
     * Used for creating and populating a dwarf with a null(off line) player
     *
     * @param player
     * @param uuid
     */
    public boolean checkDwarfData( DwarfPlayer player, UUID uuid )
    {
        try
        {
            PreparedStatement prep = mDBCon.prepareStatement( "select * from players WHERE uuid = ?;" );
            prep.setString( 1, uuid.toString() );
            ResultSet rs = prep.executeQuery();

            if ( !rs.next() )
                return false;

            player.setRace( rs.getString( "race" ) );
            player.setRaceMaster( rs.getBoolean( "raceMaster" ) );

            int id = rs.getInt( "id" );
            rs.close();

            prep.close();
            prep = mDBCon.prepareStatement( "select id, level, deposit1, deposit2, deposit3 " + "from skills WHERE player = ?;" );
            prep.setInt( 1, id );
            rs = prep.executeQuery();

            while ( rs.next() )
            {
                String skillID = rs.getString( "id" );
                int level = rs.getInt( "level" );
                DwarfSkill skill = player.getSkill( skillID );
                if ( skill != null )
                {
                    skill.setLevel( level );
                    skill.setDeposit( rs.getInt( "deposit1" ), 1 );
                    skill.setDeposit( rs.getInt( "deposit2" ), 2 );
                    skill.setDeposit( rs.getInt( "deposit3" ), 3 );
                }
            }
            rs.close();
            prep.close();

            if ( !plugin.getDataManager().dwarves.contains( player ) )
                plugin.getDataManager().dwarves.add( player );

            return true;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return false;
        }
    }

    private int getPlayerID( UUID uuid )
    {
        try
        {
            PreparedStatement prep = mDBCon.prepareStatement( "select id from players WHERE uuid = ?;" );
            prep.setString( 1, uuid.toString() );
            ResultSet rs = prep.executeQuery();

            if ( !rs.next() )
                return -1;

            int id = rs.getInt( "id" );
            rs.close();
            prep.close();
            return id;
        }
        catch ( Exception e )
        {
            plugin.getLogger().log( Level.WARNING, "Failed to get player ID: " + uuid.toString() );
        }
        return -1;
    }

    public boolean saveDwarfData( DwarfPlayer dwarfPlayer, DwarfSkill[] skills )
    {
        try
        {
            PreparedStatement prep = mDBCon.prepareStatement( "UPDATE players SET race=? WHERE uuid=?;" );
            prep.setString( 1, dwarfPlayer.getRace().getId() );
            prep.setString( 2, dwarfPlayer.getPlayer().getUniqueId().toString() );
            prep.execute();
            prep.close();

            prep = mDBCon.prepareStatement( "UPDATE players SET raceMaster=? WHERE uuid=?;" );
            prep.setBoolean( 1, dwarfPlayer.isRaceMaster() );
            prep.setString( 2, dwarfPlayer.getPlayer().getUniqueId().toString() );
            prep.execute();
            prep.close();

            prep = mDBCon.prepareStatement( "REPLACE INTO skills(player, id, level, " + "deposit1, deposit2, deposit3) " + "values(?,?,?,?,?,?);" );

            int id = getPlayerID( dwarfPlayer.getPlayer().getUniqueId() );
            for ( DwarfSkill skill : skills )
            {
                prep.setInt( 1, id );
                prep.setString( 2, skill.getId() );
                prep.setInt( 3, skill.getLevel() );
                prep.setInt( 4, skill.getDeposit( 1 ) );
                prep.setInt( 5, skill.getDeposit( 2 ) );
                prep.setInt( 6, skill.getDeposit( 3 ) );
                prep.addBatch();
            }
            prep.executeBatch();
            prep.close();
            return true;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return false;
        }
    }
}
