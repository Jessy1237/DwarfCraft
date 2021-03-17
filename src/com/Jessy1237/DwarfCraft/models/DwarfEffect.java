/*
 * Copyright (c) 2018.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.Jessy1237.DwarfCraft.models;

import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Messages;
import com.Jessy1237.DwarfCraft.events.DwarfEffectEvent;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DwarfEffect
{
    private DwarfCraft plugin;
    private String skill_id;
    private double mBase;
    private double mStep;
    private double mStepNovice;
    private double mMin, mMax;
    private boolean mException;
    private double mExceptionLow;
    private double mExceptionHigh;
    private double mExceptionValue;
    private int mNormalLevel;
    private DwarfEffectType mType;
    private DwarfItemHolder mInitiator;
    private DwarfItemHolder mResult;
    private boolean mRequireTool;
    private Material[] mTools;
    private boolean mFloorResult;

    private EntityType mCreature;

    public DwarfEffect( JsonElement element, String skill_id, DwarfCraft plugin )
    {
        if ( element == null )
            return;

        JsonObject json = element.getAsJsonObject();
        this.skill_id = skill_id;
        mBase = json.get( "base" ).getAsDouble();
        mStep = json.get( "step" ).getAsDouble();
        mStepNovice = json.get( "step_novice" ).getAsDouble();
        mMin = json.get( "min" ).getAsInt();
        mMax = json.get( "max" ).getAsInt();
        mException = json.get( "should_use_exception" ).getAsBoolean();

        JsonObject exceptionObj = json.get( "exception" ).getAsJsonObject();
        mExceptionLow = exceptionObj.get( "low" ).getAsInt();
        mExceptionHigh = exceptionObj.get( "high" ).getAsInt();
        mExceptionValue = exceptionObj.get( "value" ).getAsDouble();

        mNormalLevel = json.get( "normal_level" ).getAsInt();
        mType = DwarfEffectType.getEffectType( json.get( "type" ).getAsString() );
        if ( mType != DwarfEffectType.MOBDROP && mType != DwarfEffectType.SHEAR || json.get( "origin_material" ).getAsString().equalsIgnoreCase( "AIR" ) )
        {
            mInitiator = plugin.getUtil().getDwarfItemHolder( json, "origin_material" );
        }
        else
        {
            plugin.getUtil().checkEntityType( json.get( "origin_material" ).getAsString(), skill_id );
            mCreature = EntityType.valueOf( json.get( "origin_material" ).getAsString() );
        }
        mResult = plugin.getUtil().getDwarfItemHolder( json, "output_material" );

        mRequireTool = json.get( "should_require_tool" ).getAsBoolean();
        mFloorResult = json.get( "should_floor" ).getAsBoolean();

        if ( json.get( "tools" ).getAsJsonArray().size() <= 0 )
            mTools = new Material[0];
        else
        {
            JsonArray toolsArray = json.get( "tools" ).getAsJsonArray();
            mTools = new Material[toolsArray.size()];
            if (toolsArray.size() > 0) {
                for (int x = 0; x < toolsArray.size(); x++) {
                    String material = toolsArray.get(x).toString().trim();
                    plugin.getUtil().checkMaterial( material, skill_id );
                    Material mat = Material.matchMaterial(material);
                    if (mat != null)
                        mTools[x] = mat;
                }
            }
        }

        this.plugin = plugin;
    }

    public
    String getSkillId() {
        return this.skill_id;
    }

    /**
     * Description of a skills effect at a given level
     * 
     * @param dCPlayer the DwarfPlayer instance
     * @return description of a skills effect at a given level
     */
    public String describeLevel( DwarfPlayer dCPlayer )
    {
        if ( dCPlayer == null )
            return "Failed"; // TODO add failure code

        return plugin.getOut().parseEffectLevel( dCPlayer, this );
    }

    public String effectLevelColor( int skillLevel )
    {
        if ( skillLevel > mNormalLevel )
            return Messages.effectLevelColorGreaterThanNormal;
        else if ( skillLevel == mNormalLevel )
            return Messages.effectLevelColorEqualToNormal;
        else
            return Messages.effectLevelColorLessThanNormal;
    }

    /**
     * Returns an effect Amount for a particular Dwarf
     * 
     * @param dCPlayer the DwarfPlayer instance
     * @return the effect amount
     */
    public double getEffectAmount( DwarfPlayer dCPlayer )
    {
        return getEffectAmount( dCPlayer.getSkillLevel( this.skill_id ), dCPlayer );
    }

    public double getEffectAmount( int skillLevel, DwarfPlayer dCPlayer )
    {
        double effectAmount = mBase;
        if ( skillLevel == -1 )
            skillLevel = mNormalLevel;
        effectAmount += skillLevel * mStep;
        effectAmount += Math.min( skillLevel, 5 ) * mStepNovice;
        effectAmount = Math.min( effectAmount, mMax );
        effectAmount = Math.max( effectAmount, mMin );

        if ( dCPlayer != null )
            if ( mException && skillLevel <= mExceptionHigh && skillLevel >= mExceptionLow && !( skillLevel == plugin.getConfigManager().getRaceLevelLimit() && !plugin.getSkillManager().getSkill( this.skill_id ).doesSpecialize( dCPlayer.getRace() ) ) )
                effectAmount = mExceptionValue;

        if ( DwarfCraft.debugMessagesThreshold < 1 )
        {
            plugin.getUtil().consoleLog( Level.FINE, String.format( "DC1: GetEffectAmmount Level: %d Base: %.2f Increase: %.2f Novice: %.2f Max: %.2f Min: %.2f "
                    + "Exception: %s Exctpion Low: %.2f Exception High: %.2f Exception Value: %.2f Floor Result: %s", skillLevel, mBase, mStep, mStepNovice, mMax, mMin, mException, mExceptionLow, mExceptionHigh, mExceptionValue, mFloorResult ) );
        }

        return ( mFloorResult ? Math.floor( effectAmount ) : effectAmount );
    }

    public DwarfEffectType getEffectType()
    {
        return mType;
    }

    public Material getInitiatorMaterial()
    {
        return ( mInitiator == null ? null : mInitiator.getItemStack() == null ? null : mInitiator.getItemStack().getType() );
    }

    public Material getOutputMaterial()
    {
        return ( mResult == null ? null : mResult.getItemStack() == null ? null : mResult.getItemStack().getType() );
    }

    public DwarfItemHolder getInitiator()
    {
        return mInitiator;
    }

    public DwarfItemHolder getResult()
    {
        return mResult;
    }

    public ItemStack getResult( DwarfPlayer player )
    {
        final int count = plugin.getUtil().randomAmount( getEffectAmount( player ) );
        ItemStack item = mResult.getItemStack();
        item.setAmount( count );

        return item;
    }

    public boolean getToolRequired()
    {
        return mRequireTool;
    }

    public Material[] getTools()
    {
        return mTools;
    }

    public boolean checkInitiator( ItemStack item )
    {
        if ( item == null )
            return checkInitiator( Material.AIR );
        else
            return checkInitiator( item.getType() );
    }

    public boolean checkInitiator( Material mat )
    {
        return mInitiator.isTagged() ? ( mInitiator.getMaterials().contains( mat ) ) : ( mInitiator.getItemStack().getType() == mat );
    }

    public String toolType()
    {
        for ( Material mat : mTools )
        {
            if ( mat == Material.IRON_SWORD )
                return "sword";
            if ( mat == Material.IRON_HOE )
                return "hoe";
            if ( mat == Material.IRON_AXE )
                return "axe";
            if ( mat == Material.WOODEN_PICKAXE )
                return "pickaxe";
            if ( mat == Material.IRON_PICKAXE )
                return "most pickaxes";
            if ( mat == Material.DIAMOND_PICKAXE )
                return "diamond pickaxe";
            if ( mat == Material.IRON_SHOVEL )
                return "shovel";
            if ( mat == Material.FISHING_ROD )
                return "fishing rod";
            if ( mat == Material.FLINT_AND_STEEL )
                return "flint and steel";
        }
        return "any tool";
    }

    public boolean checkMob( Entity entity )
    {
        if ( mCreature == null )
            return false;

        switch ( mCreature )
        {
            case CHICKEN:
                return ( entity instanceof Chicken );
            case COW:
                return ( entity instanceof Cow );
            case CREEPER:
                return ( entity instanceof Creeper );
            case GHAST:
                return ( entity instanceof Ghast );
            case GIANT:
                return ( entity instanceof Giant );
            case PIG:
                return ( entity instanceof Pig );
            case ZOMBIFIED_PIGLIN:
                return ( entity instanceof PigZombie );
            case SHEEP:
                return ( entity instanceof Sheep );
            case SKELETON:
                return ( entity instanceof Skeleton );
            case SLIME:
                return ( entity instanceof Slime );
            case SPIDER:
                return ( entity instanceof Spider );
            case SQUID:
                return ( entity instanceof Squid );
            case ZOMBIE:
                return ( entity instanceof Zombie ) && !( entity instanceof PigZombie );
            case WOLF:
                return ( entity instanceof Wolf );
            case MUSHROOM_COW:
                return ( entity instanceof MushroomCow );
            case SILVERFISH:
                return ( entity instanceof Silverfish );
            case ENDERMAN:
                return ( entity instanceof Enderman );
            case VILLAGER:
                return ( entity instanceof Villager );
            case BLAZE:
                return ( entity instanceof Blaze );
            case MAGMA_CUBE:
                return ( entity instanceof MagmaCube );
            case CAVE_SPIDER:
                return ( entity instanceof CaveSpider );
            case SNOWMAN:
                return ( entity instanceof Snowman );
            case ENDER_DRAGON:
                return ( entity instanceof EnderDragon );
            case OCELOT:
                return ( entity instanceof Ocelot );
            case WITHER:
                return ( entity instanceof Wither );
            case IRON_GOLEM:
                return ( entity instanceof IronGolem );
            case BAT:
                return ( entity instanceof Bat );
            case RABBIT:
                return ( entity instanceof Rabbit );
            case GUARDIAN:
                return ( entity instanceof Guardian );
            case HORSE:
                return ( entity instanceof Horse );
            case ENDERMITE:
                return ( entity instanceof Endermite );
            case WITCH:
                return ( entity instanceof Witch );
            case POLAR_BEAR:
                return ( entity instanceof PolarBear );
            case SHULKER:
                return ( entity instanceof Shulker );
            case DONKEY:
                return ( entity instanceof Donkey );
            case MULE:
                return ( entity instanceof Mule );
            case LLAMA:
                return ( entity instanceof Llama );
            case HUSK:
                return ( entity instanceof Husk );
            case SKELETON_HORSE:
                return ( entity instanceof SkeletonHorse );
            case ELDER_GUARDIAN:
                return ( entity instanceof ElderGuardian );
            case EVOKER:
                return ( entity instanceof Evoker );
            case STRAY:
                return ( entity instanceof Stray );
            case ZOMBIE_VILLAGER:
                return ( entity instanceof ZombieVillager );
            case VEX:
                return ( entity instanceof Vex );
            case VINDICATOR:
                return ( entity instanceof Vindicator );
            case ILLUSIONER:
                return ( entity instanceof Illusioner );
            case PARROT:
                return ( entity instanceof Parrot );
            default:
                return false;
        }
    }

    public EntityType getCreature()
    {
        return mCreature;
    }

    public int getNormalLevel()
    {
        return mNormalLevel;
    }

    public boolean checkTool( ItemStack tool )
    {
        if ( !mRequireTool )
            return true;

        if ( tool == null )
            return false;

        for ( Material mat : mTools )
            if ( mat == tool.getType() )
                return true;

        return false;
    }

    public void damageTool( DwarfPlayer player, int base, ItemStack tool )
    {
        damageTool( player, base, tool, true );
    }

    @SuppressWarnings( "deprecation" )
    public
    void damageTool( DwarfPlayer player, int base, ItemStack tool, boolean negate )
    {
        short wear = ( short ) ( plugin.getUtil().randomAmount( getEffectAmount( player ) ) * base );
        Damageable dmg = ( Damageable ) tool.getItemMeta();

        if ( DwarfCraft.debugMessagesThreshold < 2 ) plugin.getUtil().consoleLog( Level.FINE, String.format( "DC2: Affected durability of a \"%s\" - Old: %d Base: %d Wear: %d", plugin.getUtil().getCleanName( tool ), tool.getDurability(), base, wear ) );

        // Some code taken from net.minecraft.server.ItemStack line 165.
        // Checks to see if damage should be skipped.
        if ( tool.containsEnchantment( Enchantment.DURABILITY ) )
        {
            int level = tool.getEnchantmentLevel( Enchantment.DURABILITY );
            Random r = new Random();
            if ( level > 0 && r.nextInt( level + 1 ) > 0 )
            {
                return;
            }
        }

        base = ( negate ? base : 0 );

        if ( wear == base ) return; // This is normal wear, skip everything and let MC handle
        // it
        // internally.

        DwarfEffectEvent e = new DwarfEffectEvent( player, this, null, null, null, null, ( double ) base, ( double ) wear, null, null, tool );
        plugin.getServer().getPluginManager().callEvent( e );

        if ( e.isCancelled() ) return;

        tool.setDurability( ( short ) ( tool.getDurability() + e.getAlteredDamage() - base ) );
        // This may have the side effect of causing items to flicker when they
        // are about to break
        // If this becomes a issue, we need to cast to a CraftItemStack, then
        // make CraftItemStack.item public,
        // And call CraftItemStack.item.damage(-base, player.getPlayer());

        if ( tool.getDurability() >= tool.getType().getMaxDurability() )
        {
            if ( tool.getType() == Material.IRON_SWORD && tool.getDurability() < 250 ) return;

            if ( tool.getAmount() > 1 )
            {
                tool.setAmount( tool.getAmount() - 1 );
                tool.setDurability( ( short ) -1 );
            }
            else
            {
                if ( player.getPlayer().getEquipment().getItemInMainHand().getType() == tool.getType() )
                {
                    player.getPlayer().getEquipment().setItemInMainHand( null );
                }
                else if ( player.getPlayer().getEquipment().getItemInOffHand().getType() == tool.getType() )
                {
                    player.getPlayer().getEquipment().setItemInOffHand( null );
                }
            }
        }
    }

}
