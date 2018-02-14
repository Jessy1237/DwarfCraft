package com.Jessy1237.DwarfCraft.commands;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.*;

import com.Jessy1237.DwarfCraft.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.Jessy1237.DwarfCraft.CommandException.Type;
import com.Jessy1237.DwarfCraft.models.DwarfPlayer;
import com.Jessy1237.DwarfCraft.models.DwarfSkill;
import org.bukkit.util.StringUtil;

public class CommandSkillInfo extends Command implements TabCompleter
{
    private final DwarfCraft plugin;

    public CommandSkillInfo( final DwarfCraft plugin )
    {
        super( "SkillInfo" );
        this.plugin = plugin;
    }

    @Override
    public boolean execute( CommandSender sender, String commandLabel, String[] args )
    {
        if ( DwarfCraft.debugMessagesThreshold < 1 )
            System.out.println( "DC1: started command 'skillinfo'" );

        if ( args.length == 0 || args == null )
        {
            plugin.getOut().sendMessage( sender, CommandInformation.Usage.SKILLINFO.getUsage() );
        }
        else if ( args[0].equalsIgnoreCase( "?" ) )
        {
            plugin.getOut().sendMessage( sender, CommandInformation.Desc.SKILLINFO.getDesc() );
        }
        else
        {
            try
            {
                CommandParser parser = new CommandParser( plugin, sender, args );
                List<Object> desiredArguments = new ArrayList<Object>();
                List<Object> outputList = null;

                DwarfPlayer dwarfPlayer = new DwarfPlayer( plugin, null );
                DwarfSkill skill = new DwarfSkill( 0, null, 0, null, null, null, null, null );
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
                    if ( dce.getType() == Type.PARSEDWARFFAIL || dce.getType() == Type.TOOFEWARGS )
                    {
                        desiredArguments.remove( 0 );
                        outputList = parser.parse( desiredArguments, true );
                        skill = ( DwarfSkill ) outputList.get( 0 );
                        if ( !( sender instanceof Player ) )
                            throw new CommandException( plugin, Type.CONSOLECANNOTUSE );
                        dwarfPlayer = plugin.getDataManager().find( ( Player ) sender );
                    }
                    else
                        throw dce;
                }
                plugin.getOut().printSkillInfo( sender, skill, dwarfPlayer, 30 );
                return true;

            }
            catch ( CommandException e )
            {
                e.describe( sender );
                sender.sendMessage( CommandInformation.Usage.SKILLINFO.getUsage() );
                return false;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if ( !command.getName().equalsIgnoreCase( "dwarfcraft" ) ) return null;

        if ( args.length == 2 ) {
            // Gets a list of all possible skill names
            Collection<DwarfSkill> skills = plugin.getConfigManager().getAllSkills().values();
            ArrayList<String> completions = new ArrayList<>();
            ArrayList<String> matches = new ArrayList<>();

            for ( DwarfSkill skill : skills )
            {
                String skillName = skill.getDisplayName().replaceAll( " ", "_" );
                completions.add( skillName.toLowerCase() );
            }

            return StringUtil.copyPartialMatches( args[1], completions, matches );
        }

        return Collections.emptyList();
    }
}