package com.Jessy1237.DwarfCraft.listeners;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Messages;
import com.Jessy1237.DwarfCraft.PlaceHolderParser.PlaceHolder;
import com.Jessy1237.DwarfCraft.events.DwarfEffectEvent;
import com.Jessy1237.DwarfCraft.guis.TrainerGUI;
import com.Jessy1237.DwarfCraft.models.DwarfEffect;
import com.Jessy1237.DwarfCraft.models.DwarfEffectType;
import com.Jessy1237.DwarfCraft.models.DwarfPlayer;
import com.Jessy1237.DwarfCraft.models.DwarfSkill;
import com.Jessy1237.DwarfCraft.models.DwarfTrainer;
import com.Jessy1237.DwarfCraft.schedules.InitTrainerGUISchedule;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class DwarfEntityListener implements Listener
{
    private final DwarfCraft plugin;
    private HashMap<Entity, DwarfPlayer> killMap;

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
                || event.getCause() == DamageCause.LAVA || event.getCause() == DamageCause.DROWNING ) )
        {

            if ( DwarfCraft.debugMessagesThreshold < -1 && !event.isCancelled() )
            {
                System.out.println( "DC-1: Damage Event: " + event.getCause() );
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

    private boolean checkTrainerLeftClick( NPCLeftClickEvent event )
    {
        DwarfTrainer trainer = plugin.getDataManager().getTrainer( event.getNPC() );
        if ( trainer != null )
        {
            if ( event.getClicker() instanceof Player )
            {
                Player player = ( Player ) event.getClicker();
                DwarfPlayer dCPlayer = plugin.getDataManager().find( player );
                DwarfSkill skill = dCPlayer.getSkill( trainer.getSkillTrained() );
                plugin.getOut().printSkillInfo( player, skill, dCPlayer, trainer.getMaxSkill() );

            }
            return true;
        }
        return false;
    }

    private boolean checkDwarfTrainer( NPCRightClickEvent event )
    {
        try
        {
            DwarfPlayer dwarfPlayer = plugin.getDataManager().find( event.getClicker() );
            DwarfTrainer trainer = plugin.getDataManager().getTrainer( event.getNPC() );
            if ( trainer != null )
            {

                if ( trainer.isWaiting() )
                {
                    plugin.getOut().sendMessage( dwarfPlayer.getPlayer(), Messages.trainerOccupied );
                }
                else
                {
                    DwarfSkill skill = dwarfPlayer.getSkill( trainer.getSkillTrained() );

                    if ( skill == null )
                    {
                        plugin.getOut().sendMessage( event.getClicker(), Messages.raceDoesNotContainSkill, plugin.getPlaceHolderParser().parseByDwarfSkill( Messages.trainSkillPrefix, skill ) );
                        return true;
                    }

                    String tag = Messages.trainSkillPrefix.replaceAll( "%skillid%", "" + skill.getId() );
                    if ( dwarfPlayer.getRace().equalsIgnoreCase( "NULL" ) )
                    {
                        plugin.getOut().sendMessage( event.getClicker(), Messages.chooseARace );
                        return true;
                    }

                    if ( dwarfPlayer.getRace().equalsIgnoreCase( "Vanilla" ) )
                    {
                        plugin.getOut().sendMessage( event.getClicker(), Messages.vanillaRace );
                        return true;
                    }

                    if ( skill.getLevel() >= plugin.getConfigManager().getRaceLevelLimit() && !plugin.getConfigManager().getAllSkills( dwarfPlayer.getRace() ).contains( skill.getId() ) )
                    {
                        plugin.getOut().sendMessage( event.getClicker(), Messages.raceDoesNotSpecialize.replaceAll( PlaceHolder.RACE_LEVEL_LIMIT.getPlaceHolder(), "" + plugin.getConfigManager().getRaceLevelLimit() ), tag );
                        return true;
                    }

                    if ( skill.getLevel() >= plugin.getConfigManager().getMaxSkillLevel() )
                    {
                        plugin.getOut().sendMessage( event.getClicker(), Messages.maxSkillLevel.replaceAll( PlaceHolder.MAX_SKILL_LEVEL.getPlaceHolder(), "" + plugin.getConfigManager().getMaxSkillLevel() ), tag );
                        return true;
                    }

                    if ( skill.getLevel() >= trainer.getMaxSkill() )
                    {
                        plugin.getOut().sendMessage( event.getClicker(), Messages.trainerMaxLevel, tag );
                        return true;
                    }

                    if ( skill.getLevel() < trainer.getMinSkill() )
                    {
                        plugin.getOut().sendMessage( event.getClicker(), Messages.trainerLevelTooHigh, tag );
                        return true;
                    }

                    trainer.setWait( true );
                    TrainerGUI trainerGUI = new TrainerGUI( plugin, trainer, dwarfPlayer );
                    plugin.getServer().getScheduler().scheduleSyncDelayedTask( plugin, new InitTrainerGUISchedule( plugin, trainerGUI ), 1 );
                }

                return true;
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return false;
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
            if ( DwarfCraft.debugMessagesThreshold < 0 )
                System.out.println( "DC0: victim is living " );
        }
        else
        {
            if ( DwarfCraft.debugMessagesThreshold < 0 )
                System.out.println( "DC0: victim is unliving " );
            return;
        }

        boolean isPVP = false;
        DwarfPlayer attacker = null;

        if ( victim instanceof Player )
        {
            isPVP = true;
            if ( DwarfCraft.debugMessagesThreshold < 1 )
                System.out.println( "DC1: EDBE is PVP" );
        }

        double damage = event.getDamage();
        double hp = victim.getHealth();
        if ( damager instanceof Player )
        {
            attacker = plugin.getDataManager().find( ( Player ) damager );
            assert ( ( Player ) event.getDamager() == attacker.getPlayer() );
            assert ( attacker != null );
        }
        else
        {// EvP no effects, EvE no effects
            if ( DwarfCraft.debugMessagesThreshold < 4 )
                System.out.println( String.format( "DC4: EVP %s attacked %s for %lf of %d\r\n", damager.getClass().getSimpleName(), victim.getClass().getSimpleName(), damage, hp ) );
            if ( !( event.getEntity() instanceof Player ) )
            {
                event.setDamage( Origdamage );
            }
            return;
        }

        // Need to test PlayerInteractEvent to see if it is called before this
        // event to add player and which item was used to attack this entity.
        ItemStack tool = attacker.getPlayer().getInventory().getItemInMainHand();
        HashMap<Integer, DwarfSkill> skills = attacker.getSkills();

        for ( DwarfSkill s : skills.values() )
        {
            for ( DwarfEffect e : s.getEffects() )
            {
                if ( tool != null && tool.getType().getMaxDurability() > 0 )
                {
                    if ( e.getEffectType() == DwarfEffectType.SWORDDURABILITY && e.checkTool( tool ) )
                        e.damageTool( attacker, 1, tool );

                    if ( e.getEffectType() == DwarfEffectType.TOOLDURABILITY && e.checkTool( tool ) )
                        e.damageTool( attacker, 2, tool );
                }

                if ( e.getEffectType() == DwarfEffectType.PVEDAMAGE && !isPVP && e.checkTool( tool ) )
                {
                    if ( hp <= 0 )
                    {
                        event.setCancelled( true );
                        return;
                    }
                    damage = plugin.getUtil().randomAmount( ( e.getEffectAmount( attacker ) ) * damage );
                    if ( damage >= hp && !killMap.containsKey( victim ) )
                    {
                        killMap.put( victim, attacker );
                    }

                    DwarfEffectEvent ev = new DwarfEffectEvent( attacker, e, null, null, null, null, Origdamage, damage, victim, null, tool );
                    plugin.getServer().getPluginManager().callEvent( ev );

                    if ( ev.isCancelled() )
                    {
                        event.setDamage( Origdamage );
                        return;
                    }

                    event.setDamage( ev.getAlteredDamage() );
                    if ( DwarfCraft.debugMessagesThreshold < 6 )
                    {
                        System.out.println( String.format( "DC6: PVE %s attacked %s for %.2f of %d doing %lf dmg of %lf hp" + " effect called: %d", attacker.getPlayer().getName(), victim.getClass().getSimpleName(), e.getEffectAmount( attacker ), event.getDamage(), damage, hp, e.getId() ) );
                    }
                }

                if ( e.getEffectType() == DwarfEffectType.PVPDAMAGE && isPVP && e.checkTool( tool ) )
                {
                    damage = plugin.getUtil().randomAmount( ( e.getEffectAmount( attacker ) ) * damage );

                    DwarfEffectEvent ev = new DwarfEffectEvent( attacker, e, null, null, null, null, Origdamage, damage, victim, null, tool );

                    if ( ev.isCancelled() )
                    {
                        event.setDamage( Origdamage );
                        return;
                    }

                    event.setDamage( ev.getAlteredDamage() );
                    if ( DwarfCraft.debugMessagesThreshold < 6 )
                    {
                        System.out.println( String.format( "DC6: PVP %s attacked %s for %.2f of %d doing %lf dmg of %lf hp" + " effect called: %d", attacker.getPlayer().getName(), ( ( Player ) victim ).getName(), e.getEffectAmount( attacker ), event.getDamage(), damage, hp, e.getId() ) );
                    }
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
                            if ( DwarfCraft.debugMessagesThreshold < 1 )
                                System.out.println( "DC1: Damage less than fall threshold" );
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
            if ( DwarfCraft.debugMessagesThreshold < 1 )
            {
                System.out.println( String.format( "DC1: environment damage type: %s base damage: %lf new damage: %.2lf\r\n", event.getCause(), event.getDamage(), damage ) );
            }
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

        Entity deadThing = event.getEntity();
        if ( deadThing instanceof Player )
            return;

        boolean changed = false;

        if ( killMap.containsKey( deadThing ) )
        {

            List<ItemStack> items = event.getDrops();

            ItemStack[] normal = new ItemStack[items.size()];
            items.toArray( normal );

            items.clear();

            DwarfPlayer killer = killMap.get( deadThing );
            for ( DwarfSkill skill : killer.getSkills().values() )
            {
                for ( DwarfEffect effect : skill.getEffects() )
                {
                    if ( effect.getEffectType() == DwarfEffectType.MOBDROP )
                    {
                        if ( effect.checkMob( deadThing ) )
                        {
                            ItemStack output = effect.getOutput( killer );

                            if ( deadThing instanceof Sheep && output.getType() == Material.WOOL )
                                output.setDurability( ( short ) ( ( Sheep ) deadThing ).getColor().ordinal() );

                            if ( DwarfCraft.debugMessagesThreshold < 5 )
                            {
                                System.out.println( String.format( "DC5: killed a %s effect called: %d created %d of %s\r\n", deadThing.getClass().getSimpleName(), effect.getId(), output.getAmount(), output.getType().name() ) );
                            }

                            if ( changed == false )
                            {
                                for ( ItemStack i : normal )
                                    items.add( i );
                            }

                            if ( output.getAmount() > 0 )
                            {
                                for ( ItemStack i : normal )
                                {
                                    if ( i.getType() == output.getType() )
                                        items.remove( i );
                                }
                                items.add( output );
                            }

                            changed = true;
                        }
                        else if ( effect.getCreature() == null )
                        {
                            ItemStack output = effect.getOutput( killer );

                            if ( deadThing instanceof Sheep && output.getType() == Material.WOOL )
                                output.setDurability( ( short ) ( ( Sheep ) deadThing ).getColor().ordinal() );

                            if ( DwarfCraft.debugMessagesThreshold < 5 )
                            {
                                System.out.println( String.format( "DC5: killed a %s effect called: %d created %d of %s\r\n", deadThing.getClass().getSimpleName(), effect.getId(), output.getAmount(), output.getType().name() ) );
                            }

                            boolean added = false;
                            for ( ItemStack i : normal )
                            {
                                if ( i.getType() == output.getType() )
                                {
                                    if ( !added )
                                    {
                                        if ( output.getAmount() > 0 )
                                        {
                                            while ( items.contains( i ) )
                                            {
                                                items.remove( i );
                                            }
                                            items.add( output );
                                            added = true;
                                        }
                                    }
                                }
                                else if ( changed == false )
                                {
                                    items.add( i );
                                }
                            }
                            changed = true;
                        }
                        if ( changed )
                        {
                            DwarfEffectEvent ev = new DwarfEffectEvent( killer, effect, normal, items.toArray( new ItemStack[items.size()] ).clone(), null, null, null, null, deadThing, null, null );
                            plugin.getServer().getPluginManager().callEvent( ev );

                            if ( ev.isCancelled() )
                                return;

                            items.clear();
                            for ( ItemStack item : ev.getAlteredItems() )
                            {
                                if ( item != null )
                                {
                                    items.add( item );
                                }
                            }
                        }
                    }
                }
            }
            if ( !changed )
            { // If there was no skill for this type of entity,
              // just give the normal drop.
                for ( ItemStack i : normal )
                    items.add( i );
            }
        }

        killMap.remove( deadThing );
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
