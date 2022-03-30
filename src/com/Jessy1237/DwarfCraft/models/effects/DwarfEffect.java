/*
 * DwarfEffect
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

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Messages;
import com.Jessy1237.DwarfCraft.Placeholder;
import com.Jessy1237.DwarfCraft.models.DwarfPlayer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DwarfEffect
{
    private DwarfCraft plugin;
    protected String skill_id;
    protected DwarfEffectType mType = null;
    
    protected double mBase;
    protected double mStep;
    protected double mStepNovice;
    protected double mMin, mMax;
    
    protected boolean mException = false;
    protected double mExceptionLow;
    protected double mExceptionHigh;
    protected double mExceptionValue;
    
    protected int mNormalLevel;
    protected boolean mFloorResult = false;
    private final Map<Placeholder,String> replacements = new HashMap<>();
    
    public DwarfEffect(JsonElement element, String skill_id, DwarfCraft plugin)
    {
        if ( element == null ) return;
        JsonObject json = element.getAsJsonObject();
    
        this.plugin = plugin;
        this.skill_id = skill_id;
        this.mType = DwarfEffectType.getEffectType( json.get("type").getAsString() );
        
        mBase = json.get("base").getAsDouble();
        mStep = json.get("step").getAsDouble();
        mStepNovice = json.get("step_novice").getAsDouble();
        mMin = json.get("min").getAsInt();
        mMax = json.get("max").getAsInt();
    
        if (json.has("exception")) {
            mException = true;
            JsonObject exceptionObj = json.get("exception").getAsJsonObject();
            mExceptionLow = exceptionObj.get("low").getAsInt();
            mExceptionHigh = exceptionObj.get("high").getAsInt();
            mExceptionValue = exceptionObj.get("value").getAsDouble();
            
            if (mExceptionLow == 0 && mExceptionHigh == 0 && mExceptionValue == 0) {
                mException = false;
            }
        }

        mNormalLevel = json.get( "normal_level" ).getAsInt();
        
        if (json.has("should_floor") && json.get("should_floor").getAsBoolean()) {
            mFloorResult = true;
        }
    }

    public
    String getSkillId()
    {
        return this.skill_id;
    }
    
    public DwarfEffectType getEffectType()
    {
        return mType;
    }
    
    public String effectLevelColor( int skillLevel )
    {
        if ( skillLevel > mNormalLevel )
            return Messages.effectLevelColorGreaterThanNormal;
        else if ( skillLevel == mNormalLevel )
            return Messages.effectLevelColorEqualToNormal;
        else
            return Messages.effectLevelColorLessThanNormal;
    }
    
    public double getEffectAmount( DwarfPlayer dCPlayer )
    {
        return getEffectAmount( dCPlayer.getSkillLevel( this.skill_id ), dCPlayer );
    }

    public double getEffectAmount( int skillLevel, DwarfPlayer dCPlayer )
    {
        if ( dCPlayer == null ) return 0;
        
        double effectAmount = mBase;
        if ( skillLevel == -1 ) skillLevel = mNormalLevel;
        effectAmount += skillLevel * mStep;
        effectAmount += Math.min( skillLevel, 5 ) * mStepNovice;
        effectAmount = Math.min( effectAmount, mMax );
        effectAmount = Math.max( effectAmount, mMin );

        if ( mException && skillLevel <= mExceptionHigh && skillLevel >= mExceptionLow
                && !( skillLevel == plugin.getConfigManager().getRaceLevelLimit()
                && !plugin.getSkillManager().getSkill( this.skill_id ).doesSpecialize( dCPlayer.getRace() ) ) ) {
            effectAmount = mExceptionValue;
        }
    
        String effectString = String.format( "DC1: GetEffectAmount Level: %d Base: %.2f Increase: %.2f Novice: %.2f Max: %.2f Min: %.2f "
                + "Exception: %s Exception Low: %.2f Exception High: %.2f Exception Value: %.2f Floor Result: %s",
                skillLevel, mBase, mStep, mStepNovice, mMax, mMin, mException, mExceptionLow, mExceptionHigh, mExceptionValue, mFloorResult );
        plugin.getUtil().debugLog( 1, Level.FINE, effectString );

        return ( mFloorResult ? Math.floor( effectAmount ) : effectAmount );
    }

    public int getNormalLevel()
    {
        return mNormalLevel;
    }
    
    public String description( DwarfPlayer dCPlayer )
    {
        if ( dCPlayer == null ) return "An unknown error has occurred";
        String description;
        
        switch ( mType )
        {
            case EXPLOSIONDAMAGE:
                if ( getEffectAmount( dCPlayer ) > 1 )
                {
                    description = Messages.describeLevelExplosionDamageMore;
                }
                else
                {
                    description = Messages.describeLevelExplosionDamageLess;
                }
                break;
            case FIREDAMAGE:
                if ( getEffectAmount( dCPlayer ) > 1 )
                {
                    description = Messages.describeLevelFireDamageMore;
                }
                else
                {
                    description = Messages.describeLevelFireDamageLess;
                }
                break;
            case FALLDAMAGE:
                if ( getEffectAmount( dCPlayer ) > 1 )
                {
                    description = Messages.describeLevelFallingDamageMore;
                }
                else
                {
                    description = Messages.describeLevelFallingDamageLess;
                }
                break;
            case FALLTHRESHOLD:
                description = Messages.describeLevelFallThreshold;
                break;
            case BREW:
                description = Messages.describeLevelBrew;
                break;
            case BOWATTACK:
                description = Messages.describeLevelBowAttack;
                break;
            default: description = "&6This Effect description is not yet implemented: " + this.getEffectType().toString();
        }
    
        double effectAmount = getEffectAmount( dCPlayer );
        double minorAmount = getEffectAmount( getNormalLevel(), null );
        double effectAmountLow = getEffectAmount( 0, dCPlayer );
        double effectAmountHigh = getEffectAmount( plugin.getConfigManager().getMaxSkillLevel(), dCPlayer );
        String minorAmountStr = String.format( "%.2f", minorAmount );
    
        // Replace placeholders
        description = Placeholder.generalParse(description, plugin);
        replacements.put(Placeholder.EFFECT_LEVEL_COLOR, effectLevelColor( dCPlayer.getSkillLevel( getSkillId() ) ) );
        replacements.put(Placeholder.EFFECT_AMOUNT, String.format( "%.2f", getEffectAmount(dCPlayer) ) );
        replacements.put(Placeholder.EFFECT_AMOUNT_MINOR, minorAmountStr );
        replacements.put(Placeholder.EFFECT_AMOUNT_LOW, String.format( "%.2f", effectAmountLow ) );
        replacements.put(Placeholder.EFFECT_AMOUNT_HIGH, String.format( "%.2f", effectAmountHigh ) );
        replacements.put(Placeholder.EFFECT_AMOUNT_NORMAL, String.valueOf(this.getNormalLevel() ) );
        replacements.put(Placeholder.EFFECT_DAMAGE, String.valueOf ( effectAmount * 100 ) );
        replacements.put(Placeholder.EFFECT_DAMAGE_BOW, String.format( "%.0f", ( effectAmount + 2 ) ) );
        replacements.put(Placeholder.EFFECT_DAMAGE_TAKEN, String.valueOf( effectAmount * 100 ) );
        replacements.put(Placeholder.EFFECT_AMOUNT_DIG, String.format( "%.0f", +( effectAmount * 100 ) ) );
        
        for(Placeholder placeholder : replacements.keySet()) {
            description = description.replaceAll(placeholder.value(), replacements.get(placeholder));
        }
        
        return description;
    }
    
}
