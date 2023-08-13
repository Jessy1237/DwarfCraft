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

public class DwarfTrainingItem
{
    private final DwarfItemHolder dih;
    private final double base;
    private final int max;

    public DwarfTrainingItem( DwarfItemHolder dih, double base, int max )
    {
        this.dih = dih;
        this.base = base;
        this.max = max;
    }

    public DwarfItemHolder getDwarfItemHolder()
    {
        return dih;
    }

    public double getBase()
    {
        return this.base;
    }

    public int getMax()
    {
        return this.max;
    }
}
