/*
 * CraftEffect
 * 3/28/2022
 *
 * Copyright (c) 2016-2021
 * Licensed under LGPL 2.1
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos, and RCarretta
 */

package com.Jessy1237.DwarfCraft.models.effects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Messages;
import com.Jessy1237.DwarfCraft.Placeholder;
import com.Jessy1237.DwarfCraft.models.DwarfItemHolder;
import com.Jessy1237.DwarfCraft.models.DwarfPlayer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CraftEffect extends DwarfEffect {
    private final DwarfCraft plugin;
    protected DwarfItemHolder item;
    private final Map<Placeholder,String> replacements = new HashMap<>();
    
    public CraftEffect(JsonElement element, String skill_id, DwarfCraft plugin) {
        super(element, skill_id, plugin);
        JsonObject json = element.getAsJsonObject();
        this.plugin = plugin;
        if (!json.has("origin_material")) return;
    
        item = plugin.getUtil().getDwarfItemHolder(json, "origin_material");
    }
    
    public DwarfItemHolder getItem()
    {
        return item;
    }
    
    public boolean checkItem( Material material )
    {
        return item.isTagged() ? ( item.getMaterials().contains( material ) ) : ( item.getItemStack().getType() == material );
    }
    
    public ItemStack getOutput(DwarfPlayer player ) {
        final int count = plugin.getUtil().randomAmount( getEffectAmount( player ) );
        ItemStack itemStack = item.getItemStack();
        itemStack.setAmount( count );
        
        return itemStack;
    }
    
    public String description( DwarfPlayer dCPlayer )
    {
        if ( dCPlayer == null ) return "An unknown error has occurred";
        String description;
        
        switch ( mType )
        {
            case CRAFT:
                description = Messages.describeLevelCraft;
                break;
            case SMELT:
                String itemName = "";
                List<Recipe> recipes = plugin.getServer().getRecipesFor(this.getItem().getItemStack());
    
                if (!recipes.isEmpty() && recipes.get(0) instanceof FurnaceRecipe) {
                    FurnaceRecipe recipe = (FurnaceRecipe) recipes.get(0);
                    itemName = plugin.getUtil().getCleanName(recipe.getInput());
                }
                description = Messages.describeLevelSmelt.replaceAll(Placeholder.EFFECT_INITIATOR.value(), itemName);
                break;
            default: description = "&6This Effect description is not yet implemented: " + this.getEffectType().toString();
        }
    
        description = Placeholder.generalParse(description, plugin);
        double effectAmount = getEffectAmount( dCPlayer );
        double minorAmount = getEffectAmount( getNormalLevel(), null );
        double effectAmountLow = getEffectAmount( 0, dCPlayer );
        double effectAmountHigh = getEffectAmount( plugin.getConfigManager().getMaxSkillLevel(), dCPlayer );
        String minorAmountStr = String.format( "%.2f", minorAmount );
        String origFoodLevel = "";
    
        String initiator = plugin.getUtil().getCleanName( getItem() );
        if (getEffectType() == DwarfEffectType.SMELT) {
            List<Recipe> recipes = plugin.getServer().getRecipesFor( getItem().getItemStack() );
            
            if (!recipes.isEmpty() && recipes.get(0) instanceof FurnaceRecipe) {
                FurnaceRecipe recipe = (FurnaceRecipe) recipes.get(0);
                initiator = plugin.getUtil().getCleanName(recipe.getInput());
            }
        }
        
        replacements.put(Placeholder.EFFECT_INITIATOR, initiator );
        replacements.put( Placeholder.EFFECT_AMOUNT_FOOD_ORIGINAL, origFoodLevel );
        replacements.put(Placeholder.EFFECT_AMOUNT_FOOD, String.format( "%.2f", ( effectAmount / 2.0 ) ) );
        replacements.put(Placeholder.EFFECT_LEVEL_COLOR, effectLevelColor( dCPlayer.getSkillLevel( getSkillId() ) ) );
        replacements.put(Placeholder.EFFECT_AMOUNT, String.format( "%.2f", getEffectAmount(dCPlayer) ) );
        replacements.put(Placeholder.EFFECT_AMOUNT_MINOR, minorAmountStr );
        replacements.put(Placeholder.EFFECT_AMOUNT_LOW, String.format( "%.2f", effectAmountLow ) );
        replacements.put(Placeholder.EFFECT_AMOUNT_HIGH, String.format( "%.2f", effectAmountHigh ) );
        replacements.put(Placeholder.EFFECT_AMOUNT_NORMAL, String.valueOf(this.getNormalLevel() ) );
        replacements.put(Placeholder.EFFECT_OUTPUT, plugin.getUtil().getCleanName( getOutput(dCPlayer) ) );
    
        for(Placeholder placeholder : replacements.keySet()) {
            description = description.replaceAll(placeholder.value(), replacements.get(placeholder));
        }
        
        return description;
    }
}
