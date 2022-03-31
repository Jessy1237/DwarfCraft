/*
 * Copyright (c) 2018.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.Jessy1237.DwarfCraft.listeners;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.events.DwarfEffectEvent;
import com.Jessy1237.DwarfCraft.models.effects.DwarfEffect;
import com.Jessy1237.DwarfCraft.models.effects.DwarfEffectType;
import com.Jessy1237.DwarfCraft.models.DwarfPlayer;
import com.Jessy1237.DwarfCraft.models.DwarfSkill;
import com.Jessy1237.DwarfCraft.models.DwarfVehicle;

public class DwarfVehicleListener implements Listener
{
    private final DwarfCraft plugin;

    public DwarfVehicleListener( final DwarfCraft plugin )
    {
        this.plugin = plugin;
    }

    /**
     * Called when a vehicle is destroyed
     * 
     * @param event
     */
    @EventHandler( priority = EventPriority.HIGHEST )
    public void onVehicleDestroy( VehicleDestroyEvent event )
    {
        if ( event.getAttacker() != null )
        {

            if ( !plugin.getUtil().isWorldAllowed( event.getAttacker().getWorld() ) )
                return;

            boolean dropChange = false;

            if ( event.getVehicle() instanceof Boat && event.getAttacker() instanceof Player )
            {

                Player player = ( Player ) event.getAttacker();
                DwarfPlayer dwarfPlayer = plugin.getDataManager().find( player );
                Location loc = event.getVehicle().getLocation();

                for ( DwarfSkill skill : dwarfPlayer.getSkills().values() )
                {
                    for ( DwarfEffect effect : skill.getEffects() )
                    {
                        if ( effect.getEffectType() == DwarfEffectType.VEHICLEDROP )
                        {
                            ItemStack drop = effect.getOutput( dwarfPlayer );

                            DwarfEffectEvent ev = new DwarfEffectEvent( dwarfPlayer, effect, new ItemStack[] { new ItemStack( Material.OAK_BOAT, 1 ) }, new ItemStack[] { drop }, null, null, null, null, event.getVehicle().getVehicle(), null, null );
                            plugin.getServer().getPluginManager().callEvent( ev );

                            if ( ev.isCancelled() ) return;

                            plugin.getUtil().debugLog( 6, Level.FINE, "Debug: dropped " + drop.toString() );

                            for ( ItemStack i : ev.getAlteredItems() )
                            {
                                if ( i != null )
                                {
                                    if ( i.getAmount() > 0 )
                                    {
                                        loc.getWorld().dropItemNaturally( loc, i );
                                    }
                                }
                            }

                            dropChange = true;
                        }
                    }
                }
            }

            if ( dropChange )
            {
                event.getVehicle().remove();
                event.setCancelled( true );
            }
        }
    }

    @EventHandler( priority = EventPriority.NORMAL )
    public void onVehicleEnter( VehicleEnterEvent event )
    {
        if ( !plugin.getUtil().isWorldAllowed( event.getVehicle().getWorld() ) )
            return;

        if ( !( event.getVehicle() instanceof Boat ) )
            return;
        plugin.getDataManager().addVehicle( new DwarfVehicle( event.getVehicle() ) );
        if ( DwarfCraft.debugMessagesThreshold < 6 )
            plugin.getUtil().consoleLog( Level.FINE, "DC6:Added DwarfVehicle to vehicleList" );
    }

    @EventHandler( priority = EventPriority.NORMAL )
    public void onVehicleExit( VehicleExitEvent event )
    {
        plugin.getDataManager().removeVehicle( event.getVehicle() );
    }

    /**
     * Called when a vehicle moves.
     * 
     * @param event
     */
    @SuppressWarnings( "deprecation" )
    @EventHandler( priority = EventPriority.LOWEST )
    public void onVehicleMove( VehicleMoveEvent event )
    {
        if ( !plugin.getUtil().isWorldAllowed( event.getVehicle().getWorld() ) )
            return;

        for ( Entity passenger : event.getVehicle().getPassengers() )
        {
            if ( !( passenger instanceof Player ) || !( event.getVehicle() instanceof Boat ) )
                return;

            DwarfPlayer dCPlayer = plugin.getDataManager().find( (Player) passenger );
            double effectAmount = 1.0;
            DwarfEffect effect = null;
            for ( DwarfSkill s : dCPlayer.getSkills().values() )
            {
                for ( DwarfEffect e : s.getEffects() )
                {
                    if ( e.getEffectType() == DwarfEffectType.VEHICLEMOVE )
                    {
                        effect = e;
                        effectAmount = e.getEffectAmount( dCPlayer );
                    }
                }
            }

            DwarfVehicle dv = plugin.getDataManager().getVehicle( event.getVehicle() );
            if ( dv != null )
            {
                if ( !dv.changedSpeed() )
                {
                    Boat boat = ( Boat ) event.getVehicle();

                    // The original boat speed and altered boat speed are assigned
                    // to the damage variables
                    DwarfEffectEvent e = new DwarfEffectEvent( dCPlayer, effect, null, null, null, null, boat.getMaxSpeed(), boat.getMaxSpeed() * effectAmount, event.getVehicle(), null, null );
                    plugin.getServer().getPluginManager().callEvent( e );
                    if ( !e.isCancelled() )
                    {
                        boat.setMaxSpeed( e.getAlteredDamage() );
                        dv.speedChanged();
                    }
                }
            }
        }
    }
}
