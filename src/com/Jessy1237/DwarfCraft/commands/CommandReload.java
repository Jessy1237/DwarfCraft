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

import java.util.logging.Level;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.models.DwarfCommand;

public class CommandReload extends DwarfCommand
{
    public CommandReload( String name )
    {
        super( name );
        setDescription("Reloads the DwarfCraft plugin.");
    }

    @Override
    public boolean execute( CommandSender sender, String commandLabel, String[] args )
    {
        if ( args.length > 0 )
        {
            if ( args[0].equalsIgnoreCase( "?" ) )
            {
                plugin.getOut().sendMessage( sender, description );
            }
        }
        else
        {
            plugin.getUtil().debugLog( 1, Level.FINE, "Started command 'reload'" );

            if ( sender instanceof Player )
                plugin.getOut().sendMessage( sender, "&aReloading DwarfCraft..." );
            plugin.getUtil().consoleLog( "Reloading...", Level.FINE );

            plugin.getConfigManager().clearCommands();
            plugin.onDisable();
            plugin.reloadConfig();
            plugin.onEnable( true );

            if ( sender instanceof Player )
                plugin.getOut().sendMessage( sender, "&aReload complete" );
            plugin.getUtil().consoleLog( "Reload complete", Level.FINE );
        }
        return true;
    }

    @Override
    public String getUsage() {
        return "/dwarfcraft reload";
    }

    @Override
    public boolean isOp() {
        return true;
    }
}
