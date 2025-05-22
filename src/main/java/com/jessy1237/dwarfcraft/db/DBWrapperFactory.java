package com.jessy1237.dwarfcraft.data;

import com.jessy1237.dwarfcraft.ConfigManager;
import com.jessy1237.dwarfcraft.DwarfCraft;

public class DBWrapperFactory
{

    public static DBWrapper createWrapper( DwarfCraft plugin, String type )
    {
        DBWrapper dbWrapper = null;

        if ( type.equalsIgnoreCase( "sqlite" ) )
        {
            dbWrapper = new DBWrapperSQLite( plugin, plugin.getConfigManager() );
        }
        else if ( type.equalsIgnoreCase( "MySQL" ) )
        {
            dbWrapper = new DBWrapperMySQL( plugin, plugin.getConfigManager() );
        }

        return dbWrapper;
    }

}
