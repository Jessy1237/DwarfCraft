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

import com.jessy1237.dwarfcraft.DwarfCraft;
import org.bukkit.command.CommandSender;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */
public class CommandException extends Throwable
{

    public enum Type
    {
        
        TOOFEWARGS( "&cYou did not provide enough arguments for that command" ),
        TOOMANYARGS( "&cYou gave too many arguments for that command" ),
        PARSEDWARFFAIL( "&cCould not locate the player you named" ),
        PARSELEVELFAIL( "&cCould not understand the skill level as a number" ),
        PARSESKILLFAIL( "&cCould not find a skill with that Skill ID" ),
        PARSEEFFECTFAIL( "&cCould not understand your effect input (Use an ID)" ),
        EMPTYPLAYER( "Player argument was empty" ),
        COMMANDUNRECOGNIZED( "Could not understand what command you were trying to use" ),
        LEVELOUTOFBOUNDS( "Skill level must be between -1 and 30" ),
        PARSEINTFAIL( "Could not understand some input as a number" ),
        PAGENUMBERNOTFOUND( "Could not find the page number provided" ),
        CONSOLECANNOTUSE( "Command must be run as a player" ),
        NEEDPERMISSIONS( "You must be an op to use this command." ),
        NOGREETERMESSAGE( "Could not find that greeter message. Add it to greeters.config" ),
        NPCIDINUSE( "You can't use this ID for a trainer, it is already used." ),
        PARSEPLAYERFAIL( "Could not locate the player you named" ),
        NPCIDNOTFOUND( "You must specify the exact ID for the trainer, the one provided was not found." ),
        PARSERACEFAIL( "Could not understand the race name you used." ),
        INVALIDENTITYTYPE( "You must specify a valid EntityType." );

        String errorMsg;

        
        Type( String errorMsg )
        {
            this.errorMsg = errorMsg;
        }
    }

    private Type type;
    private static DwarfCraft plugin;
    private static final long serialVersionUID = 7319961775971310701L;

    protected CommandException( final DwarfCraft plugin )
    {
        CommandException.plugin = plugin;
    }

    public CommandException( final DwarfCraft plugin, Type type )
    {
        CommandException.plugin = plugin;
        this.type = type;

        // This cant be done beforehand as the plugin will be null and cannot access the config value
        // This caused the initialization of the CommandException to fail due to a null max skill level
        Type.LEVELOUTOFBOUNDS.errorMsg = "Skill level must be between -1 and " + plugin.getConfigManager().getMaxSkillLevel();
    }

    public void describe( CommandSender sender )
    {
        plugin.getOut().sendMessage( sender, type.errorMsg );
    }

    public Type getType()
    {
        return type;
    }

}
