package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import org.bukkit.command.CommandSender;

public class CommandException extends Throwable
{

    public enum Type
    {
        TOOFEWARGS( "You did not provide enough arguments for that command" ),
        TOOMANYARGS( "You gave too many arguments for that command" ),
        PARSEDWARFFAIL( "Could not locate the player you named" ),
        PARSELEVELFAIL( "Could not understand the skill level as a number" ),
        PARSESKILLFAIL( "Could not find the skill name or ID you provided" ),
        PARSEEFFECTFAIL( "Could not understand your effect input (Use an ID)" ),
        EMPTYPLAYER( "Player argument was empty" ),
        COMMANDUNRECOGNIZED( "Could not understand what command you were trying to use" ),
        LEVELOUTOFBOUNDS( "Skill level must be between -1 and 30" ),
        PARSEINTFAIL( "Could not understand some input as a number" ),
        PAGENUMBERNOTFOUND( "Could not find the page number provided" ),
        CONSOLECANNOTUSE( "Either the console cannot use this command, or a player must be provided as a target." ),
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
    private final DwarfCraft plugin;
    private static final long serialVersionUID = 7319961775971310701L;

    protected CommandException( final DwarfCraft plugin )
    {
        this.plugin = plugin;
    }

    public CommandException( final DwarfCraft plugin, Type type )
    {
        this.plugin = plugin;
        this.type = type;
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
