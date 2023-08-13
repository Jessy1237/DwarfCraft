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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.md_5.bungee.api.ChatColor;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Messages;
import com.Jessy1237.DwarfCraft.commands.CommandException.Type;
import com.Jessy1237.DwarfCraft.models.DwarfCommand;
import com.Jessy1237.DwarfCraft.models.DwarfPlayer;
import com.Jessy1237.DwarfCraft.models.DwarfSkill;

public class CommandSkill extends DwarfCommand implements TabCompleter
{
    public CommandSkill( String name, DwarfCraft plugin )
    {
        super( name, plugin );
        setDescription("Displays a description of a dwarf's skill and training costs.");
    }

    @Override
    public boolean execute( CommandSender sender, String commandLabel, String[] args )
    {
        plugin.getUtil().debugLog( 1, Level.FINE, "Started command 'skill'" );

        if ( args.length == 0 || args == null )
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
                List<Object> outputList;

                DwarfPlayer dwarfPlayer = new DwarfPlayer( plugin, null );
                DwarfSkill skill = new DwarfSkill( plugin, "", null, new LinkedHashMap<>(),0, null, null, null, null, null );
                desiredArguments.add( dwarfPlayer );
                desiredArguments.add( skill );

                try
                {
                    outputList = parser.parse( desiredArguments, false );
                    if ( args.length > outputList.size() )
                        throw new CommandException( plugin, Type.TOOMANYARGS );

                    skill = ( DwarfSkill ) outputList.get( 1 );
                    dwarfPlayer = ( DwarfPlayer ) outputList.get( 0 );
                }
                catch ( CommandException dce )
                {
                    if ( dce.getType() == Type.PARSEDWARFFAIL || dce.getType() == Type.TOOFEWARGS || dce.getType() == Type.EMPTYPLAYER )
                    {
                        if ( !( sender instanceof Player ) )
                            throw new CommandException( plugin, Type.CONSOLECANNOTUSE );
                        
                        desiredArguments.remove( 0 );
                        outputList = parser.parse( desiredArguments, true );
                        skill = ( DwarfSkill ) outputList.get( 0 );
                        dwarfPlayer = plugin.getDataManager().find( ( Player ) sender );

                        if ( dwarfPlayer.getRace().getId().equals( "" ) )
                        {
                            plugin.getOut().sendMessage( sender, Messages.chooseARace );
                            return true;
                        }
                    }
                    else
                        throw dce;
                }
                plugin.getOut().printSkillInfo( sender, skill, dwarfPlayer, plugin.getConfigManager().getMaxSkillLevel() );
                return true;
            }
            catch ( CommandException e )
            {
                e.describe( sender );
                sender.sendMessage( getUsage() );
                return true;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete( CommandSender commandSender, Command command, String s, String[] args )
    {
        if ( !command.getName().equalsIgnoreCase( "dwarfcraft" ) || !plugin.isEnabled() )
            return null;

        ArrayList<String> completions = new ArrayList<>();
        ArrayList<String> matches = new ArrayList<>();
        if ( args.length == 2 )
        {
            completions.clear();
            matches.clear();

            for ( Player player : plugin.getServer().getOnlinePlayers() )
            {
                // Strip Colours from Display Names from silly plugins that add it to the Player Name instead of using prefixes
                completions.add( ChatColor.stripColor( player.getPlayerListName() ) );
            }

            return StringUtil.copyPartialMatches( args[1], completions, matches );
        }
        else if ( args.length == 3 )
        {
            completions.clear();
            matches.clear();

            // Gets a list of all possible skill names
            completions.addAll( plugin.getSkillManager().getAllSkills().keySet() );
            return StringUtil.copyPartialMatches( args[2], completions, matches );
        }

        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "Displays a description of a skill and training costs\n/dwarfcraft skill <player name> [skill_id]\n" +
                "Example: /dwarfcraft skill pickaxe_use - Prints details about the Pickaxe Use skill\nExample: "
                + "/dwarfcraft skill smartaleq pickaxe_use - Prints details about smartaleq's Pickaxe Use skill";
    }
}
