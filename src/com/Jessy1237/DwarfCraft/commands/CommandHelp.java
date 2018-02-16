package com.Jessy1237.DwarfCraft.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import com.Jessy1237.DwarfCraft.DwarfCraft;

public class CommandHelp extends Command implements TabCompleter
{
    // A list of all supported DwarfCraft commands
    private static final String[] COMMANDS = new String[] { "debug", "help", "tutorial", "info", "skillsheet", "skillinfo", "effectinfo", "race", "reload", "setskill", "create", "list" };

    @SuppressWarnings( "unused" )
    private final DwarfCraft plugin;

    public CommandHelp( final DwarfCraft plugin )
    {
        super( "dchelp" );
        this.plugin = plugin;
    }

    @Override
    public boolean execute( CommandSender sender, String commandLabel, String[] args )
    {
        if ( DwarfCraft.debugMessagesThreshold < 1 )
        {
            System.out.println( "DC1: started command 'dchelp'" );
        }

        sender.sendMessage( "Available Commands: " + String.join( ", ", COMMANDS ) );
        return true;
    }

    @Override
    public List<String> onTabComplete( CommandSender commandSender, Command command, String s, String[] args )
    {
        if ( !command.getName().equalsIgnoreCase( "dwarfcraft" ) )
            return null;

        final List<String> completions = new ArrayList<>( Arrays.asList( COMMANDS ) );
        List<String> matches = new ArrayList<>();

        StringUtil.copyPartialMatches( args[0], completions, matches );
        Collections.sort( matches );

        return matches;
    }
}
