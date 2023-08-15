/*
 * Copyright (c) 2023.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.jessy1237.dwarfcraft.ConfigManager;
import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.models.DwarfCommand;

public class CommandDebug extends DwarfCommand implements TabCompleter
{
    public CommandDebug( String name, DwarfCraft plugin )
    {
        super( name, plugin );
        setDescription("Sets the debug message threshold in console, from -10(everthing) to +10(critical only).");
    }

    @Override
    public boolean execute( CommandSender sender, String commandLabel, String[] args )
    {
        if ( args.length == 0 )
        {
            plugin.getOut().sendMessage( sender, getUsage() );
        }
        else if ( args[0].equalsIgnoreCase( "?" ) )
        {
            plugin.getOut().sendMessage( sender, description );
        }
        else
        {
            try
            {
                CommandParser parser = new CommandParser( plugin, sender, args );
                List<Object> desiredArguments = new ArrayList<Object>();
                List<Object> outputList = null;

                plugin.getUtil().debugLog( 1, Level.FINE, "Started command 'debug'" );

                Integer i = 0;
                desiredArguments.add( i );
                outputList = parser.parse( desiredArguments, false );

                if (outputList.size() < 1) {
                    sender.sendMessage( getUsage() );
                    return false;
                }

                plugin.getConfig().set( "Debug Level", outputList.get( 0 ) );
                plugin.saveConfig();
                plugin.debugMessagesThreshold = (Integer) plugin.getConfig().get("Debug Level");

                plugin.getUtil().consoleLog( "*** DC DEBUG LEVEL CHANGED TO " + plugin.debugMessagesThreshold + " ***", Level.FINE );
                if ( sender instanceof Player )
                    plugin.getOut().sendMessage( sender, "Debug messaging level set to " + plugin.debugMessagesThreshold );
            }
            catch ( CommandException e )
            {
                e.describe( sender );
                sender.sendMessage( getUsage() );
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete( CommandSender commandSender, Command command, String s, String[] args )
    {
        if ( !command.getName().equalsIgnoreCase( "dwarfcraft" ) || !plugin.isEnabled() )
            return null;

        if ( args[0].equalsIgnoreCase( "debug" ) && args.length >= 2 )
        {
            final List<String> completions = new ArrayList<>( Arrays.asList( "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" ) );
            List<String> matches = new ArrayList<>();

            if ( args[1].equalsIgnoreCase( "" ) )
            {
                matches.addAll( completions );
                return matches;
            }
        }

        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "/dc debug <debug level>\nExample: /dc debug 2 - sets the console debug printing threshold to 2";
    }

    @Override
    public boolean isOp() {
        return true;
    }
}
