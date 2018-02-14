package com.Jessy1237.DwarfCraft.commands;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.Jessy1237.DwarfCraft.CommandException;
import com.Jessy1237.DwarfCraft.CommandInformation;
import com.Jessy1237.DwarfCraft.CommandParser;
import com.Jessy1237.DwarfCraft.DwarfCraft;

public class CommandDebug extends Command implements TabCompleter
{
    private final DwarfCraft plugin;

    public CommandDebug( final DwarfCraft plugin )
    {
        super( "Debug" );
        this.plugin = plugin;
    }

    @Override
    public boolean execute( CommandSender sender, String commandLabel, String[] args )
    {
        if ( args.length == 0 )
        {
            plugin.getOut().sendMessage( sender, CommandInformation.Usage.DEBUG.getUsage() );
        }
        else if ( args[0].equalsIgnoreCase( "?" ) )
        {
            plugin.getOut().sendMessage( sender, CommandInformation.Desc.DEBUG.getDesc() );
        }
        else
        {
            try
            {
                CommandParser parser = new CommandParser( plugin, sender, args );
                List<Object> desiredArguments = new ArrayList<Object>();
                List<Object> outputList = null;

                if ( DwarfCraft.debugMessagesThreshold < 1 )
                    System.out.println( "DC1: started command 'debug'" );

                Integer i = 0;
                desiredArguments.add( i );
                outputList = parser.parse( desiredArguments, false );

                DwarfCraft.debugMessagesThreshold = ( Integer ) outputList.get( 0 );
                System.out.println( "*** DC DEBUG LEVEL CHANGED TO " + DwarfCraft.debugMessagesThreshold + " ***" );
                if ( sender instanceof Player )
                    plugin.getOut().sendMessage( sender, "Debug messaging level set to " + DwarfCraft.debugMessagesThreshold );
            }
            catch ( CommandException e )
            {
                e.describe( sender );
                sender.sendMessage( CommandInformation.Usage.DEBUG.getUsage() );
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete( CommandSender commandSender, Command command, String s, String[] args )
    {
        if ( !command.getName().equalsIgnoreCase( "dwarfcraft" ) )
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
}