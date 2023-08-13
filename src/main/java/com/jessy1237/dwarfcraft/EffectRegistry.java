/*
 * EntityManager
 * 3/31/2022
 *
 * Copyright (c) 2016-2021
 * Licensed under LGPL 2.1
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos, and RCarretta
 */

package com.jessy1237.dwarfcraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.jessy1237.dwarfcraft.models.effects.DwarfEffect;
import com.jessy1237.dwarfcraft.models.effects.DwarfEffectType;

public class EffectRegistry {
    HashMap<DwarfEffectType, List<DwarfEffect>> registry;
    
    public EffectRegistry() {
        this.registry = new HashMap<>();
    }
    
    public void registerEffect(DwarfEffectType type, DwarfEffect effect) {
        if (this.registry.containsKey(type)) {
            this.registry.get(type).add(effect);
        } else {
            ArrayList<DwarfEffect> effectList = new ArrayList<>();
            effectList.add(effect);
            this.registry.put(type, effectList);
        }
    }
    
    public List<DwarfEffect> getEffectsOfType( DwarfEffectType type ) {
        if (!this.registry.containsKey(type))
            return new ArrayList<>();
        else
            return this.registry.get(type);
    }
}
