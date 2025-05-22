/*
 * DwarfEffectType
 * 3/27/2022
 *
 * Copyright (c) 2016-2021
 * Licensed under LGPL 2.1
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos, and RCarretta
 */

package com.jessy1237.dwarfcraft.models.effects;

public enum DwarfEffectType
{
    // IMPLEMENTATION PRIORITY ORDER
    BLOCKDROP,
    MOBDROP,
    SWORDDURABILITY,
    PVPDAMAGE,
    PVEDAMAGE,
    EXPLOSIONDAMAGE,
    FIREDAMAGE,
    FALLDAMAGE,
    FALLTHRESHOLD,
    PLOWDURABILITY,
    TOOLDURABILITY,
    EAT,
    CRAFT,
    PLOW,
    DIGTIME,
    BOWATTACK,
    VEHICLEDROP,
    VEHICLEMOVE,
    SPECIAL,
    FISH,
    RODDURABILITY,
    SMELT,
    BREW,
    SHEAR;

    public static DwarfEffectType getEffectType( String name )
    {
        for ( DwarfEffectType effectType : DwarfEffectType.values() )
        {
            if ( effectType.toString().equalsIgnoreCase( name ) )
                return effectType;
        }
        return null;
    }
    
    public static boolean has( String name )
    {
        for ( DwarfEffectType effectType : DwarfEffectType.values() )
        {
            if ( effectType.toString().equalsIgnoreCase( name ) )
                return true;
        }
        return false;
    }
}
