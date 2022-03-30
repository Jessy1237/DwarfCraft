/*
 * DwarfEatEffect
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
import java.util.Map;

import org.bukkit.Material;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Messages;
import com.Jessy1237.DwarfCraft.Placeholder;
import com.Jessy1237.DwarfCraft.Util;
import com.Jessy1237.DwarfCraft.models.DwarfItemHolder;
import com.Jessy1237.DwarfCraft.models.DwarfPlayer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class EatEffect extends DwarfEffect {
    private final DwarfCraft plugin;
    private DwarfItemHolder food;
    
    private final Map<Placeholder,String> replacements = new HashMap<>();
    
    public EatEffect(JsonElement element, String skill_id, DwarfCraft plugin) {
        super(element, skill_id, plugin);
        this.plugin = plugin;
    
        JsonObject json = element.getAsJsonObject();
        if (!json.has("origin_material")) return;
    
        food = plugin.getUtil().getDwarfItemHolder(json, "origin_material");
    }
    
    public DwarfItemHolder getFood()
    {
        return food;
    }
    
    public boolean checkFood( Material material )
    {
        return food.isTagged() ? ( food.getMaterials().contains( material ) ) : ( food.getItemStack().getType() == material );
    }
    
    public String description( DwarfPlayer dCPlayer )
    {
        // Replace placeholders
        String description = Placeholder.generalParse(Messages.describeLevelEat, plugin);
        double effectAmount = getEffectAmount( dCPlayer );
        double minorAmount = getEffectAmount( getNormalLevel(), null );
        double effectAmountLow = getEffectAmount( 0, dCPlayer );
        double effectAmountHigh = getEffectAmount( plugin.getConfigManager().getMaxSkillLevel(), dCPlayer );
        String minorAmountStr = String.format( "%.2f", minorAmount );
        String origFoodLevel = "";
        if ( this.getFood() != null )
            origFoodLevel = String.format( "%.2f", ( ( double ) Util.FoodLevel.getLvl( this.getFood().getItemStack().getType() ) ) / 2.0 );
        
        replacements.put(Placeholder.EFFECT_INITIATOR, plugin.getUtil().getCleanName( getFood() ) );
        replacements.put( Placeholder.EFFECT_AMOUNT_FOOD_ORIGINAL, origFoodLevel );
        replacements.put(Placeholder.EFFECT_AMOUNT_FOOD, String.format( "%.2f", ( effectAmount / 2.0 ) ) );
        replacements.put(Placeholder.EFFECT_LEVEL_COLOR, effectLevelColor( dCPlayer.getSkillLevel( getSkillId() ) ) );
        replacements.put(Placeholder.EFFECT_AMOUNT, String.format( "%.2f", getEffectAmount(dCPlayer) ) );
        replacements.put(Placeholder.EFFECT_AMOUNT_MINOR, minorAmountStr );
        replacements.put(Placeholder.EFFECT_AMOUNT_LOW, String.format( "%.2f", effectAmountLow ) );
        replacements.put(Placeholder.EFFECT_AMOUNT_HIGH, String.format( "%.2f", effectAmountHigh ) );
        replacements.put(Placeholder.EFFECT_AMOUNT_NORMAL, String.valueOf(this.getNormalLevel() ) );
        
        for(Placeholder placeholder : replacements.keySet()) {
            description = description.replaceAll(placeholder.value(), replacements.get(placeholder));
        }
        
        return description;
    }
    
}
