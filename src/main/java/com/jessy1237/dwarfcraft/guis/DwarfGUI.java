/*
 * Copyright (c) 2018.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft.guis;

import java.util.ArrayList;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.models.DwarfPlayer;

public abstract class DwarfGUI
{

    protected DwarfCraft plugin;
    protected DwarfPlayer dwarfPlayer;
    protected Inventory inventory;

    public DwarfGUI( DwarfCraft plugin, DwarfPlayer dwarfPlayer )
    {
        this.plugin = plugin;
        this.dwarfPlayer = dwarfPlayer;
    }

    public abstract void init();

    /**
     * This is any cleanup code that is fired before the GUI is deleted
     */
    public abstract void remove();

    /**
     * This is what the GUI does when it is clicked
     * 
     * @param event The event that has observed the click in the GUI
     */
    public abstract void click( InventoryClickEvent event );

    public void openGUI()
    {
        if ( dwarfPlayer != null && inventory != null )
        {
            dwarfPlayer.getPlayer().openInventory( inventory );
        }
    }

    public DwarfPlayer getDwarfPlayer()
    {
        return dwarfPlayer;
    }

    public Inventory getInventory()
    {
        return inventory;
    }

    /**
     * Adds an Item as a clickable option to the GUI
     *
     * @param name The display name of the item in the inventory
     * @param lore The lore of the item in the inventory
     * @param guiIndex This the index of the item slot
     * @param item The item to be added as an option to the GUI
     */
    public void addItem( String name, ArrayList<String> lore, int guiIndex, ItemStack item )
    {
        if ( inventory == null )
            return;

        ItemMeta meta = item.getItemMeta();
        if ( lore != null )
            meta.setLore( lore );
        if ( name != null )
            meta.setDisplayName( name );
        meta.addItemFlags( ItemFlag.HIDE_ATTRIBUTES );
        meta.addItemFlags( ItemFlag.HIDE_ENCHANTS );
        meta.addItemFlags( ItemFlag.HIDE_DESTROYS );
        meta.addItemFlags( ItemFlag.HIDE_PLACED_ON );
        meta.addItemFlags( ItemFlag.HIDE_POTION_EFFECTS );
        meta.addItemFlags( ItemFlag.HIDE_UNBREAKABLE );

        item.setItemMeta( meta );

        inventory.setItem( guiIndex, item );
    }

    /**
     * Parses the input string into a readable lore for items
     * 
     * @param str The input string to convert to lore
     * @param prefix The prefix formatting to add to the lore. i.e ChatColor
     * @return The lore generated from the string
     */
    protected ArrayList<String> parseStringToLore( String str, String prefix )
    {
        final int loreMax = 30;
        ArrayList<String> lore = new ArrayList<String>();
        String leftOver = str;
        while ( leftOver != null )
        {
            String section = "";
            if ( leftOver.length() > loreMax )
            {
                section = leftOver.substring( 0, loreMax );

                int index = section.lastIndexOf( ' ' );
                section = section.substring( 0, index++ );
                leftOver = leftOver.substring( index );
            }
            else
            {
                section = leftOver;
                leftOver = null;
            }

            lore.add( prefix + section );
        }

        return lore;
    }
}
