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

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.models.DwarfCommand;

public class CommandInfo extends DwarfCommand
{
    public CommandInfo( String name, DwarfCraft plugin )
    {
        super( name, plugin );
    }

    @Override
    public boolean execute( CommandSender sender, String commandLabel, String[] args )
    {
        plugin.getUtil().debugLog( 1, Level.FINE, "Started command 'info'" );
        plugin.getOut().info( sender );
        return true;
    }
}
