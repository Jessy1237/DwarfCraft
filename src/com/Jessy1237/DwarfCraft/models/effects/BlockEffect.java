/*
 * BlockDropEffect
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

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Messages;
import com.Jessy1237.DwarfCraft.Placeholder;
import com.Jessy1237.DwarfCraft.models.DwarfItemHolder;
import com.Jessy1237.DwarfCraft.models.DwarfPlayer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class BlockEffect extends ToolEffect {
    private final DwarfCraft plugin;
    protected DwarfItemHolder block;
    protected DwarfItemHolder blockDrop = null;
    private boolean hasDrop = false;
    protected boolean allowDupe = false;
    private final Map<Placeholder,String> replacements = new HashMap<>();
    
    public BlockEffect(JsonElement element, String skill_id, DwarfCraft plugin) {
        super(element, skill_id, plugin);
        this.plugin = plugin;
        JsonObject json = element.getAsJsonObject();
        if (!json.has("origin_material")) return;
        
        block = plugin.getUtil().getDwarfItemHolder(json, "origin_material");
        if (json.has("output_material")) {
            hasDrop = true;
            blockDrop = plugin.getUtil().getDwarfItemHolder(json, "output_material");
        }
        
        if (json.has("allow_dupe")) allowDupe = json.get("allow_dupe").getAsBoolean();
    }
    
    public DwarfItemHolder getBlock()
    {
        return block;
    }
    
    public boolean checkBlock( Material material )
    {
        return block.isTagged() ? ( block.getMaterials().contains( material ) ) : ( block.getItemStack().getType() == material );
    }
    
    public boolean hasDrop() {
        return getDrop() != null && hasDrop;
    }
    
    public DwarfItemHolder getDrop() {
        return blockDrop;
    }
    
    public ItemStack getOutput( DwarfPlayer player ) {
        final int count = plugin.getUtil().randomAmount( getEffectAmount( player ) );
        ItemStack item = blockDrop.getItemStack();
        item.setAmount( count );
    
        return item;
    }
    
    public boolean isAllowDupe() {
        return allowDupe;
    }
    
    public String description( DwarfPlayer dCPlayer )
    {
        if ( dCPlayer == null ) return "An unknown error has occurred";
        String description;
        
        switch ( mType )
        {
            case BLOCKDROP:
                description = Messages.describeLevelBlockdrop;
                break;
            case PLOW:
                description = Messages.describeLevelPlow;
                break;
            case DIGTIME:
                description = Messages.describeLevelDigTime;
                break;
            default: description = "&6This Effect description is not yet implemented: " + this.getEffectType().toString();
        }
    
        String initiator = plugin.getUtil().getCleanName( getBlock() );
        double effectAmount = getEffectAmount( dCPlayer );
        double minorAmount = getEffectAmount( getNormalLevel(), null );
        double effectAmountLow = getEffectAmount( 0, dCPlayer );
        double effectAmountHigh = getEffectAmount( plugin.getConfigManager().getMaxSkillLevel(), dCPlayer );
        String minorAmountStr = String.format( "%.2f", minorAmount );
    
        replacements.put(Placeholder.EFFECT_INITIATOR, initiator );
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
