/*
 * Copyright (c) 2018.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.Jessy1237.DwarfCraft.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.models.DwarfCommand;

public class CommandHelp extends DwarfCommand implements TabCompleter
{
    public CommandHelp( String name, DwarfCraft plugin )
    {
        super( name, plugin );
    }

    @Override
    public boolean execute( CommandSender sender, String commandLabel, String[] args )
    {
        plugin.getUtil().debugLog( 1, Level.FINE, "Started command 'dchelp'" );

        Set<String> keys = plugin.getCommandManager().getAllCommands().keySet();
        sender.sendMessage( "Available Commands: " + String.join( ", ", keys ) );
        return true;
    }

    @Override
    public List<String> onTabComplete( CommandSender commandSender, Command command, String s, String[] args )
    {
        if ( !command.getName().equalsIgnoreCase( "dwarfcraft" ) || !plugin.isEnabled() )
            return null;

        Set<String> keys = plugin.getCommandManager().getAllCommands().keySet();
        final List<String> completions = new ArrayList<>(keys);
        List<String> matches = new ArrayList<>();

        StringUtil.copyPartialMatches( args[0], completions, matches );
        Collections.sort( matches );

        return matches;
    }
}
