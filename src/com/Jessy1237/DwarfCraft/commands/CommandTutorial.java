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
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Messages;
import com.Jessy1237.DwarfCraft.commands.CommandException.Type;
import com.Jessy1237.DwarfCraft.models.DwarfCommand;
import com.Jessy1237.DwarfCraft.models.DwarfPlayer;

public class CommandTutorial extends DwarfCommand
{
    final int pageCharLimit = 256;

    public CommandTutorial( final DwarfCraft plugin, String name )
    {
        super( plugin, name );
        setDescription("Gives the player the DwarfCraft Pocket Guide");
    }

    @Override
    public boolean execute( CommandSender sender, String commandLabel, String[] args )
    {
        if ( DwarfCraft.debugMessagesThreshold < 1 )
            plugin.getUtil().consoleLog( Level.FINE, "DC1: started command 'tutorial'" );

        try
        {
            // Ensure the command is being run by an in-game player, otherwise the book cannot be given to them
            if ( sender instanceof Player && args.length == 0 )
            {
                Player player = ( Player ) sender;
                // Add Written Book to Players Inventory
                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem( createTutorialBook( plugin.getDataManager().find( player ) ) );
                dropBookIfInventoryFull( player, overflow );

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
                List<Object> desiredArguments = new ArrayList<Object>();
                List<Object> outputList = null;

                DwarfPlayer target = new DwarfPlayer( plugin, null );
                desiredArguments.add( target );

                outputList = parser.parse( desiredArguments, false );
                target = ( DwarfPlayer ) outputList.get( 0 );

                HashMap<Integer, ItemStack> overflow = target.getPlayer().getInventory().addItem( createTutorialBook( target ) );
                dropBookIfInventoryFull( target.getPlayer(), overflow );
                sender.sendMessage( ChatColor.DARK_GREEN + "Spawned tutorial book for " + target.getPlayer().getName() );
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

    public ItemStack createTutorialBook( DwarfPlayer dwarfPlayer )
    {
        // Create a new Written Book
        ItemStack book = new ItemStack( Material.WRITTEN_BOOK );

        // Set the BookMeta onto the Written Book
        BookMeta bookMeta = ( BookMeta ) book.getItemMeta();
        bookMeta.setTitle( "DwarfCraft Pocket Guide" );
        bookMeta.setAuthor( "Jessy1237" );

        for ( String readPage : Messages.tutorial )
        {

            String page = plugin.getOut().parseColors( dwarfPlayer.toString( readPage ) );

            if ( isOverPageLimit( bookMeta, dwarfPlayer.getPlayer() ) )
                break;

            if ( page.startsWith( "{" ) || page.startsWith( "[" ) )
            {
                try
                {
                    BaseComponent[] comps = ComponentSerializer.parse( page );
                    bookMeta.spigot().addPage( comps );
                }
                catch ( Exception e )
                {
                    plugin.getLogger().log( Level.SEVERE, "Failed to add page to Tutorial Book! Skipping..." );
                    plugin.getLogger().log( Level.SEVERE, "Invalid JSON found at page: " + page );
                }
            }
            else
            {
                // Handle text overflowing the page by inserting additional pages
                String leftOver = page;
                while ( leftOver != null )
                {
                    String section;
                    if ( leftOver.length() > pageCharLimit )
                    {
                        section = leftOver.substring( 0, pageCharLimit );

                        int index = section.lastIndexOf( ' ' );
                        section = section.substring( 0, index++ );
                        leftOver = leftOver.substring( index);
                    }
                    else
                    {
                        section = leftOver;
                        leftOver = null;
                    }

                    bookMeta.addPage( section );
                }

                if ( isOverPageLimit( bookMeta, dwarfPlayer.getPlayer() ) )
                    break;

            }
        }

        book.setItemMeta( bookMeta );

        return book;
    }

    private boolean isOverPageLimit( BookMeta bookMeta, CommandSender sender )
    {
        if ( bookMeta.getPageCount() >= 50 )
        {
            plugin.getLogger().log( Level.SEVERE, "The tutorial book cannot support more than 50 pages!" );
            sender.sendMessage( ChatColor.DARK_RED + "The tutorial book reached the maximum page limit of 50. Skipping all additional pages... " );

            return true;
        }

        return false;
    }

    public void dropBookIfInventoryFull( Player player, HashMap<Integer, ItemStack> overflowStacks )
    {
        if ( !overflowStacks.isEmpty() )
        {
            if ( overflowStacks.get( 0 ).getType() != Material.WRITTEN_BOOK )
                return;
            player.getWorld().dropItem( player.getLocation(), overflowStacks.get( 0 ) );
        }
    }

    @Override
    public String getUsage() {
        return "/dwarfcraft tutorial \nExample: /dwarfcraft tutorial - Gives you the DwarfCraft Pocket Guide.\n" +
                "Admin: /dwarfcraft tutorial <Player> - Gives the specified player the DwarfCraft Pocket Guide.";
    }
}
