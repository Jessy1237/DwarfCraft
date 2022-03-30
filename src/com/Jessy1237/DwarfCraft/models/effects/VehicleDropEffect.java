/*
 * VehicleDropEffect
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

import org.bukkit.inventory.ItemStack;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Messages;
import com.Jessy1237.DwarfCraft.models.DwarfItemHolder;
import com.Jessy1237.DwarfCraft.models.DwarfPlayer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class VehicleDropEffect extends DwarfEffect {
    private DwarfCraft plugin;
    protected DwarfItemHolder blockDrop;
    
    public VehicleDropEffect(JsonElement element, String skill_id, DwarfCraft plugin) {
        super(element, skill_id, plugin);
        JsonObject json = element.getAsJsonObject();
        this.plugin = plugin;
        if (!json.has("output_material")) return;
    
        blockDrop = plugin.getUtil().getDwarfItemHolder(json, "output_material");
    }
    
    public DwarfItemHolder getDrop() {
        return blockDrop;
    }
    
    public ItemStack getOutput(DwarfPlayer player ) {
        final int count = plugin.getUtil().randomAmount( getEffectAmount( player ) );
        ItemStack item = blockDrop.getItemStack();
        item.setAmount( count );
        
        return item;
    }
    
    public String description( DwarfPlayer dCPlayer )
    {
        if ( dCPlayer == null ) return "An unknown error has occurred";
        return Messages.describeLevelVehicleDrop;
    }
}
