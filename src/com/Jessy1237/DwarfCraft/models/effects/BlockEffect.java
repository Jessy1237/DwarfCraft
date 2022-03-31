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

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Messages;
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
        String out;
        
        switch ( mType )
        {
            case BLOCKDROP:
                out = Messages.describeLevelBlockdrop;
                break;
            case PLOW:
                out = Messages.describeLevelPlow;
                break;
            case DIGTIME:
                out = Messages.describeLevelDigTime;
                break;
            default: out = "&6This Effect description is not yet implemented: " + this.getEffectType().toString();
        }
        
        return out;
    }
}
