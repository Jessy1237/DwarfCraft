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

import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Messages;
import com.Jessy1237.DwarfCraft.Placeholder;
import com.Jessy1237.DwarfCraft.events.DwarfEffectEvent;
import com.Jessy1237.DwarfCraft.guis.TrainerGUI;
import com.Jessy1237.DwarfCraft.models.*;
import com.Jessy1237.DwarfCraft.models.effects.DwarfEffect;
import com.Jessy1237.DwarfCraft.models.effects.DwarfEffectType;
import com.Jessy1237.DwarfCraft.schedules.InitTrainerGUISchedule;

public class DwarfEntityListener implements Listener
{
    private final DwarfCraft plugin;
    private final HashMap<Entity, DwarfPlayer> killMap;

    public DwarfEntityListener( DwarfCraft plugin )
    {
        this.plugin = plugin;
        killMap = new HashMap<>();
    }

    @EventHandler( priority = EventPriority.HIGH )
    public void onEntityDamage( EntityDamageEvent event )
    {
        if ( !plugin.getUtil().isWorldAllowed( event.getEntity().getWorld() ) )
            return;

        if ( event.isCancelled() )
            return;

        if ( ( event.getCause() == DamageCause.BLOCK_EXPLOSION || event.getCause() == DamageCause.ENTITY_EXPLOSION || event.getCause() == DamageCause.FALL || event.getCause() == DamageCause.SUFFOCATION || event.getCause() == DamageCause.FIRE || event.getCause() == DamageCause.FIRE_TICK
                || event.getCause() == DamageCause.LAVA || event.getCause() == DamageCause.DROWNING || event.getCause() == DamageCause.CONTACT || event.getCause() == DamageCause.FALLING_BLOCK ) )
        {
            if ( !event.isCancelled() )
            {
                plugin.getUtil().debugLog( -1, Level.FINE, "Damage Event: " + event.getCause() );
            }
            onEntityDamagedByEnvirons( event );

        }
        else if ( event instanceof EntityDamageByEntityEvent )
        {
            EntityDamageByEntityEvent nevent = ( EntityDamageByEntityEvent ) event;
            if ( ( nevent.getDamager() instanceof Arrow ) )
            {
                onEntityDamageByProjectile( nevent );
            }
            else
            {
                onEntityAttack( nevent );
            }
        }
    }

    private
    void checkTrainerLeftClick( NPCLeftClickEvent event )
    {
        DwarfTrainer trainer = plugin.getDataManager().getTrainer( event.getNPC() );
        if ( trainer != null )
        {
            Player player = event.getClicker();
            DwarfPlayer dCPlayer = plugin.getDataManager().find( player );
            DwarfSkill skill = dCPlayer.getSkill( trainer.getSkillTrained() );
            if (skill == null) return;

            if ( dCPlayer.getRace().getId().equals( "" ) )
            {
                plugin.getOut().sendMessage( event.getClicker(), Messages.chooseARace );
                return;
            }

            plugin.getOut().printSkillInfo( player, skill, dCPlayer, trainer.getMaxSkill() );

        }
    }

    private
    void checkDwarfTrainer( NPCRightClickEvent event )
    {
        try
        {
            DwarfPlayer dwarfPlayer = plugin.getDataManager().find( event.getClicker() );
            DwarfTrainer trainer = plugin.getDataManager().getTrainer( event.getNPC() );
            if ( trainer != null )
            {

                if ( trainer.isWaiting() )
                {
                    dwarfPlayer.getPlayer().spigot().sendMessage( ChatMessageType.ACTION_BAR, new TextComponent( ChatColor.translateAlternateColorCodes( '&', Messages.trainerOccupied ) ) );
                }
                else
                {
                    DwarfSkill skill = dwarfPlayer.getSkill( trainer.getSkillTrained() );

                    if ( skill == null )
                    {
                        dwarfPlayer.getPlayer().spigot().sendMessage( ChatMessageType.ACTION_BAR, new TextComponent( ChatColor.translateAlternateColorCodes( '&', Messages.raceDoesNotContainSkill ) ) );
                        return;
                    }

                    if ( dwarfPlayer.getRace().getId().equals( "" ) )
                    {
                        dwarfPlayer.getPlayer().spigot().sendMessage( ChatMessageType.ACTION_BAR, new TextComponent( ChatColor.translateAlternateColorCodes( '&', Messages.chooseARace ) ) );
                        return;
                    }

                    if ( dwarfPlayer.getRace().getId().equalsIgnoreCase( "vanilla" ) )
                    {
                        dwarfPlayer.getPlayer().spigot().sendMessage( ChatMessageType.ACTION_BAR, new TextComponent( ChatColor.translateAlternateColorCodes( '&', Messages.vanillaRace ) ) );
                        return;
                    }

                    if ( skill.getLevel() >= plugin.getConfigManager().getRaceLevelLimit() && !skill.doesSpecialize( dwarfPlayer.getRace() ) )
                    {
                        dwarfPlayer.getPlayer().spigot().sendMessage( ChatMessageType.ACTION_BAR, new TextComponent( ChatColor.translateAlternateColorCodes( '&', Messages.raceDoesNotSpecialize.replaceAll( Placeholder.RACE_LEVEL_LIMIT.value(), "" + plugin.getConfigManager().getRaceLevelLimit() ) ) ) );
                        return;
                    }

                    if ( skill.getLevel() >= plugin.getConfigManager().getMaxSkillLevel() )
                    {
                        dwarfPlayer.getPlayer().spigot().sendMessage( ChatMessageType.ACTION_BAR, new TextComponent( ChatColor.translateAlternateColorCodes( '&', Messages.maxSkillLevel.replaceAll( Placeholder.SKILL_MAX_LEVEL.value(), "" + plugin.getConfigManager().getMaxSkillLevel() ) ) ) );
                        return;
                    }

                    if ( skill.getLevel() >= trainer.getMaxSkill() )
                    {
                        dwarfPlayer.getPlayer().spigot().sendMessage( ChatMessageType.ACTION_BAR, new TextComponent( ChatColor.translateAlternateColorCodes( '&', Messages.trainerMaxLevel ) ) );
                        return;
                    }

                    if ( skill.getLevel() < trainer.getMinSkill() )
                    {
                        dwarfPlayer.getPlayer().spigot().sendMessage( ChatMessageType.ACTION_BAR, new TextComponent( ChatColor.translateAlternateColorCodes( '&', Messages.trainerLevelTooHigh ) ) );
                        return;
                    }

                    trainer.setWait( true );
                    TrainerGUI trainerGUI = new TrainerGUI( plugin, trainer, dwarfPlayer );
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask( plugin, new InitTrainerGUISchedule( plugin, trainerGUI ), 1 );
                }

            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    public void onEntityAttack( EntityDamageByEntityEvent event )
    {
        if ( !plugin.getUtil().isWorldAllowed( event.getDamager().getWorld() ) )
            return;

        if ( ( plugin.getDataManager().isTrainer( event.getEntity() ) ) && event.getEntity() instanceof HumanEntity )
        {
            event.setDamage( 0 );
            return;
        }

        if ( !( event.getDamager() instanceof Player ) )
        {
            return;
        }

        double Origdamage = event.getDamage();
        Entity damager = event.getDamager();
        LivingEntity victim;

        if ( event.getEntity() instanceof LivingEntity )
        {
            victim = ( LivingEntity ) event.getEntity();
            plugin.getUtil().debugLog( 0, Level.FINE, "DC0: victim is living " );
        }
        else
        {
            plugin.getUtil().debugLog( 0, Level.FINE, "DC0: victim is unliving " );
            return;
        }

        boolean isPVP = false;
        DwarfPlayer attacker;

        if ( victim instanceof Player )
        {
            isPVP = true;
            plugin.getUtil().debugLog( 1,  Level.FINE, "DC1: EDBE is PVP" );
        }

        double damage = event.getDamage();
        double hp = victim.getHealth();
        if ( damager instanceof Player )
        {
            attacker = plugin.getDataManager().find( ( Player ) damager );
            assert ( event.getDamager() == attacker.getPlayer() );
        }
        else
        {
            // EvP no effects, EvE no effects
            plugin.getUtil().debugLog( 4, Level.FINE, String.format( "DC4: EVP %s attacked %s for %f of %d\r\n", damager.getClass().getSimpleName(), victim.getClass().getSimpleName(), damage, hp ) );
            if ( !( event.getEntity() instanceof Player ) )
            {
                event.setDamage( Origdamage );
            }
            return;
        }

        // Need to test PlayerInteractEvent to see if it is called before this
        // event to add player and which item was used to attack this entity.
        ItemStack tool = attacker.getPlayer().getInventory().getItemInMainHand();
        HashMap<String, DwarfSkill> skills = attacker.getSkills();

        for ( DwarfSkill s : skills.values() )
        {
            for ( DwarfEffect e : s.getEffects() ) {
                if (tool.getType().getMaxDurability() > 0) {
                    if (e.getEffectType() == DwarfEffectType.SWORDDURABILITY && e.checkTool(tool))
                        e.damageTool(attacker, 1, tool);
    
                    if (e.getEffectType() == DwarfEffectType.TOOLDURABILITY && e.checkTool(tool))
                        e.damageTool(attacker, 2, tool);
                }

                if (e.getEffectType() == DwarfEffectType.PVEDAMAGE && !isPVP && e.checkTool(tool)) {
                    if (hp <= 0) {
                        event.setCancelled(true);
                        return;
                    }
                    damage = plugin.getUtil().randomAmount((e.getEffectAmount(attacker)) * damage);
                    if (damage >= hp && !killMap.containsKey(victim)) {
                        killMap.put(victim, attacker);
                    }
    
                    DwarfEffectEvent ev = new DwarfEffectEvent(attacker, e, null, null, null, null, Origdamage, damage, victim, null, tool);
                    plugin.getServer().getPluginManager().callEvent(ev);
    
                    if (ev.isCancelled()) {
                        event.setDamage(Origdamage);
                        return;
                    }
    
                    event.setDamage(ev.getAlteredDamage());
                    plugin.getUtil().debugLog(6, Level.FINE, String.format("DC6: PVE %s attacked %s for %.2f of %d doing %f dmg of %f hp", attacker.getPlayer().getName(), victim.getClass().getSimpleName(), e.getEffectAmount(attacker), event.getDamage(), damage, hp));
                }

                if (e.getEffectType() == DwarfEffectType.PVPDAMAGE && isPVP && e.checkTool(tool)) {
                    damage = plugin.getUtil().randomAmount((e.getEffectAmount(attacker)) * damage);
    
                    DwarfEffectEvent ev = new DwarfEffectEvent(attacker, e, null, null, null, null, Origdamage, damage, victim, null, tool);
    
                    if (ev.isCancelled()) {
                        event.setDamage(Origdamage);
                        return;
                    }
    
                    event.setDamage(ev.getAlteredDamage());
                    plugin.getUtil().debugLog(6, Level.FINE, String
                            .format("DC6: PVP %s attacked %s for %.2f of %d doing %f dmg of %f hp", attacker.getPlayer().getName(), victim.getName(), e.getEffectAmount(attacker), event.getDamage(), damage, hp));
                }
            }
        }
    }

    public void onEntityDamageByProjectile( EntityDamageByEntityEvent event )
    {
        if ( !plugin.getUtil().isWorldAllowed( event.getDamager().getWorld() ) )
            return;

        if ( ( plugin.getDataManager().isTrainer( event.getEntity() ) ) && event.getEntity() instanceof HumanEntity )
        {
            event.setDamage( 0 );
            return;
        }

        Arrow arrow = ( Arrow ) event.getDamager();
        ProjectileSource attacker = arrow.getShooter();
        if ( !( event.getEntity() instanceof LivingEntity ) )
        {
            return;
        }

        LivingEntity hitThing = ( LivingEntity ) event.getEntity();

        double hp = hitThing.getHealth();
        if ( hp <= 0 )
        {
            event.setCancelled( true );
            return;
        }
        double damage = event.getDamage();
        final double origDamage = event.getDamage();
        double mitigation = 1;
        DwarfPlayer attackDwarf = null;

        if ( attacker instanceof Player )
        {
            attackDwarf = plugin.getDataManager().find( ( Player ) attacker );
            for ( DwarfSkill skill : attackDwarf.getSkills().values() )
            {
                for ( DwarfEffect effect : skill.getEffects() )
                {
                    if ( effect.getEffectType() == DwarfEffectType.BOWATTACK )
                    {
                        damage = effect.getEffectAmount( attackDwarf );

                        DwarfEffectEvent ev = new DwarfEffectEvent( attackDwarf, effect, null, null, null, null, origDamage, damage, hitThing, null, null );
                        plugin.getServer().getPluginManager().callEvent( ev );

                        if ( ev.isCancelled() )
                        {
                            event.setDamage( origDamage );
                            return;
                        }
                    }
                }
            }
        }

        damage = plugin.getUtil().randomAmount( ( damage * mitigation ) + ( origDamage / 4 ) );
        event.setDamage( damage );
        if ( damage >= hp && attacker instanceof Player && !killMap.containsKey( hitThing ) && !( hitThing instanceof Player ) )
        {
            killMap.put( hitThing, attackDwarf );
        }
    }

    public void onEntityDamagedByEnvirons( EntityDamageEvent event )
    {
        if ( !plugin.getUtil().isWorldAllowed( event.getEntity().getWorld() ) )
            return;

        if ( ( event.getEntity() instanceof Player ) )
        {
            DwarfPlayer dCPlayer = plugin.getDataManager().find( ( Player ) event.getEntity() );
            double damage = event.getDamage();
            final double origDamage = event.getDamage();
            for ( DwarfSkill s : dCPlayer.getSkills().values() )
            {
                for ( DwarfEffect e : s.getEffects() )
                {
                    if ( e.getEffectType() == DwarfEffectType.FALLDAMAGE && event.getCause() == DamageCause.FALL )
                        damage = plugin.getUtil().randomAmount( e.getEffectAmount( dCPlayer ) * damage );
                    else if ( e.getEffectType() == DwarfEffectType.FIREDAMAGE && event.getCause() == DamageCause.FIRE )
                        damage = plugin.getUtil().randomAmount( e.getEffectAmount( dCPlayer ) * damage );
                    else if ( e.getEffectType() == DwarfEffectType.FIREDAMAGE && event.getCause() == DamageCause.FIRE_TICK )
                        damage = plugin.getUtil().randomAmount( e.getEffectAmount( dCPlayer ) * damage );
                    else if ( e.getEffectType() == DwarfEffectType.EXPLOSIONDAMAGE && event.getCause() == DamageCause.ENTITY_EXPLOSION )
                        damage = plugin.getUtil().randomAmount( e.getEffectAmount( dCPlayer ) * damage );
                    else if ( e.getEffectType() == DwarfEffectType.EXPLOSIONDAMAGE && event.getCause() == DamageCause.BLOCK_EXPLOSION )
                        damage = plugin.getUtil().randomAmount( e.getEffectAmount( dCPlayer ) * damage );

                    if ( e.getEffectType() == DwarfEffectType.FALLTHRESHOLD && event.getCause() == DamageCause.FALL )
                    {
                        if ( event.getDamage() <= e.getEffectAmount( dCPlayer ) )
                        {
                            plugin.getUtil().debugLog( 1, Level.FINE, "Damage less than fall threshold" );
                            event.setCancelled( true );
                        }
                    }

                    DwarfEffectEvent ev = new DwarfEffectEvent( dCPlayer, e, null, null, null, null, origDamage, damage, null, null, null );
                    plugin.getServer().getPluginManager().callEvent( ev );

                    if ( ev.isCancelled() )
                    {
                        event.setDamage( origDamage );
                        return;
                    }

                    damage = ev.getAlteredDamage();
                }
            }
            plugin.getUtil().debugLog( 1, Level.FINE, String.format( "Environment damage type: %s base damage: %f new damage: %.2f\r\n", event.getCause(), event.getDamage(), damage ) );
            event.setDamage( damage );
            if ( damage == 0 )
                event.setCancelled( true );
        }
    }

    @EventHandler( priority = EventPriority.LOW )
    public void onEntityDeath( EntityDeathEvent event )
    {
        if ( !plugin.getUtil().isWorldAllowed( event.getEntity().getWorld() ) )
            return;

        Entity entity = event.getEntity();
        if ( entity instanceof Player )
            return;

        if ( killMap.containsKey( entity ) )
        {
            DwarfPlayer killer = killMap.get( entity );
            for ( DwarfSkill skill : killer.getSkills().values() )
            {
                for ( DwarfEffect effect : skill.getEffects() )
                {
                    if ( effect.getEffectType() == DwarfEffectType.MOBDROP)
                    {
                        ItemStack result = effect.getOutput( killer );

                        // In the event that no initiator creature is given,
                        // check all drops to see if they match an effects result
                        // and modify the drop amount.
                        if ( effect.getEntity() == null )
                        {
                            int index = 0;
                            for ( ItemStack drop : event.getDrops() )
                            {
                                if ( ( effect.getOutput().isTagged() && effect.getOutput().getMaterials().contains( drop.getType() ) || effect.getOutput().getItemStack().getType() == drop.getType() ) )
                                {
                                    plugin.getUtil().debugLog( 5, Level.FINE, String.format( "DC5: killed a %s created %d of %s\r\n", entity.getClass().getSimpleName(), result.getAmount(), result.getType().name() ) );

                                    DwarfEffectEvent ev = new DwarfEffectEvent( killer, effect, new ItemStack[] { drop }, new ItemStack[] { result }, null, null, null, null, entity, null, null );
                                    plugin.getServer().getPluginManager().callEvent( ev );

                                    if ( !ev.isCancelled() )
                                        event.getDrops().get( index ).setAmount( result.getAmount() );
                                }
                                index++;
                            }
                        }
                        else if ( ( effect.getEntity() != null && ( entity.getType() == effect.getEntity() ) ) )
                        {
                            plugin.getUtil().debugLog( 5, Level.FINE, String.format( "DC5: killed a %s created %d of %s\r\n", entity.getClass().getSimpleName(), result.getAmount(), result.getType().name() ) );

                            ItemStack[] original = event.getDrops().toArray( new ItemStack[0] );
                            DwarfEffectEvent ev = new DwarfEffectEvent( killer, effect, original, new ItemStack[] { result }, null, null, null, null, entity, null, null );
                            plugin.getServer().getPluginManager().callEvent( ev );

                            if ( ev.isCancelled() )
                                return;

                            event.getDrops().clear();
                            if ( entity instanceof Sheep )
                            {
                                ItemStack item = effect.getOutput( killer );
                                item.setType( original[0].getType() );
                                event.getDrops().add( item );
                            }
                            else
                            {
                                event.getDrops().add( effect.getOutput( killer ) );
                            }
                        }
                    }
                }
            }
        }

        killMap.remove( entity );
    }

    public void onNPCLeftClickEvent( NPCLeftClickEvent event )
    {
        checkTrainerLeftClick( event );
    }

    // Replaced EntityTarget Event since 1.5.1
    public void onNPCRightClickEvent( NPCRightClickEvent event )
    {
        checkDwarfTrainer( event );
    }
}
