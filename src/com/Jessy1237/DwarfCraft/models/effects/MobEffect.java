/*
 * MobDropEffect
 * 3/27/2022
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
import java.util.logging.Level;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Messages;
import com.Jessy1237.DwarfCraft.Placeholder;
import com.Jessy1237.DwarfCraft.models.DwarfItemHolder;
import com.Jessy1237.DwarfCraft.models.DwarfPlayer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MobEffect extends ToolEffect {
    private final DwarfCraft plugin;
    protected DwarfItemHolder mobDrop;
    protected EntityType mEntity;
    private final Map<Placeholder,String> replacements = new HashMap<>();
    
    public MobEffect(JsonElement element, String skill_id, DwarfCraft plugin) {
        super(element, skill_id, plugin);
        this.plugin = plugin;
        
        JsonObject json = element.getAsJsonObject();
        try {
            mEntity = EntityType.valueOf(json.get("origin_material").getAsString());
        } catch (IllegalArgumentException e) {
            plugin.getUtil().consoleLog(Level.FINE, "Invalid entity type " + json.get("origin_material").getAsString() + " in effect!");
        }
            
        if ( json.has("output_material") ) {
            mobDrop = plugin.getUtil().getDwarfItemHolder(json, "output_material");
        }
    }
    
    public EntityType getEntity()
    {
        return mEntity;
    }
    
    public DwarfItemHolder getMobDrop() {
        return mobDrop;
    }
    
    public ItemStack getOutput( DwarfPlayer player ) {
        final int count = plugin.getUtil().randomAmount( getEffectAmount( player ) );
        ItemStack item = mobDrop.getItemStack();
        item.setAmount( count );
        
        return item;
    }
    
    public String description( DwarfPlayer dCPlayer )
    {
        if ( dCPlayer == null ) return "An unknown error has occurred";
        String description;
        
        switch ( mType )
        {
            case MOBDROP:
                if ( getEntity() == null )
                    description = Messages.describeLevelMobdropNoCreature;
                else
                    description = Messages.describeLevelMobdrop;
                break;
            case SHEAR:
                description = Messages.describeLevelShear;
                break;
            default: description = "&6This Effect description is not yet implemented: " + this.getEffectType().toString();
        }
        
        // Replace placeholders
        description = Placeholder.generalParse(description, plugin);
        replacements.put(Placeholder.EFFECT_CREATURE_NAME, (mEntity != null) ? plugin.getUtil().getCleanName(mEntity) : "");
        replacements.put(Placeholder.EFFECT_LEVEL_COLOR, effectLevelColor( dCPlayer.getSkillLevel( getSkillId() ) ) );
        replacements.put(Placeholder.EFFECT_AMOUNT, String.format( "%.2f", getEffectAmount(dCPlayer) ) );
        replacements.put(Placeholder.EFFECT_OUTPUT, plugin.getUtil().getCleanName( getOutput(dCPlayer) ) );
        
        for(Placeholder placeholder : replacements.keySet()) {
            description = description.replaceAll(placeholder.value(), replacements.get(placeholder));
        }
        return description;
    }
}
