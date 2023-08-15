package com.jessy1237.dwarfcraft.models.effects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.Placeholder;
import com.jessy1237.dwarfcraft.Util;
import com.jessy1237.dwarfcraft.events.DwarfEffectEvent;
import com.jessy1237.dwarfcraft.models.DwarfItemHolder;
import com.jessy1237.dwarfcraft.models.DwarfPlayer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DwarfEffect
{
    private DwarfCraft plugin;
    private String skillId;
    private double base, step, stepNovice, min, max;
    private double exceptionLow, exceptionHigh, exceptionValue;
    private int normalLevel;
    private DwarfEffectType type;
    private DwarfItemHolder initiator, output;
    private boolean exception, requireTool, floor;
    private Material[] tools;
    private boolean hasDescription = false;
    private String description, descriptionMore, descriptionLess;
    
    private EntityType entity;
    private final Map<Placeholder,String> replacements = new HashMap<>();
    
    public DwarfEffect(JsonElement element, String skillId, DwarfCraft plugin )
    {
        if ( element == null ) return;
        
        JsonObject json = element.getAsJsonObject();
        this.skillId = skillId;
        base = json.get("base").getAsDouble();
        step = json.get("step").getAsDouble();
        stepNovice = json.get("step_novice").getAsDouble();
        min = json.get("min").getAsInt();
        max = json.get("max").getAsInt();
        exception = false;
        requireTool = false;
        floor = false;
    
        if (json.has("description") || json.has("description_more") || json.has("description_less")) {
            hasDescription = true;
            if ( json.has("description_more") && json.has("description_less") ) {
                descriptionMore = json.get("descriptionMore").getAsString();
                descriptionLess = json.get("descriptionLess").getAsString();
            } else {
                description = json.get("description").getAsString();
            }
        } else {
            plugin.getUtil().consoleLog("Effect is missing `description` for Skill ID `" + skillId.toUpperCase() + "`", Level.WARNING);
        }
        
        if (json.has("exception")) {
            exception = true;
            JsonObject exceptionObj = json.get("exception").getAsJsonObject();
            exceptionLow = exceptionObj.get("low").getAsInt();
            exceptionHigh = exceptionObj.get("high").getAsInt();
            exceptionValue = exceptionObj.get("value").getAsDouble();
            
            if (exceptionLow == 0 && exceptionHigh == 0 && exceptionValue == 0) {
                exception = false;
            }
        }
        
        normalLevel = json.get( "normal_level" ).getAsInt();
        type = DwarfEffectType.getEffectType( json.get( "type" ).getAsString() );
        if ( type != DwarfEffectType.MOBDROP && type != DwarfEffectType.SHEAR || json.get( "origin_material" ).getAsString().equalsIgnoreCase( "AIR" ) )
        {
            initiator = plugin.getUtil().getDwarfItemHolder( json, "origin_material" );
        }
        else
        {
            plugin.getUtil().checkEntityType( json.get("origin_material").getAsString(), skillId );
            entity = EntityType.valueOf( json.get("origin_material").getAsString() );
        }
        output = plugin.getUtil().getDwarfItemHolder(json, "output_material");
        
        if (json.has("should_floor") && json.get("should_floor").getAsBoolean()) {
            floor = true;
        }
        
        if ( !json.has("tools") || json.get("tools").getAsJsonArray().size() <= 0 )
            tools = new Material[0];
        else
        {
            requireTool = true;
            JsonArray toolsArray = json.get("tools").getAsJsonArray();
            tools = new Material[toolsArray.size()];
            if (toolsArray.size() > 0) {
                for (int x = 0; x < toolsArray.size(); x++) {
                    String material = toolsArray.get(x).toString().trim();
                    plugin.getUtil().checkMaterial( material, skillId );
                    Material mat = Material.matchMaterial(material);
                    if (mat != null)
                        tools[x] = mat;
                }
            }
        }
        
        this.plugin = plugin;
    }
    
    public
    String getSkillId() {
        return this.skillId;
    }
    
    public String effectLevelColor( int skillLevel )
    {
        if ( skillLevel > normalLevel)
            return ChatColor.GREEN.toString();
        else if ( skillLevel == normalLevel)
            return ChatColor.YELLOW.toString();
        else
            return ChatColor.RED.toString();
    }
    
    public double getEffectAmount( DwarfPlayer dCPlayer )
    {
        return getEffectAmount( dCPlayer.getSkillLevel( this.skillId), dCPlayer );
    }
    
    public double getEffectAmount( int skillLevel, DwarfPlayer dCPlayer )
    {
        double effectAmount = base;
        if ( skillLevel == -1 )
            skillLevel = normalLevel;
        effectAmount += skillLevel * step;
        effectAmount += Math.min( skillLevel, 5 ) * stepNovice;
        effectAmount = Math.min( effectAmount, max);
        effectAmount = Math.max( effectAmount, min);
        
        if ( dCPlayer != null )
            if ( exception && skillLevel <= exceptionHigh && skillLevel >= exceptionLow && !( skillLevel == plugin.getConfigManager().getRaceLevelLimit() && !plugin.getSkillManager().getSkill( this.skillId).doesSpecialize( dCPlayer.getRace() ) ) )
                effectAmount = exceptionValue;
        
        plugin.getUtil().debugLog( 1, Level.FINE, String.format( "Effect Triggered - Level: %d Base: %.2f Increase: %.2f Novice: %.2f Max: %.2f Min: %.2f "
                    + "Exception: %s Exception Low: %.2f Exception High: %.2f Exception Value: %.2f Floor Result: %s", skillLevel, base, step, stepNovice, max, min, exception, exceptionLow, exceptionHigh, exceptionValue, floor) );
        
        return ( floor ? Math.floor( effectAmount ) : effectAmount );
    }
    
    public DwarfEffectType getEffectType()
    {
        return type;
    }
    
    public Material getInitiatorMaterial()
    {
        return ( initiator == null ? null : initiator.getItemStack() == null ? null : initiator.getItemStack().getType() );
    }
    
    public DwarfItemHolder getInitiator()
    {
        return initiator;
    }
    
    public DwarfItemHolder getOutput()
    {
        return output;
    }
    
    public ItemStack getOutput(DwarfPlayer player )
    {
        final int count = plugin.getUtil().randomAmount( getEffectAmount( player ) );
        ItemStack item = output.getItemStack();
        item.setAmount( count );
        
        return item;
    }
    
    public Material[] getTools()
    {
        return tools;
    }
    
    public boolean checkInitiator( Material mat )
    {
        return initiator.isTagged() ? ( initiator.getMaterials().contains( mat ) ) : ( initiator.getItemStack().getType() == mat );
    }
    
    public EntityType getEntity()
    {
        return entity;
    }
    
    public int getNormalLevel()
    {
        return normalLevel;
    }
    
    public boolean checkTool( ItemStack tool )
    {
        if ( !requireTool)
            return true;
        
        if ( tool == null )
            return false;
        
        for ( Material mat : tools)
            if ( mat == tool.getType() )
                return true;
        
        return false;
    }
    
    public String toolType()
    {
        for ( Material mat : tools )
        {
            if ( mat == Material.IRON_SWORD )
                return "swords";
            if ( mat == Material.IRON_HOE )
                return "hoes";
            if ( mat == Material.IRON_AXE )
                return "axes";
            if ( mat == Material.WOODEN_PICKAXE )
                return "pickaxes";
            if ( mat == Material.IRON_PICKAXE )
                return "most picks";
            if ( mat == Material.DIAMOND_PICKAXE )
                return "high picks";
            if ( mat == Material.IRON_SHOVEL )
                return "shovels";
            if ( mat == Material.FISHING_ROD )
                return "fishing rod";
        }
        return "any tool";
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
    
        plugin.getUtil().debugLog( 2, Level.FINE, String.format( "Affected durability of a \"%s\" - Old: %d Base: %d Wear: %d", plugin.getUtil().getCleanName( tool ), tool.getDurability(), base, wear ) );
        
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
    
    public String description( DwarfPlayer dCPlayer )
    {
        if ( dCPlayer == null ) return "An unknown error has occurred";
        String output;
        
        if (hasDescription) {
            switch (type) {
                case EXPLOSIONDAMAGE:
                case FIREDAMAGE:
                case FALLDAMAGE:
                    String suffix = ( getEffectAmount(dCPlayer) > 1 ) ? "more" : "less";
                    output = this.description.replaceAll(Placeholder.EFFECT_DAMAGE_SUFFIX.value(), suffix);
                    break;
                default:
                    output = this.description;
                    break;
            }
        } else {
            output = "Effect description not specified in data file";
        }
    
        String origFoodLevel = "";
        if ( getInitiatorMaterial() != null )
            origFoodLevel = String.format( "%.2f", ( ( double ) Util.FoodLevel.getLvl( getInitiatorMaterial() ) ) / 2.0 );
    
        String initiator;
        if ( getEntity() != null ) {
            initiator = plugin.getUtil().getCleanName( getEntity() );
        } else {
            initiator = plugin.getUtil().getCleanName( getInitiator() );
            if (getEffectType() == DwarfEffectType.SMELT) {
                List<Recipe> recipes = plugin.getServer().getRecipesFor(new ItemStack(getInitiatorMaterial()));
            
                if (!recipes.isEmpty() && recipes.get(0) instanceof FurnaceRecipe) {
                    FurnaceRecipe recipe = (FurnaceRecipe) recipes.get(0);
                    initiator = plugin.getUtil().getCleanName(recipe.getInput());
                }
            }
        }
        
        double effectAmount = getEffectAmount( dCPlayer );
        double minorAmount = getEffectAmount( getNormalLevel(), null );
        double effectAmountLow = getEffectAmount( 0, dCPlayer );
        double effectAmountHigh = getEffectAmount( plugin.getConfigManager().getMaxSkillLevel(), dCPlayer );
        String minorAmountStr = String.format( "%.2f", minorAmount );
        
        // Replace placeholders
        output = Placeholder.generalParse(output, plugin);
        replacements.put(Placeholder.EFFECT_INITIATOR, ChatColor.DARK_GREEN + initiator );
        replacements.put(Placeholder.EFFECT_CREATURE_NAME, plugin.getUtil().getCleanName( getEntity() ) );
        replacements.put(Placeholder.EFFECT_LEVEL_COLOR, effectLevelColor( dCPlayer.getSkillLevel( getSkillId() ) ) );
        replacements.put(Placeholder.EFFECT_AMOUNT, String.format( "%.2f", getEffectAmount(dCPlayer) ) );
        replacements.put(Placeholder.EFFECT_AMOUNT_INT, String.format( "%d", (int)effectAmount) );
        replacements.put(Placeholder.EFFECT_AMOUNT_MINOR, minorAmountStr );
        replacements.put(Placeholder.EFFECT_AMOUNT_LOW, String.format( "%.2f", effectAmountLow ) );
        replacements.put(Placeholder.EFFECT_AMOUNT_HIGH, String.format( "%.2f", effectAmountHigh ) );
        replacements.put(Placeholder.EFFECT_AMOUNT_NORMAL, String.valueOf(this.getNormalLevel() ) );
        replacements.put(Placeholder.EFFECT_AMOUNT_FOOD_ORIGINAL, origFoodLevel );
        replacements.put(Placeholder.EFFECT_AMOUNT_FOOD, String.format( "%.2f", ( effectAmount / 2.0 ) ) );
        replacements.put(Placeholder.EFFECT_DAMAGE, String.format( "%.2f", effectAmount * 100 ) );
        replacements.put(Placeholder.EFFECT_DAMAGE_BOW, String.format( "%.0f", ( effectAmount + 2 ) ) );
        replacements.put(Placeholder.EFFECT_DAMAGE_TAKEN, String.format( "%.2f", effectAmount * 100 ) );
        replacements.put(Placeholder.EFFECT_AMOUNT_DIG, String.format( "%.0f", +( effectAmount * 100 ) ) );
        replacements.put(Placeholder.EFFECT_OUTPUT, ChatColor.DARK_GREEN + plugin.getUtil().getCleanName( getOutput(dCPlayer) ) );
        replacements.put(Placeholder.EFFECT_TOOL_TYPE, toolType());
    
        for(Placeholder placeholder : replacements.keySet()) {
            String replacement = replacements.get(placeholder);
            if (placeholder != Placeholder.EFFECT_LEVEL_COLOR) replacement += ChatColor.GOLD;
            output = output.replaceAll(placeholder.value(), replacement);
        }
    
        return ChatColor.GOLD + output;
    }
    
}
