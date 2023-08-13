/*
 * Copyright (c) 2018.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft.listeners;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.events.DwarfEffectEvent;
import com.jessy1237.dwarfcraft.guis.DwarfGUI;
import com.jessy1237.dwarfcraft.models.DwarfPlayer;
import com.jessy1237.dwarfcraft.models.DwarfSkill;
import com.jessy1237.dwarfcraft.models.effects.DwarfEffect;
import com.jessy1237.dwarfcraft.models.effects.DwarfEffectType;

public class DwarfInventoryListener implements Listener
{
    private final DwarfCraft plugin;
    private final HashMap<Location, BrewerInventory> stands = new HashMap<>();
    private final HashMap<Player, DwarfGUI> dwarfGUIs = new HashMap<>();

    public DwarfInventoryListener( DwarfCraft plugin )
    {
        this.plugin = plugin;
    }

    @EventHandler( priority = EventPriority.NORMAL )
    public void onFurnaceExtractEvent( FurnaceExtractEvent event )
    {
        Player player = event.getPlayer();
        DwarfPlayer dCPlayer = plugin.getDataManager().find( player );
        ItemStack result = new ItemStack( event.getItemType(), event.getItemAmount() );

        if ( !plugin.getUtil().isWorldAllowed( player.getWorld() ) )
            return;

        for ( DwarfSkill skill : dCPlayer.getSkills().values() )
        {
            for ( DwarfEffect effect : skill.getEffectsOfType(DwarfEffectType.SMELT) )
            {
                if ( effect.checkInitiator( result.getType() ) )
                {
                    final ItemStack output = effect.getOutput( dCPlayer );

                    DwarfEffectEvent ev = new DwarfEffectEvent( dCPlayer, effect, new ItemStack[] { result }, new ItemStack[] { output }, null, null, null, null, null, null, null );
                    plugin.getServer().getPluginManager().callEvent( ev );

                    if ( ev.isCancelled() )
                        return;

                    for ( ItemStack item : ev.getAlteredItems() )
                    {
                        if ( item != null && item.getAmount() > 0 )
                        {
                            if ( item.getType() == result.getType() )
                            {
                                int extraAmount = ( item.getAmount() * event.getItemAmount() ) - event.getItemAmount();
                                int exp = ( event.getExpToDrop() / event.getItemAmount() );

                                event.setExpToDrop( exp * ( event.getItemAmount() * extraAmount ) );

                                HashMap<Integer, ItemStack> overflow = player.getInventory().addItem( new ItemStack( item.getType(), extraAmount ) );

                                if ( !overflow.isEmpty() )
                                {
                                    for ( Map.Entry<Integer, ItemStack> overflowSet : overflow.entrySet() )
                                    {
                                        player.getWorld().dropItemNaturally( player.getLocation(), overflowSet.getValue() );
                                    }
                                }

                                player.updateInventory();
                            }
                            else
                            {
                                player.getWorld().dropItemNaturally( player.getLocation(), item );
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler( priority = EventPriority.NORMAL )
    public void onCraftItem( CraftItemEvent event )
    {
        HumanEntity player = event.getWhoClicked();
        DwarfPlayer dCPlayer = plugin.getDataManager().find( ( Player ) event.getWhoClicked() );
        ItemStack result = event.getRecipe().getResult();
        boolean isShiftClick = event.getClick().isShiftClick();

        if ( !plugin.getUtil().isWorldAllowed( player.getWorld() ) )
            return;

        // TODO 1.14 Do we still need to special case repairing of tools? Test this....
        // Make sure they aren't duping when repairing tools
        //        if ( plugin.getUtil().isTool( toCraft.getType() ) )
        //        {
        //            CraftingInventory ci = ( CraftingInventory ) event.getClickedInventory();
        //            if ( ci.getRecipe() instanceof ShapelessRecipe )
        //            {
        //                ShapelessRecipe r = ( ShapelessRecipe ) ci.getRecipe();
        //                for ( ItemStack i : r.getIngredientList() )
        //                {
        //                    if ( plugin.getUtil().isTool( i.getType() ) && toCraft.getType() == i.getType() )
        //                    {
        //                        return;
        //                    }
        //                }
        //            }
        //        }

        for ( DwarfSkill skill : dCPlayer.getSkills().values() )
        {
            for ( DwarfEffect effect : skill.getEffectsOfType(DwarfEffectType.CRAFT) )
            {
                if ( effect.checkInitiator( result.getType() ) )
                {
                    final ItemStack output = effect.getOutput( dCPlayer );
                    int itemsChecked = 0;
                    int possibleCrafts = 1; // the number of possible crafting operations on a shift click craft

                    DwarfEffectEvent ev = new DwarfEffectEvent( dCPlayer, effect, new ItemStack[] { result }, new ItemStack[] { output }, null, null, null, null, null, null, null );
                    plugin.getServer().getPluginManager().callEvent( ev );

                    if ( ev.isCancelled() )
                        return;

                    if ( isShiftClick )
                    {
                        for ( ItemStack item : event.getInventory().getMatrix() )
                        {
                            if ( item != null && !item.getType().equals( Material.AIR ) )
                            {
                                if ( itemsChecked == 0 )
                                    possibleCrafts = item.getAmount();
                                else
                                    possibleCrafts = Math.min( possibleCrafts, item.getAmount() );
                                itemsChecked++;
                            }
                        }
                    }

                    for ( ItemStack item : ev.getAlteredItems() )
                    {
                        if ( item != null && item.getAmount() > 0 )
                        {
                            if ( item.getType() == result.getType() )
                            {
                                // If the craft is a shift click we need to cancel the crafting event and modify the
                                // players inventory directly. Otherwise we modify the crafting result and allow the
                                // craft to proceed as normal.
                                if ( isShiftClick )
                                {
                                    event.setCancelled( true );
                                    item.setAmount( item.getAmount() * possibleCrafts );

                                    event.getInventory().setMatrix( new ItemStack[9] ); // Clear crafting grid
                                    HashMap<Integer, ItemStack> overflow = player.getInventory().addItem( item ); // Add items to player inventory

                                    // Check if there are any overflow items that couldn't fit in the players inventory
                                    // and drop them into the world at the players location.
                                    if ( !overflow.isEmpty() )
                                    {
                                        for ( Map.Entry<Integer, ItemStack> overflowSet : overflow.entrySet() )
                                        {
                                            player.getWorld().dropItemNaturally( player.getLocation(), overflowSet.getValue() );
                                        }
                                    }

                                }
                                else
                                {
                                    event.getInventory().setResult( item );
                                }
                            }
                            else
                            {
                                player.getWorld().dropItemNaturally( player.getLocation(), item );
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings( "unlikely-arg-type" )
    @EventHandler( priority = EventPriority.NORMAL )
    public void onBrewEvent( BrewEvent event )
    {
        if ( !stands.containsKey( event.getBlock() ) )
        {
            stands.put( event.getBlock().getLocation(), event.getContents() );
        }
        else
        {
            stands.remove( event.getBlock().getLocation() );
            stands.put( event.getBlock().getLocation(), event.getContents() );
        }
    }

    @EventHandler( priority = EventPriority.NORMAL )
    public void onInventoryCloseEvent( InventoryCloseEvent event )
    {
        DwarfGUI dwarfGUI = dwarfGUIs.get( event.getPlayer() );

        if ( dwarfGUI == null )
            return;

        dwarfGUI.remove();
        dwarfGUIs.remove( event.getPlayer() );
    }

    public void addDwarfGUI( Player player, DwarfGUI gui )
    {
        gui.init();
        gui.openGUI();
        player.updateInventory();
        dwarfGUIs.put( player, gui );
    }

    @EventHandler( priority = EventPriority.NORMAL )
    public void onInventoryClickEvent( InventoryClickEvent event )
    {
        if ( !plugin.getUtil().isWorldAllowed( event.getWhoClicked().getWorld() ) )
            return;

        DwarfGUI dwarfGUI = dwarfGUIs.get( event.getWhoClicked() );
        if ( dwarfGUI != null )
        {
            if ( dwarfGUI.getInventory().equals( event.getClickedInventory() ) )
            {
                event.setCancelled( true );
                dwarfGUI.click( event );
            }
        }

        // Note: This is used as a BrewingStandExtract event as Spigot doesn't have one.
        // Also because items can be placed into the "result" slot of a stand at any time,
        // we must track the stands in BrewEvent to ensure brewing has actually happened before
        // we respond to the effect. This is probably why Spigot doesnt have a BrewingExtractEvent.
        // This code is kinda ugly but still needs to happen this way as far as im aware.
        if ( event.getSlotType() == SlotType.CRAFTING && event.getSlot() < 3 && event.getSlot() >= 0 && event.getClickedInventory().getHolder() instanceof BrewingStand )
        {
            DwarfPlayer dwarfPlayer = plugin.getDataManager().find( ( Player ) event.getWhoClicked() );
            HashMap<String, DwarfSkill> skills = dwarfPlayer.getSkills();
            ItemStack item = event.getCurrentItem();
            final int amount = item.getAmount();
            BrewingStand block = ( BrewingStand ) event.getInventory().getHolder();
            BrewerInventory brewInventory = check( block.getLocation() );
            PotionMeta potionMeta = ( PotionMeta ) item.getItemMeta();

            // Brewing has not taken place yet but a player has clicked the result slots of the stand, so exit early
            if ( brewInventory == null )
                return;

            ItemStack[] stack = brewInventory.getContents();
            if ( sameInv( stack, block.getInventory() ) )
            {
                for ( DwarfSkill s : skills.values() )
                {
                    for ( DwarfEffect effect : s.getEffects() )
                    {
                        if ( effect.getEffectType() == DwarfEffectType.BREW && item.getType() == Material.POTION )
                        {
                            int newAmount = ( int ) ( amount * effect.getEffectAmount( dwarfPlayer ) );

                            ItemStack clickedStack = new ItemStack( item.getType(), newAmount );
                            clickedStack.setItemMeta( potionMeta );

                            DwarfEffectEvent ev = new DwarfEffectEvent( dwarfPlayer, effect, new ItemStack[] { item }, new ItemStack[] { clickedStack }, null, null, null, null, null, block.getBlock(), null );
                            plugin.getServer().getPluginManager().callEvent( ev );

                            if ( ev.isCancelled() )
                                return;

                            // Potions from DwarfEffect should still use fuel from the stand for gameplay reasons
                            block.setFuelLevel( block.getFuelLevel() - ( newAmount - 1 ) );

                            Player player = dwarfPlayer.getPlayer();
                            for ( ItemStack item1 : ev.getAlteredItems() )
                            {
                                if ( item1 != null && item1.getAmount() > 0 )
                                {
                                    if ( item1.getType() == item.getType() )
                                    {
                                        ItemStack newItem = new ItemStack( item1.getType(), newAmount - 1 );

                                        if ( item1.getType() != Material.POTION || item1.getType() != Material.SPLASH_POTION || item1.getType() != Material.LINGERING_POTION )
                                        {
                                            newItem.setItemMeta( potionMeta );
                                        }

                                        // TODO: should we allow item stacks over .maxStackSize() or should we take up multiple slots?
                                        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem( newItem );

                                        // Check if there are any overflow items that couldn't fit in the players inventory
                                        // and drop them into the world at the players location.
                                        if ( !overflow.isEmpty() )
                                        {
                                            for ( Map.Entry<Integer, ItemStack> overflowSet : overflow.entrySet() )
                                            {
                                                player.getWorld().dropItemNaturally( player.getLocation(), overflowSet.getValue() );
                                            }
                                        }
                                    }
                                    else
                                    {
                                        player.getWorld().dropItemNaturally( player.getLocation(), item1 );
                                    }
                                }
                            }

                            stands.remove( block.getLocation() );
                        }
                    }
                }
            }
        }
    }

    private boolean sameInv( ItemStack[] orig, BrewerInventory new1 )
    {
        for ( int n = 0; n != orig.length; n++ )
        {
            ItemStack i = orig[n];
            if ( i != null )
            {
                //TODO: Check potion item meta more thoroughly, maybe put back in something related to the new damageable item meta instead of the deprecated durability
                if ( new1.contains( i ) && i.getType() == Material.POTION )
                {
                    PotionMeta p1 = ( PotionMeta ) new1.getItem( n ).getItemMeta();
                    PotionMeta p2 = ( PotionMeta ) i.getItemMeta();

                    if ( p1.getCustomEffects().containsAll( p2.getCustomEffects() ) )
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private BrewerInventory check( Location location )
    {
        for ( Location standLocation : stands.keySet() )
        {
            if ( standLocation != null )
            {
                if ( standLocation.getX() == location.getX() && standLocation.getY() == location.getY() && standLocation.getZ() == location.getZ() )
                {
                    return stands.get( standLocation );
                }
            }
        }
        return null;
    }
}
