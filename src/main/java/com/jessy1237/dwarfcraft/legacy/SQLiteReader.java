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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.data.ConfigManager;
import com.jessy1237.dwarfcraft.models.DwarfPlayer;
import com.jessy1237.dwarfcraft.models.DwarfSkill;

@Deprecated
class SQLiteReader
{
    private final ConfigManager configManager;
    private final DwarfCraft plugin;
    private Connection mDBCon;

    SQLiteReader( DwarfCraft plugin, ConfigManager cm )
    {
        this.plugin = plugin;
        this.configManager = cm;
    }

    public void dbInitialize()
    {
        try
        {
            Class.forName( "org.sqlite.JDBC" );
            mDBCon = DriverManager.getConnection( "jdbc:sqlite:" + configManager.getDatabasePath() );
        } catch (Exception e) {
            e.printStackTrace();
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

    // public boolean saveDwarfData( DwarfPlayer dwarfPlayer, DwarfSkill[] skills )
    // {
    //     try
    //     {
    //         PreparedStatement prep = mDBCon.prepareStatement( "UPDATE players SET race=? WHERE uuid=?;" );
    //         prep.setString( 1, dwarfPlayer.getRace().getId() );
    //         prep.setString( 2, dwarfPlayer.getPlayer().getUniqueId().toString() );
    //         prep.execute();
    //         prep.close();

    //         prep = mDBCon.prepareStatement( "UPDATE players SET raceMaster=? WHERE uuid=?;" );
    //         prep.setBoolean( 1, dwarfPlayer.isRaceMaster() );
    //         prep.setString( 2, dwarfPlayer.getPlayer().getUniqueId().toString() );
    //         prep.execute();
    //         prep.close();

    //         prep = mDBCon.prepareStatement( "REPLACE INTO skills(player, id, level, " + "deposit1, deposit2, deposit3) " + "values(?,?,?,?,?,?);" );

    //         int id = getPlayerID( dwarfPlayer.getPlayer().getUniqueId() );
    //         for ( DwarfSkill skill : skills )
    //         {
    //             prep.setInt( 1, id );
    //             prep.setString( 2, skill.getId() );
    //             prep.setInt( 3, skill.getLevel() );
    //             prep.setInt( 4, skill.getDeposit( 1 ) );
    //             prep.setInt( 5, skill.getDeposit( 2 ) );
    //             prep.setInt( 6, skill.getDeposit( 3 ) );
    //             prep.addBatch();
    //         }
    //         prep.executeBatch();
    //         prep.close();
    //         return true;
    //     }
    //     catch ( Exception e )
    //     {
    //         e.printStackTrace();
    //         return false;
    //     }
    // }
}
