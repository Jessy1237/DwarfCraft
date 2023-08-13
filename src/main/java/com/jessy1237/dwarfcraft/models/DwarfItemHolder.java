/*
 * Copyright (c) 2023.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft.models;

import java.util.Set;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;

public class DwarfItemHolder
{

    private Set<Material> mats;
    private Tag<Material> tag;
    private String tagName;

    public DwarfItemHolder( Set<Material> mats, Tag<Material> tag, String tagName )
    {
        this.tag = tag;
        this.mats = mats;
        this.tagName = tagName;

        if ( tag != null )
        {
            this.mats = tag.getValues();
        }
    }

    public boolean isTagged()
    {
        return tag != null;
    }

    public Set<Material> getMaterials()
    {
        return mats;
    }

    public ItemStack getItemStack()
    {
        return mats.isEmpty() ? new ItemStack( Material.AIR ) : new ItemStack( mats.iterator().next() );
    }

    public Tag<Material> getTag()
    {
        return tag;
    }

    public String getTagName()
    {
        return tagName;
    }
}
