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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.events.DwarfEffectEvent;
import com.jessy1237.dwarfcraft.models.DwarfPlayer;
import com.jessy1237.dwarfcraft.models.DwarfSkill;
import com.jessy1237.dwarfcraft.models.effects.DwarfEffect;
import com.jessy1237.dwarfcraft.models.effects.DwarfEffectType;

public class DwarfBlockListener implements Listener
{
    private final DwarfCraft plugin;
    private HashMap<Block, Player> crops = new HashMap<Block, Player>();

    public DwarfBlockListener( DwarfCraft plugin )
    {
        this.plugin = plugin;
    }

    @EventHandler( priority = EventPriority.HIGH )
    public void onBlockFromTo( BlockFromToEvent event )
    {
        if ( plugin.getConfigManager().disableCacti )
        {
            if ( !plugin.getUtil().isWorldAllowed( event.getBlock().getWorld() ) )
                return;

            // Code to prevent water from normally breaking crops
            // Added due to players being able to bypass DC skill restrictions
            if ( event.getToBlock().getType() == Material.WHEAT || event.getToBlock().getType() == Material.POTATO || event.getToBlock().getType() == Material.CARROT || event.getToBlock().getType() == Material.SUGAR_CANE || event.getToBlock().getType() == Material.CACTUS
                    || event.getToBlock().getType() == Material.COCOA || event.getToBlock().getType() == Material.NETHER_WART && ( event.getBlock().getType() == Material.WATER || event.getBlock().getType() == Material.WATER ) )
            {
                event.getToBlock().setType( Material.AIR, true );
                event.setCancelled( true );
            }
        }
    }

    // Checks when a cactus grows to see if it trying to be auto farmed
    @EventHandler( priority = EventPriority.HIGH )
    public void onBlockGrow( BlockGrowEvent event )
    {
        if ( plugin.getConfigManager().disableCacti )
        {
            if ( !plugin.getUtil().isWorldAllowed( event.getBlock().getWorld() ) )
                return;

            Block b = event.getNewState().getBlock();
            if ( event.getBlock().getType() == Material.CACTUS )
            {
                if ( !checkCacti( b.getWorld(), b.getLocation() ) )
                {
                    b.setType( Material.AIR );
                    event.setCancelled( true );
                }
                else
                {
                    b = event.getBlock();
                    if ( !checkCacti( b.getWorld(), b.getLocation() ) )
                    {
                        b.setType( Material.AIR );
                        event.setCancelled( true );
                    }
                    else
                    {
                        Location l = b.getLocation();
                        l.setY( l.getBlockY() + 1 );
                        if ( !checkCacti( b.getWorld(), l ) )
                        {
                            b.getWorld().getBlockAt( l ).setType( Material.AIR );
                            event.setCancelled( true );
                        }
                    }
                }
            }
        }
    }

    @EventHandler( priority = EventPriority.HIGH )
    public void onBlockBreak( BlockBreakEvent event )
    {
        if ( event.isCancelled() || event.getPlayer().getGameMode() == GameMode.CREATIVE || !plugin.getUtil().isWorldAllowed( event.getPlayer().getWorld() ) )
        {
            return;
        }

        DwarfPlayer player = plugin.getDataManager().find( event.getPlayer() );
        HashMap<String, DwarfSkill> skills = player.getSkills();

        ItemStack tool = player.getPlayer().getInventory().getItemInMainHand();
        Block block = event.getBlock();
        Location loc = event.getBlock().getLocation();
        Material blockMat = event.getBlock().getType();
        
        boolean blockDropChange = false;
        for ( DwarfEffect effect : plugin.getEffectRegistry().getEffectsOfType( DwarfEffectType.BLOCKDROP ) ) {
            if ( effect.checkInitiator( blockMat ) ) {
                // Check if the block was placed by a player and prevent additional drops if the effect type is not "BLOCKDROPDUPE"
                //todo: add support for dupe
                //if (effect.isAllowDupe() && event.getBlock().hasMetadata("playerPlaced")) return;
    
                //Checks for any ageable block to make sure we are only acting when it is fully aged
                if (block.getBlockData() instanceof Ageable) {
                    Ageable a = (Ageable) block.getBlockData();
                    if (a.getAge() != a.getMaximumAge())
                        return;
                }
    
                // Checks for cactus/sugar cane blocks above the one
                // broken to apply the dwarfcraft blocks in the block physics
                // event.
                if (block.getType() == Material.CACTUS) {
                    for (int i = 1; block.getWorld().getBlockAt(block.getX(), block.getY() + i, block.getZ()).getType() == Material.CACTUS; i++) {
                        crops.put(block.getWorld().getBlockAt(block.getX(), block.getY() + i, block.getZ()), event.getPlayer());
                    }
                }
    
                if (block.getType() == Material.SUGAR_CANE) {
                    for (int i = 1; block.getWorld().getBlockAt(block.getX(), block.getY() + i, block.getZ()).getType() == Material.SUGAR_CANE; i++) {
                        crops.put(block.getWorld().getBlockAt(block.getX(), block.getY() + i, block.getZ()), event.getPlayer());
                    }
                }
    
                if (effect.checkTool(tool)) {
                    ItemStack item = effect.getOutput(player);
                    ItemStack item1 = null;
        
                    // Gives the 2% to drop poisonous potatoes when
                    // potatoes are broken
                    if (effect.getInitiator().getItemStack().getType() == Material.POTATO && item.getType() == Material.POTATO) {
                        Random r = new Random();
                        final int i = r.nextInt(100);
                        if (i == 0 || i == 1) {
                            loc.getWorld().dropItem(loc, new ItemStack(Material.POISONOUS_POTATO, 1));
                        }
                    }
        
                    if (tool.containsEnchantment(Enchantment.SILK_TOUCH)) {
                        // If enabled in the config, silk touch block
                        // replaces one of the items drop in the stack, if
                        // not acts as vanilla and no DC drops
                        if (plugin.getConfigManager().silkTouch) {
                            item.setAmount(item.getAmount() - plugin.getUtil().randomAmount(effect.getEffectAmount(effect.getNormalLevel(), player)));
                            switch (block.getType()) {
                                case STONE:
                                case DIAMOND_ORE:
                                case EMERALD_ORE:
                                case NETHER_QUARTZ_ORE:
                                case COAL_ORE:
                                case REDSTONE_ORE:
                                case GLOWSTONE:
                                case GRASS:
                                case LAPIS_ORE:
                                    item1 = new ItemStack(block.getType(), 1);
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            switch (block.getType()) {
                                case STONE:
                                case DIAMOND_ORE:
                                case EMERALD_ORE:
                                case NETHER_QUARTZ_ORE:
                                case COAL_ORE:
                                case REDSTONE_ORE:
                                case GLOWSTONE:
                                case GRASS:
                                case LAPIS_ORE:
                                    item = new ItemStack(block.getType(), 1);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
        
                    // Checks for Fortune tools and adds it to the
                    // Dwarfcraft drops
                    Material type = block.getType();
                    if (type == Material.DIAMOND_ORE || type == Material.COAL_ORE || type == Material.REDSTONE_ORE || type == Material.EMERALD_ORE || type == Material.NETHER_QUARTZ_ORE || type == Material.GRASS || type == Material.STONE || type == Material.LAPIS_ORE
                            || type == Material.GLOWSTONE) {
                        if (tool.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
                            int lvl = tool.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
                            Random r = new Random();
                            int num = r.nextInt(99) + 1;
                            switch (lvl) {
                                case 1:
                                    if (1 <= num && num <= 33) {
                                        item.setAmount(item.getAmount() + 1);
                                    }
                                    break;
                                case 2:
                                    if (1 <= num && num <= 25) {
                                        item.setAmount(item.getAmount() + 1);
                                    } else if (26 <= num && num <= 50) {
                                        item.setAmount(item.getAmount() + 2);
                                    }
                                    break;
                                case 3:
                                    if (1 <= num && num <= 20) {
                                        item.setAmount(item.getAmount() + 1);
                                    } else if (21 <= num && num <= 40) {
                                        item.setAmount(item.getAmount() + 2);
                                    } else if (41 <= num && num <= 60) {
                                        item.setAmount(item.getAmount() + 2);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                    DwarfEffectEvent e;
                    ItemStack[] altered = new ItemStack[2];
        
                    if (item.getAmount() > 0)
                        altered[0] = item;
                    if (item1 != null)
                        altered[1] = item1;
        
                    ItemStack[] orig = new ItemStack[block.getDrops().size()];
                    block.getDrops().toArray(orig);
        
                    e = new DwarfEffectEvent(player, effect, orig, altered, null, null, null, null, null, block, null);
                    plugin.getServer().getPluginManager().callEvent(e);
        
                    if (e.isCancelled())
                        return;
                    
                    plugin.getUtil().debugLog(6, Level.FINE, "Affected ItemStack: " + item.getType() + " (x" + item.getAmount() + ")");
        
                    for (ItemStack i : e.getAlteredItems()) {
                        if (i != null) {
                            if (i.getAmount() > 0) {
                                loc.getWorld().dropItem(loc.add(0.5, 0, 0.5), i).setVelocity(new Vector(0, 0.15, 0));
                                loc.getWorld().playEffect(loc.add(0.5, 0, 0.5), Effect.BONE_MEAL_USE, 5);
                            }
                        }
                    }
        
                    if (event.getExpToDrop() != 0) {
                        (loc.getWorld().spawn(loc, ExperienceOrb.class)).setExperience(event.getExpToDrop());
                    }
        
                    blockDropChange = true;
                }
    
                // todo: support passing in multiple effect types. this wont be reached
                if (tool.getType().getMaxDurability() > 0) {
                    if (effect.getEffectType() == DwarfEffectType.SWORDDURABILITY && effect.checkTool(tool))
                        effect.damageTool(player, 2, tool, !blockDropChange);
        
                    if (effect.getEffectType() == DwarfEffectType.TOOLDURABILITY && effect.checkTool(tool))
                        effect.damageTool(player, 1, tool, !blockDropChange);
                }
            }
        }

        if ( tool.getType().getMaxDurability() > 0 )
        {
            for ( DwarfSkill s : skills.values() )
            {
                for ( DwarfEffect e : s.getEffects() )
                {
                    if (e.getEffectType() == DwarfEffectType.SWORDDURABILITY && e.checkTool(tool))
                        e.damageTool(player, 2, tool, !blockDropChange);

                    if (e.getEffectType() == DwarfEffectType.TOOLDURABILITY && e.checkTool(tool))
                        e.damageTool(player, 1, tool, !blockDropChange);
                }
            }
        }

        if ( blockDropChange )
        {
            event.setDropItems( false );
            event.setExpToDrop( 0 );
        }
    }

    /**
     * onBlockDamage used to accelerate how quickly blocks are destroyed. setDamage() not implemented yet
     */
    @EventHandler( priority = EventPriority.NORMAL )
    public void onBlockDamage( BlockDamageEvent event )
    {
        if ( !plugin.getUtil().isWorldAllowed( event.getPlayer().getWorld() ) )
            return;

        if ( event.isCancelled() )
            return;

        Player player = event.getPlayer();
        DwarfPlayer dCPlayer = plugin.getDataManager().find( player );
        HashMap<String, DwarfSkill> skills = dCPlayer.getSkills();

        // Effect Specific information
        ItemStack tool = player.getInventory().getItemInMainHand();
        Material mat = event.getBlock().getType();
        
        for ( DwarfEffect effect : plugin.getEffectRegistry().getEffectsOfType( DwarfEffectType.DIGTIME ) )
        {
            if ( effect.checkInitiator( mat ) && effect.checkTool( tool ) )
            {
                plugin.getUtil().debugLog( 2, Level.FINE, "Started instamine check" );

                if ( plugin.getUtil().randomAmount( effect.getEffectAmount( dCPlayer ) ) == 0 )
                    return;

                plugin.getUtil().debugLog( 3, Level.FINE, "Insta-mine occured. Block: " + mat );

                DwarfEffectEvent ev = new DwarfEffectEvent( dCPlayer, effect, null, null, null, null, null, null, null, event.getBlock(), null );
                plugin.getServer().getPluginManager().callEvent( ev );

                if ( ev.isCancelled() )
                    return;

                event.setInstaBreak( true );
            }
        }
    }

    @EventHandler( priority = EventPriority.HIGH )
    public void onBlockPlace( BlockPlaceEvent event )
    {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        block.setMetadata( "playerPlaced", new FixedMetadataValue( plugin, player.getUniqueId() ) );
    }

    // Code to check for farm automation i.e. (breaking the
    // block below, cacti farms, etc)
    @EventHandler( priority = EventPriority.HIGH )
    public void onBlockPhysics( BlockPhysicsEvent event )
    {
        if ( plugin.getConfigManager().disableCacti )
        {
            if ( !plugin.getUtil().isWorldAllowed( event.getBlock().getWorld() ) )
                return;

            if ( event.getBlock().getType() == Material.WHEAT || event.getBlock().getType() == Material.POTATO || event.getBlock().getType() == Material.CARROT )
            {

                World world = event.getBlock().getWorld();
                Location loc = event.getBlock().getLocation();
                if ( !( checkCrops( world, loc ) ) )
                {
                    event.getBlock().setType( Material.AIR, true );
                    event.setCancelled( true );
                }
            }
            else if ( event.getBlock().getType() == Material.CACTUS )
            {
                World world = event.getBlock().getWorld();
                Location loc = event.getBlock().getLocation();
                if ( !( checkCacti( world, loc ) ) )
                {
                    int x = loc.getBlockX();
                    int y = loc.getBlockY();
                    int z = loc.getBlockZ();

                    boolean remove = false;
                    boolean checked = false;
                    ArrayList<Block> removal = new ArrayList<Block>();
                    for ( Block b : crops.keySet() )
                    {
                        if ( b != null )
                        {
                            if ( b.getX() == x && b.getY() == y && b.getZ() == z )
                            {
                                DwarfPlayer dCPlayer = plugin.getDataManager().find( crops.get( b ) );
                                for ( DwarfEffect effect : plugin.getEffectRegistry().getEffectsOfType( DwarfEffectType.BLOCKDROP ) ) {
                                    if ( effect.checkInitiator( Material.CACTUS ) ) {
                                        int amount = plugin.getUtil().randomAmount(effect.getEffectAmount(dCPlayer));
                                        if (amount != 0) {
    
                                            DwarfEffectEvent ev;
                                            ItemStack[] altered = new ItemStack[1];
                                            altered[0] = new ItemStack(Material.CACTUS, amount);
    
                                            ItemStack[] orig = new ItemStack[event.getBlock().getDrops().size()];
                                            event.getBlock().getDrops().toArray(orig);
    
                                            ev = new DwarfEffectEvent(dCPlayer, effect, orig, altered, null, null, null, null, null, event.getBlock(), null);
                                            plugin.getServer().getPluginManager().callEvent(ev);
    
                                            if (ev.isCancelled())
                                                return;
    
                                            for (ItemStack i : ev.getAlteredItems()) {
                                                if (i != null) {
                                                    if (i.getAmount() > 0) {
                                                        world.dropItemNaturally(loc, i);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                removal.add( b );
                                remove = true;
                                checked = true;
                            }
                        }
                    }

                    for ( Block b : removal )
                    {
                        if ( b != null )
                        {
                            crops.remove( b );
                        }
                    }
                    if ( remove )
                    {
                        event.getBlock().setType( Material.AIR, true );
                        event.setCancelled( true );
                    }
                    else if ( !checked )
                    {
                        event.getBlock().setType( Material.AIR, true );
                        event.setCancelled( true );
                    }
                }
            }
            else if ( event.getBlock().getType() == Material.SUGAR_CANE )
            {
                World world = event.getBlock().getWorld();
                Location loc = event.getBlock().getLocation();
                int x = loc.getBlockX();
                int y = loc.getBlockY();
                int z = loc.getBlockZ();

                ArrayList<Block> removal = new ArrayList<Block>();
                for ( Block b : crops.keySet() )
                {
                    if ( b != null )
                    {
                        if ( b.getX() == x && b.getY() == y && b.getZ() == z )
                        {
                            DwarfPlayer dCPlayer = plugin.getDataManager().find( crops.get( b ) );
                            for ( DwarfEffect e : plugin.getEffectRegistry().getEffectsOfType( DwarfEffectType.BLOCKDROP ) ) {
                                if (e.checkInitiator( Material.SUGAR_CANE )) {
                                    int amount = plugin.getUtil().randomAmount(e.getEffectAmount(dCPlayer));
                                    if (amount != 0) {
                                        DwarfEffectEvent ev;
                                        ItemStack[] altered = new ItemStack[1];
                                        altered[0] = new ItemStack(Material.CACTUS, amount);
    
                                        ItemStack[] orig = new ItemStack[event.getBlock().getDrops().size()];
                                        event.getBlock().getDrops().toArray(orig);
    
                                        ev = new DwarfEffectEvent(dCPlayer, e, orig, altered, null, null, null, null, null, event.getBlock(), null);
                                        plugin.getServer().getPluginManager().callEvent(ev);
    
                                        if (ev.isCancelled())
                                            return;
    
                                        for (ItemStack i : ev.getAlteredItems()) {
                                            if (i != null) {
                                                if (i.getAmount() > 0) {
                                                    world.dropItemNaturally(loc, i);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            removal.add( b );
                        }
                    }
                }

                for ( Block b : removal )
                {
                    if ( b != null )
                    {
                        crops.remove( b );
                    }
                }
            }
        }
    }

    // Checks to see if pistons are breaking crop related blocks
    @EventHandler( priority = EventPriority.HIGH )
    public void onBlockPistonExtend( BlockPistonExtendEvent event )
    {
        if ( plugin.getConfigManager().disableCacti )
        {
            if ( !plugin.getUtil().isWorldAllowed( event.getBlock().getWorld() ) )
                return;

            Material[] mats = { Material.COCOA, Material.CACTUS, Material.WHEAT, Material.POTATO, Material.CARROT, Material.NETHER_WART, Material.MELON, Material.SUGAR_CANE };
            if ( removeCrops( event.getBlocks(), mats ) )
                event.setCancelled( true );
        }
    }

    // Checks to see if pistons are breaking crop related blocks
    @EventHandler( priority = EventPriority.HIGH )
    public void onBlockPistonRetract( BlockPistonRetractEvent event )
    {
        if ( plugin.getConfigManager().disableCacti )
        {
            if ( !plugin.getUtil().isWorldAllowed( event.getBlock().getWorld() ) )
                return;

            Material[] mats = { Material.COCOA, Material.CACTUS, Material.WHEAT, Material.POTATO, Material.CARROT, Material.NETHER_WART, Material.MELON, Material.SUGAR_CANE };
            if ( removeCrops( event.getBlocks(), mats ) )
                event.setCancelled( true );
        }
    }

    private boolean removeCrops( List<Block> blocks, Material[] mats )
    {
        boolean bool = false;
        for ( Material m : mats )
        {
            if ( m != null )
            {
                for ( Block b : blocks )
                {
                    if ( b != null )
                    {
                        if ( b.getType() == m )
                        {
                            b.setType( Material.AIR, true );
                            bool = true;
                        }
                        else if ( m == Material.COCOA )
                        {
                            if ( b.getRelative( BlockFace.SOUTH ).getType() == m )
                            {
                                b.getRelative( BlockFace.SOUTH ).setType( Material.AIR );
                                bool = true;
                            }
                            else if ( b.getRelative( BlockFace.NORTH ).getType() == m )
                            {
                                b.getRelative( BlockFace.NORTH ).setType( Material.AIR );
                                bool = true;
                            }
                            else if ( b.getRelative( BlockFace.WEST ).getType() == m )
                            {
                                b.getRelative( BlockFace.WEST ).setType( Material.AIR );
                                bool = true;
                            }
                            else if ( b.getRelative( BlockFace.EAST ).getType() == m )
                            {
                                b.getRelative( BlockFace.EAST ).setType( Material.AIR );
                                bool = true;
                            }
                        }
                    }
                }
            }
        }
        return bool;
    }

    private boolean checkCrops( World world, Location loc )
    {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        Material base = world.getBlockAt( x, y - 1, z ).getType();

        return ( base == Material.FARMLAND );
    }

    private boolean checkCacti( World world, Location loc )
    {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        if ( isBuildable( world.getBlockAt( x - 1, y, z ).getType() ) )
            return false;
        if ( isBuildable( world.getBlockAt( x + 1, y, z ).getType() ) )
            return false;
        if ( isBuildable( world.getBlockAt( x, y, z - 1 ).getType() ) )
            return false;
        if ( isBuildable( world.getBlockAt( x, y, z + 1 ).getType() ) )
            return false;

        Material base = world.getBlockAt( x, y - 1, z ).getType();

        return ( base == Material.CACTUS ) || ( base == Material.SAND );
    }

    // Bukkit really needs to implement access to Material.isBuildable()
    private boolean isBuildable( Material block )
    {
        switch ( block )
        {
            case AIR:
            case WATER:
            case LAVA:
            case SUNFLOWER:
            case RED_TULIP:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case OAK_SAPLING:
            case SUGAR_CANE:
            case FIRE:
            case STONE_BUTTON:
            case COMPARATOR:
            case REPEATER:
            case LADDER:
            case LEVER:
            case RAIL:
            case REDSTONE_WIRE:
            case TORCH:
            case REDSTONE_TORCH:
            case SNOW:
            case POWERED_RAIL:
            case DETECTOR_RAIL:
                return false;
            default:
                return true;
        }
    }

}
