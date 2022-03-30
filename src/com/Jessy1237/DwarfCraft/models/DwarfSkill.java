/*
 * Copyright (c) 2018.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.Jessy1237.DwarfCraft.models;

import java.util.*;

import org.bukkit.Material;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Placeholder;
import com.Jessy1237.DwarfCraft.models.effects.DwarfEffect;
import com.Jessy1237.DwarfCraft.models.effects.DwarfEffectType;

public class DwarfSkill implements Cloneable
{
    private final DwarfCraft plugin;
    private final String mID;
    private final String mName;
    private final LinkedHashMap<String, DwarfRace> mRaces;
    private int mLevel;
    private final List<DwarfEffect> mEffects;
    private final Material mHeldItem;
    private final DwarfTrainingItem mItem1, mItem2, mItem3;
    private int mDeposit1, mDeposit2, mDeposit3;
    
    private final Map<Placeholder,String> replacements = new HashMap<>();

    public DwarfSkill(final DwarfCraft plugin, String id, String displayName, LinkedHashMap<String, DwarfRace> races, int level, List<DwarfEffect> effects, DwarfTrainingItem item1, DwarfTrainingItem item2, DwarfTrainingItem item3, Material trainerHeldMaterial )
    {
        this.plugin = plugin;
        mID = id;
        mName = displayName;
        mRaces = races;

        mItem1 = item1;
        mItem2 = item2;
        mItem3 = item3;

        mLevel = level;
        mEffects = effects;
        mHeldItem = trainerHeldMaterial;
    }

    @Override
    public DwarfSkill clone()
    {
        try
        {
            super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            e.printStackTrace();
        }
        return new DwarfSkill( plugin, mID, mName, mRaces, mLevel, mEffects, mItem1, mItem2, mItem3, mHeldItem );
    }

    public String getDisplayName()
    {
        return mName;
    }

    public LinkedHashMap<String, DwarfRace> getRaces()
    {
        return mRaces;
    }

    public List<DwarfEffect> getEffects()
    {
        return mEffects;
    }
    
    public List<DwarfEffect> getEffectsOfType(DwarfEffectType type) {
        List<DwarfEffect> effectList = new ArrayList<>();
        for ( DwarfEffect effect : this.getEffects() ) {
            if (effect.getEffectType() == type) effectList.add(effect);
        }
        return effectList;
    }

    public String getId()
    {
        return mID;
    }

    public int getLevel()
    {
        return mLevel;
    }

    protected Material getTrainerHeldMaterial()
    {
        if (mHeldItem == null)
            return Material.AIR;

        return mHeldItem;
    }

    public void setLevel( int newLevel )
    {
        mLevel = newLevel;
    }

    @Override
    public String toString()
    {
        if ( mName == null ) return "";
        return mName.toLowerCase().replaceAll( " ", "_" );
    }

    public DwarfTrainingItem getItem( int itemId )
    {
        if ( itemId == 3 )
            return mItem3;
        else if ( itemId == 2 )
            return mItem2;
        else
            return mItem1;
    }

    public int getDeposit( int depositId )
    {
        if ( depositId == 3 )
            return mDeposit3;
        else if ( depositId == 2 )
            return mDeposit2;
        else
            return mDeposit1;
    }

    public void setDeposit( int amount, int depositId )
    {
        if ( depositId == 3 )
            this.mDeposit3 = amount;
        else if ( depositId == 2 )
            this.mDeposit2 = amount;
        else
            this.mDeposit1 = amount;
    }

    public boolean doesSpecialize( DwarfRace race ) {
        return this.mRaces.containsValue( race );
    }

    public int getMaxLevel( DwarfPlayer dcPlayer )
    {
        int maxLevel = plugin.getConfigManager().getMaxSkillLevel();
        int raceLevel = plugin.getConfigManager().getRaceLevelLimit();
        return doesSpecialize(dcPlayer.getRace() ) ? maxLevel : raceLevel;
    }
    
    public String description( String text )
    {
        text = Placeholder.generalParse(text, plugin);
    
        replacements.put( Placeholder.SKILL_ID, String.valueOf(this.getId()) );
        replacements.put( Placeholder.SKILL_NAME, this.getDisplayName() );
        replacements.put( Placeholder.SKILL_LEVEL_NEXT, String.valueOf(this.getLevel() + 1) );
        for(Placeholder placeholder : replacements.keySet()) {
            text = text.replaceAll(placeholder.value(), replacements.get(placeholder));
        }
        
        return text;
    }
    
    public String description( String text, DwarfPlayer dwarfPlayer )
    {
        // Calculate max level limit for skill. Checks to see if the players race specializes in the skill to see if skill should be locked to level cap.
        int levelLimit = this.getMaxLevel( dwarfPlayer );
        replacements.put( Placeholder.SKILL_LEVEL, String.valueOf( dwarfPlayer.getSkillLevel( getId() ) ) );
        return description( dwarfPlayer.toString( text.replaceAll(Placeholder.SKILL_MAX_LEVEL.value(), String.valueOf( levelLimit ) ) ) );
    }
}
