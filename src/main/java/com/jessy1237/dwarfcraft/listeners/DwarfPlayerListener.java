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
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;

import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.Messages;
import com.jessy1237.dwarfcraft.Util;
import com.jessy1237.dwarfcraft.commands.CommandTutorial;
import com.jessy1237.dwarfcraft.events.DwarfEffectEvent;
import com.jessy1237.dwarfcraft.models.DwarfPlayer;
import com.jessy1237.dwarfcraft.models.DwarfSkill;
import com.jessy1237.dwarfcraft.models.DwarfTrainer;
import com.jessy1237.dwarfcraft.models.effects.DwarfEffect;
import com.jessy1237.dwarfcraft.models.effects.DwarfEffectType;

public class DwarfPlayerListener implements Listener
{
    private final DwarfCraft plugin;

    public DwarfPlayerListener( DwarfCraft plugin)
    {
        this.plugin = plugin;
    }

    /**
     * When a player joins the server this initialized their data from the database or creates new info for them. also broadcasts a welcome "player" message
     */
    @EventHandler( priority = EventPriority.NORMAL )
    public void onPlayerJoin( PlayerJoinEvent event )
    {
        plugin.getUtil().setPlayerPrefix( event.getPlayer() );

        DwarfPlayer dwarfPlayer = plugin.getDataManager().find( event.getPlayer() );
        if ( dwarfPlayer.getRace().getId().isEmpty() )
            plugin.getOut().sendMessage( dwarfPlayer.getPlayer(), Messages.chooseARace );

        if ( plugin.getConfigManager().sendGreeting )
            plugin.getOut().welcome( dwarfPlayer );

        // Spawn tutorial book on first join
        if ( !event.getPlayer().hasPlayedBefore() && plugin.getConfigManager().spawnTutorialBook )
        {
            DwarfPlayer dcPlayer = plugin.getDataManager().find( event.getPlayer() );

            // Add Written Book to Players Inventory
            CommandTutorial commandTutorial = new CommandTutorial( "tutorial", plugin );
            HashMap<Integer, ItemStack> overflow = dcPlayer.getPlayer().getInventory().addItem( commandTutorial.createTutorialBook( dcPlayer ) );
            commandTutorial.dropBookIfInventoryFull( dcPlayer.getPlayer(), overflow );

            plugin.getLogger().log( Level.INFO, event.getPlayer().getDisplayName() + " is new to the server! Spawning DwarfCraft Tutorial Book..." );
        }

        if ( dwarfPlayer.isDwarfCraftDev() ) {
            plugin.getServer().getScheduler().runTaskLater( plugin, new AuraSpawnTask( plugin ), 10 );
        }
    }

    /**
     * Called when a player interacts
     * 
     * @param event Relevant event details
     */
    @EventHandler( priority = EventPriority.NORMAL )
    public void onPlayerInteract( PlayerInteractEvent event )
    {
        if ( !plugin.getUtil().isWorldAllowed( event.getPlayer().getWorld() ) )
            return;

        Player player = event.getPlayer();
        DwarfPlayer dwarfPlayer = plugin.getDataManager().find( player );
        HashMap<String, DwarfSkill> skills = dwarfPlayer.getSkills();

        // ItemStack item = player.getItemInHand(); Does this work the same as
        // below?
        ItemStack item = event.getItem();

        // EffectType.PLOWDURABILITY
        if ( event.getAction() == Action.RIGHT_CLICK_BLOCK )
        {
            Block block = event.getClickedBlock();
            Material material = block.getType();

            if ( material == Material.DIRT || material == Material.GRASS )
            {
                for ( DwarfSkill s : skills.values() )
                {
                    for ( DwarfEffect effect : s.getEffectsOfType(DwarfEffectType.PLOWDURABILITY) )
                    {
                        if ( effect.checkTool( item ) )
                        {
                            effect.damageTool( dwarfPlayer, 1, item );
                        }
                    }
                }
            }
        }

        Block block = event.getClickedBlock();

        // EffectType.EAT
        if ( event.getAction() == Action.RIGHT_CLICK_BLOCK )
        {
            for ( DwarfSkill skill : skills.values() )
            {
                for ( DwarfEffect effect : skill.getEffectsOfType(DwarfEffectType.EAT) )
                {
                    if ( effect.checkInitiator( block.getType() ) )
                    {
                        int foodLevel = plugin.getUtil().randomAmount( ( effect.getEffectAmount( dwarfPlayer ) ) );

                        if ( block.getType() == Material.CAKE )
                        {
                            int lvl = Util.FoodLevel.getLvl( block.getType() );

                            if ( lvl == 0 )
                            {
                                return;
                            }

                            DwarfEffectEvent ev = new DwarfEffectEvent( dwarfPlayer, effect, null, null, 2, foodLevel, null, null, null, block, null );
                            plugin.getServer().getPluginManager().callEvent( ev );

                            if ( ev.isCancelled() )
                                return;

                            if ( ( ( player.getFoodLevel() - 2 ) + foodLevel ) > 20 )
                            {
                                event.getPlayer().setFoodLevel( 20 );
                            }
                            else
                            {
                                event.getPlayer().setFoodLevel( ( player.getFoodLevel() - lvl ) + foodLevel );
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler( priority = EventPriority.NORMAL )
    public void onPlayerItemConsume( PlayerItemConsumeEvent event )
    {
        if ( !plugin.getUtil().isWorldAllowed( event.getPlayer().getWorld() ) )
            return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Material mat = item.getType();
        DwarfPlayer dwarfPlayer = plugin.getDataManager().find( player );
        HashMap<String, DwarfSkill> skills = dwarfPlayer.getSkills();
        int lvl = Util.FoodLevel.getLvl( mat );
        
        if ( lvl == 0 )
        {
            return;
        }

        for ( DwarfSkill skill : skills.values() )
        {
            for ( DwarfEffect effect : skill.getEffectsOfType(DwarfEffectType.EAT) )
            {
                if ( effect.checkInitiator( item.getType() ) )
                {
                    int foodLevel = plugin.getUtil().randomAmount( ( effect.getEffectAmount( dwarfPlayer ) ) );

                    DwarfEffectEvent ev = new DwarfEffectEvent( dwarfPlayer, effect, null, null, lvl, foodLevel, null, null, null, null, item );
                    plugin.getServer().getPluginManager().callEvent( ev );

                    if ( ev.isCancelled() )
                        return;

                    if ( ( ( player.getFoodLevel() - 2 ) + foodLevel ) > 20 )
                    {
                        event.getPlayer().setFoodLevel( 20 );
                    }
                    else
                    {
                        event.getPlayer().setFoodLevel( ( player.getFoodLevel() - lvl ) + foodLevel );
                    }
                }
            }
        }
    }

    @EventHandler( priority = EventPriority.NORMAL )
    public void onPlayerShearEntityEvent( PlayerShearEntityEvent event )
    {
        if ( !plugin.getUtil().isWorldAllowed( event.getPlayer().getWorld() ) )
            return;

        Player player = event.getPlayer();
        Entity entity = event.getEntity();
        DwarfPlayer dwarfPlayer = plugin.getDataManager().find( player );
        HashMap<String, DwarfSkill> skills = dwarfPlayer.getSkills();
        boolean changed = false;

        for ( DwarfSkill skill : skills.values() )
        {
            for ( DwarfEffect effect : skill.getEffectsOfType(DwarfEffectType.SHEAR) )
            {
                if ( entity.getType() == EntityType.SHEEP && ( entity.getType() == effect.getEntity() ) )
                {
                    Sheep sheep = ( Sheep ) entity;
                    if ( !sheep.isSheared() )
                    {
                        if ( sheep.isAdult() )
                        {

                            ItemStack item = effect.getOutput( dwarfPlayer );

                            DwarfEffectEvent ev = new DwarfEffectEvent( dwarfPlayer, effect, new ItemStack[] { new ItemStack( item.getType(), 2 ) }, new ItemStack[] { item }, null, null, null, null, entity, null, player.getEquipment().getItemInMainHand() );
                            plugin.getServer().getPluginManager().callEvent( ev );

                            if ( ev.isCancelled() )
                                return;

                            for ( ItemStack i : ev.getAlteredItems() )
                            {
                                if ( i != null )
                                {
                                    if ( i.getAmount() > 0 )
                                    {
                                        entity.getWorld().dropItemNaturally( entity.getLocation(), i );
                                    }
                                }
                            }

                            sheep.setSheared( true );
                            changed = true;
                        }
                    }
                }
                else if ( entity.getType() == EntityType.MUSHROOM_COW && ( entity.getType() == effect.getEntity() ) )
                {
                    MushroomCow mooshroom = ( MushroomCow ) entity;
                    if ( mooshroom.isAdult() )
                    {
                        ItemStack item = effect.getOutput( dwarfPlayer );

                        DwarfEffectEvent ev = new DwarfEffectEvent( dwarfPlayer, effect, new ItemStack[] { new ItemStack( Material.RED_MUSHROOM, 5 ) }, new ItemStack[] { item }, null, null, null, null, entity, null, player.getEquipment().getItemInMainHand() );
                        plugin.getServer().getPluginManager().callEvent( ev );

                        if ( ev.isCancelled() )
                            return;

                        Entity newE = entity.getWorld().spawnEntity( entity.getLocation(), EntityType.COW );
                        Cow cow = ( Cow ) newE;
                        cow.setAge( mooshroom.getAge() );
                        cow.setAdult();
                        cow.setBreed( mooshroom.canBreed() );
                        cow.setAgeLock( mooshroom.getAgeLock() );
                        cow.setHealth( mooshroom.getHealth() );
                        cow.setCustomName( mooshroom.getCustomName() );
                        cow.setCustomNameVisible( mooshroom.isCustomNameVisible() );
                        cow.setTicksLived( mooshroom.getTicksLived() );
                        cow.setTarget( mooshroom.getTarget() );

                        for ( ItemStack i : ev.getAlteredItems() )
                        {
                            if ( i != null )
                            {
                                if ( i.getAmount() > 0 )
                                {
                                    entity.getWorld().dropItemNaturally( entity.getLocation(), i );
                                }
                            }
                        }
                        changed = true;

                        entity.remove();
                    }
                }
            }
        }

        if ( changed )
            event.setCancelled( true );
    }

    /**
     * Called when a player opens an inventory
     * 
     * @param event Relevant event details
     */

    @EventHandler( priority = EventPriority.NORMAL )
    public void onPlayerFish( PlayerFishEvent event )
    {
        if ( !plugin.getUtil().isWorldAllowed( event.getPlayer().getWorld() ) )
            return;

        if ( event.isCancelled() )
            return;

        if ( event.getState() == State.CAUGHT_FISH )
        {
            DwarfPlayer player = plugin.getDataManager().find( event.getPlayer() );
            ItemStack item = ( ( Item ) event.getCaught() ).getItemStack();
            Location loc = player.getPlayer().getLocation();

            ItemStack tool = player.getPlayer().getInventory().getItemInMainHand();
            if ( tool.getType() != Material.FISHING_ROD )
            {
                tool = player.getPlayer().getInventory().getItemInOffHand();
            }

            
                for ( DwarfSkill skill : player.getSkills().values() )
                {
                    for ( DwarfEffect effect : skill.getEffectsOfType(DwarfEffectType.FISH) ) {
                        if ( effect.checkInitiator( item.getType() ) ) {
                            ItemStack drop = effect.getOutput(player);
    
                            DwarfEffectEvent ev = new DwarfEffectEvent(player, effect, new ItemStack[]{item}, new ItemStack[]{drop}, null, null, null, null, null, null, tool);
                            plugin.getServer().getPluginManager().callEvent(ev);
    
                            if (ev.isCancelled())
                                return;
    
                            for (ItemStack i : ev.getAlteredItems()) {
                                if (i != null) {
                                    if (i.getAmount() > 0) {
                                        loc.getWorld().dropItemNaturally(loc, i);
                                    }
                                }
                            }
                            item.setAmount(0);
                        }
                    }
                }

                if ( tool != null && tool.getType().getMaxDurability() > 0 )
                {
                    for ( DwarfSkill skill : player.getSkills().values() )
                    {
                        for ( DwarfEffect effect : skill.getEffectsOfType(DwarfEffectType.RODDURABILITY) )
                        {
                            if ( effect.getEffectType() == DwarfEffectType.RODDURABILITY && effect.checkTool( tool ) )
                                effect.damageTool( player, 1, tool );
                        }
                    }
                }
        }
    }
}

class AuraSpawnTask implements Runnable {

    private DwarfCraft plugin;

    AuraSpawnTask( DwarfCraft plugin )
    {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (DwarfTrainer trainer : plugin.getDataManager().trainerList.values()) {
            trainer.getWorld().spawnParticle( Particle.ENCHANTMENT_TABLE, trainer.getLocation(), 100 );
        }
    }

}

class CancelAuraTask implements Runnable {

    private DwarfCraft plugin;

    CancelAuraTask( DwarfCraft plugin )
    {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (plugin.isAuraActive) {
            plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&cThe aura boosting trainers has ended..."));
            plugin.isAuraActive = false;
        }
    }

}
