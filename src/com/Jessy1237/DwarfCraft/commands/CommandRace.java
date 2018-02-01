package com.Jessy1237.DwarfCraft.commands;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Jessy1237.DwarfCraft.CommandInformation;
import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.events.DwarfCraftRaceChangeEvent;
import com.Jessy1237.DwarfCraft.model.DwarfPlayer;

public class CommandRace extends Command
{
    private final DwarfCraft plugin;

    public CommandRace( final DwarfCraft plugin )
    {
        super( "DwarfRace" );
        this.plugin = plugin;
    }

    @Override
    public boolean execute( CommandSender sender, String commandLabel, String[] args )
    {
        if ( DwarfCraft.debugMessagesThreshold < 1 )
            System.out.println( "DC1: started command 'race'" );

        if ( args.length == 0 )
        {
            plugin.getOut().race( sender, ( Player ) sender );
        }
        else if ( ( !( sender instanceof Player ) || plugin.getPermission().has( sender, "dwarfcraft.op.race" ) ) && args.length == 1 )
        {
            Player p = plugin.getServer().getPlayer( args[0] );
            if ( p == null )
            {
                plugin.getOut().sendMessage( sender, CommandInformation.Usage.RACE.getUsage() );
            }
            else
            {
                plugin.getOut().adminRace( sender, p );
            }
        }
        else if ( args.length < 2 )
        {
            plugin.getOut().sendMessage( sender, CommandInformation.Usage.RACE.getUsage() );
        }
        else if ( args[0].equalsIgnoreCase( "?" ) )
        {
            plugin.getOut().sendMessage( sender, CommandInformation.Desc.RACE.getDesc() );
        }
        else if ( args.length == 3 )
        {
            String newRace = args[1];
            String name = args[0];
            Player p = plugin.getServer().getPlayer( args[0] );
            DwarfPlayer dCPlayer = null;
            if ( p == null )
            {
                plugin.getOut().sendMessage( sender, "Not a valid Player Name." );
                return true;
            }
            else
            {
                dCPlayer = plugin.getDataManager().find( p );
            }
            boolean confirmed = false;
            if ( args[2] != null )
            {
                if ( args[2].equalsIgnoreCase( "confirm" ) )
                {
                    confirmed = true;
                }
            }
            if ( sender instanceof Player )
            {
                if ( plugin.getPermission().has( sender, "dwarfcraft.op.race" ) )
                {
                    race( newRace, confirmed, dCPlayer, ( CommandSender ) plugin.getServer().getPlayer( name ) );
                }
            }
            else
            {
                race( newRace, confirmed, dCPlayer, sender );
            }
        }
        else
        {
            String newRace = args[0];
            DwarfPlayer dCPlayer = plugin.getDataManager().find( ( Player ) sender );
            boolean confirmed = false;
            if ( args[1] != null )
            {
                if ( args[1].equalsIgnoreCase( "confirm" ) )
                {
                    confirmed = true;
                }
            }
            race( newRace, confirmed, dCPlayer, sender );
        }
        return true;
    }

    private void race(String newRace, boolean confirm, DwarfPlayer dCPlayer, CommandSender sender )
    {
        if ( dCPlayer.getRace() == newRace )
        {
            plugin.getOut().alreadyRace( sender, dCPlayer, newRace );
        }
        else
        {
            if ( confirm )
            {
                if ( plugin.getConfigManager().getRace( newRace ) != null )
                {
                    if ( sender instanceof Player )
                    {
                        if ( plugin.getPermission().has( ( Player ) sender, "dwarfcraft.norm.race." + newRace.toLowerCase() ) )
                        {
                            DwarfCraftRaceChangeEvent e = new DwarfCraftRaceChangeEvent( dCPlayer, plugin.getConfigManager().getRace( newRace ) );
                            plugin.getServer().getPluginManager().callEvent( e );

                            if ( !e.isCancelled() )
                            {
                                plugin.getOut().changedRace( sender, dCPlayer, plugin.getConfigManager().getRace( e.getRace().getName() ).getName() );
                                dCPlayer.changeRace( e.getRace().getName() );
                            }
                        }
                        else
                        {
                            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to do that." );
                        }
                    }
                    else
                    {
                        DwarfCraftRaceChangeEvent e = new DwarfCraftRaceChangeEvent( dCPlayer, plugin.getConfigManager().getRace( newRace ) );
                        plugin.getServer().getPluginManager().callEvent( e );

                        if ( !e.isCancelled() )
                        {
                            plugin.getOut().changedRace( dCPlayer.getPlayer(), dCPlayer, plugin.getConfigManager().getRace( e.getRace().getName() ).getName() );
                            dCPlayer.changeRace( e.getRace().getName() );
                        }
                    }
                }
                else
                {
                    if ( sender instanceof Player )
                        plugin.getOut().dExistRace( sender, dCPlayer, newRace );
                }
            }
            else
            {
                if ( sender instanceof Player )
                    plugin.getOut().confirmRace( sender, dCPlayer, newRace );
            }
        }
    }
}
