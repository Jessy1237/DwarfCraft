/*
 * Copyright (c) 2018.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft.models;

import org.bukkit.Material;

public class DwarfRace implements Cloneable
{
    private final String mId;
    private final String mName;
    private String mDescription;
    private String mPrefixColour;
    private Material mIcon;

    public DwarfRace( String id, String name )
    {
        this.mId = id;
        this.mName = name;
        this.mPrefixColour = "&f";
    }

    public DwarfRace( String id, String name, String description, Material icon )
    {
        this.mId = id;
        this.mName = name;
        this.mDescription = description;
        this.mPrefixColour = "&f";
        this.mIcon = icon;
    }

    @Override
    public DwarfRace clone()
    {
        try
        {
            super.clone();
        }
        catch ( CloneNotSupportedException e )
        {
            e.printStackTrace();
        }
        return new DwarfRace( mId, mName, mDescription, mIcon);
    }

    public String getId()
    {
        return mId;
    }

    public String getName()
    {
        return mName;
    }

    public String getDescription()
    {
        return this.mDescription;
    }

    public String getPrefixColour()
    {
        return this.mPrefixColour;
    }

    public Material getIcon()
    {
        return this.mIcon;
    }

    public void setDescription( String description )
    {
        this.mDescription = description;
    }

    public void setPrefixColour( String prefixColour )
    {
        this.mPrefixColour = prefixColour;
    }

    public void setIcon( Material icon )
    {
        this.mIcon = icon;
    }
}
