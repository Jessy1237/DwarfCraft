/*
 * Copyright (c) 2018.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.Messages;
import com.jessy1237.dwarfcraft.commands.CommandException.Type;
import com.jessy1237.dwarfcraft.models.DwarfCommand;
import com.jessy1237.dwarfcraft.models.DwarfPlayer;

public class CommandSkillSheet extends DwarfCommand
{
    public CommandSkillSheet( String name, DwarfCraft plugin )
    {
        super( name, plugin );
        setDescription("Displays a list of skills and levels for a Dwarf.");
    }

    @Override
    public boolean execute( CommandSender sender, String commandLabel, String[] args )
    {
        plugin.getUtil().debugLog( 1, Level.FINE, "Started command 'skillsheet'" );

        try
        {
            if ( args.length == 0 && sender instanceof Player )
            {
                DwarfPlayer dCPlayer = plugin.getDataManager().find( ( Player ) sender );
                if ( dCPlayer.getRace().getId().equals( "" ) )
                {
                    plugin.getOut().sendMessage( sender, Messages.chooseARace );
                    return true;
                }

                plugin.getOut().printSkillSheet( dCPlayer, sender, false );
                return true;
            }
            else if ( args.length == 0 )
            {
                throw new CommandException( plugin, Type.CONSOLECANNOTUSE );
            }
            else if ( args[0].equalsIgnoreCase( "?" ) )
            {
                plugin.getOut().sendMessage( sender, description );
                return true;
            }
            else
            {
                CommandParser parser = new CommandParser( plugin, sender, args );
                List<Object> desiredArguments = new ArrayList<>();
                List<Object> outputList;
                boolean printFull = false;

                if ( args[0].equalsIgnoreCase( "-f" ) || args[0].equalsIgnoreCase( "full" ) )
                {
                    printFull = true;
                    desiredArguments.add( args[0] );
                }

                DwarfPlayer dCPlayer = new DwarfPlayer( plugin, null );
                desiredArguments.add( dCPlayer );

                try
                {
                    outputList = parser.parse( desiredArguments, false );
                    if ( outputList.get( 0 ) instanceof String )
                        dCPlayer = ( DwarfPlayer ) outputList.get( 1 );
                    else
                        dCPlayer = ( DwarfPlayer ) outputList.get( 0 );
                }
                catch ( CommandException dce )
                {
                    if ( dce.getType() == Type.PARSEDWARFFAIL || dce.getType() == Type.TOOFEWARGS || dce.getType() == Type.EMPTYPLAYER )
                    {
                        if ( sender instanceof Player )
                        {
                            dCPlayer = plugin.getDataManager().find( ( Player ) sender );
                        }
                        else
                            throw new CommandException( plugin, Type.CONSOLECANNOTUSE );
                    }
                    else
                        throw dce;
                }

                if ( dCPlayer.getRace().getId().equals( "" ) )
                {
                    plugin.getOut().sendMessage( sender, Messages.chooseARace );
                    return true;
                }
                plugin.getOut().printSkillSheet( dCPlayer, sender, printFull );
                return true;
            }
        }
        catch ( CommandException e )
        {
            e.describe( sender );
            sender.sendMessage( getUsage() );
            return false;
        }
    }

    @Override
    public String getUsage() {
        return "Displays a list of current skills and levels" +
                "\\n/dwarfcraft skillsheet (full/-f) (player_name)\\n" +
                "Example: /dwarfcraft skillsheet smartaleq - Prints smartaleq's skillsheet" +
                "\\nExample: /dwarfcraft ss -f - Prints your skillsheet including untrained skills";
    }
}
