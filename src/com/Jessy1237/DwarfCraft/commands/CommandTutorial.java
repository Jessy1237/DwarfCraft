package com.Jessy1237.DwarfCraft.commands;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import com.Jessy1237.DwarfCraft.Messages;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.logging.Level;

public class CommandTutorial extends Command
{
    private final DwarfCraft plugin;

    public CommandTutorial( final DwarfCraft plugin )
    {
        super( "Tutorial" );
        this.plugin = plugin;
    }

    @Override
    public boolean execute( CommandSender sender, String commandLabel, String[] args )
    {
        if ( DwarfCraft.debugMessagesThreshold < 1 )
            System.out.println( "DC1: started command 'tutorial'" );

        // Ensure the command is being run by an in-game player, otherwise the book cannot be given to them
        if ( sender instanceof Player )
        {
            // TODO: Make Tutorial Messages customizable
            Messages.TutorialMessage[] bookPages = Messages.TutorialMessage.values();

            // Create a new Written Book
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);

            // Set the BookMeta onto the Written Book
            BookMeta bookMeta = (BookMeta) book.getItemMeta();
            bookMeta.setTitle( "Welcome to DwarfCraft" );
            bookMeta.setAuthor( "Jessy1237" );

            for ( Messages.TutorialMessage pageFixed : bookPages )
            {
                String page = pageFixed.getMessage();

                if ( page.startsWith( "{" ) || page.startsWith( "[" ) )
                {
                    try {
                        BaseComponent[] comps = ComponentSerializer.parse( ChatColor.translateAlternateColorCodes( '&', page ) );
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
                    bookMeta.addPage( ChatColor.translateAlternateColorCodes( '&', page ) );
                }
            }

            book.setItemMeta( bookMeta );

            // Add Written Book to Players Inventory
            ( (Player) sender ).getInventory().addItem( book );
        }
        else
        {
            sender.sendMessage( "Error: This command must be run from in-game" );
        }

        return true;
    }
}
