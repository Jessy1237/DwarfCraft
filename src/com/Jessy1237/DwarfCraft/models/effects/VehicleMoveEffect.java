/*
 * VehicleMoveEffect
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

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Messages;
import com.Jessy1237.DwarfCraft.models.DwarfItemHolder;
import com.Jessy1237.DwarfCraft.models.DwarfPlayer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class VehicleMoveEffect extends DwarfEffect {
    protected DwarfItemHolder block;
    
    public VehicleMoveEffect(JsonElement element, String skill_id, DwarfCraft plugin) {
        super(element, skill_id, plugin);
    
        JsonObject json = element.getAsJsonObject();
        if (!json.has("origin_material")) return;
    
        block = plugin.getUtil().getDwarfItemHolder(json, "origin_material");
    }
    
    public DwarfItemHolder getBlock()
    {
        return block;
    }
    
    public String description( DwarfPlayer dCPlayer )
    {
        if ( dCPlayer == null ) return "An unknown error has occurred";
        return Messages.describeLevelVehicleMove;
    }
}
