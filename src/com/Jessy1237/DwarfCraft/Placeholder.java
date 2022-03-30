/*
 * Copyright (c) 2018.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.Jessy1237.DwarfCraft;

public enum Placeholder
{
    EFFECT_AMOUNT( "<effect.amount>" ),
    EFFECT_AMOUNT_DIG( "<effect.amount.dig>" ),
    EFFECT_AMOUNT_FOOD( "<effect.amount.food>" ),
    EFFECT_AMOUNT_FOOD_ORIGINAL( "<effect.amount.food.original>" ),
    EFFECT_AMOUNT_HIGH( "<effect.amount.high>" ),
    EFFECT_AMOUNT_INT( "<effect.amount.int>" ),
    EFFECT_AMOUNT_LOW( "<effect.amount.low>" ),
    EFFECT_AMOUNT_MINOR( "<effect.minor.amount>" ),
    EFFECT_AMOUNT_NORMAL( "<effect.normal.level>" ),
    EFFECT_CREATURE_NAME( "<effect.creature.name>" ),
    EFFECT_DAMAGE( "<effect.damage>" ),
    EFFECT_DAMAGE_BOW( "<effect.damage.bow>" ),
    EFFECT_DAMAGE_TAKEN( "<effect.damage.taken>" ),
    EFFECT_INITIATOR( "<effect.initiator>" ),
    EFFECT_OUTPUT( "<effect.output>" ),
    EFFECT_TOOL_TYPE( "<effect.tool.type>" ),
    EFFECT_LEVEL_COLOR( "<effect.level.color>" ),
    ITEM_NAME( "<item.name>" ),
    LEVEL( "<level>" ),
    SKILL_MAX_LEVEL( "<skill.max.level>" ),
    PLAYER_LEVEL( "<player.level>" ),
    PLAYER_NAME( "<player.name>" ),
    PLAYER_RACE( "<player.race>" ),
    RACE_LEVEL_LIMIT( "<race.level.limit>" ),
    RACE_NAME( "<race.name>" ),
    SKILL_COST_AMOUNT( "<skill.cost.amount>" ),
    SKILL_TOTAL_COST( "<skill.cost.total>" ),
    SKILL_DEPOSIT_AMOUNT( "<skill.deposit.amount>" ),
    SKILL_ID( "<skill.id>" ),
    SKILL_ITEM_TYPE( "<skill.item.type>" ),
    SKILL_LEVEL( "<skill.level>" ),
    SKILL_LEVEL_NEXT( "<skill.level.next>" ),
    SKILL_NAME( "<skill.name>" );
    
    private final String placeholder;
    
    Placeholder( String placeholder )
    {
        this.placeholder = placeholder;
    }
    
    public String value()
    {
        return placeholder;
    }
    
    public static String generalParse( String text, DwarfCraft plugin )
    {
        return text.replaceAll( Placeholder.SKILL_MAX_LEVEL.value(), "" + plugin.getConfigManager().getMaxSkillLevel() ).replaceAll( Placeholder.RACE_LEVEL_LIMIT.value(), "" + plugin.getConfigManager().getRaceLevelLimit() );
    }
}


