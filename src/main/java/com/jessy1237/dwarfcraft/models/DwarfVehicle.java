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

import org.bukkit.entity.Vehicle;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */
public class DwarfVehicle
{
    private Vehicle vehicle;
    private boolean changedSpeed;

    public DwarfVehicle( Vehicle vehicle )
    {
        this.vehicle = vehicle;
        this.changedSpeed = false;
    }

    @Override
    public boolean equals( Object that )
    {
        if ( that instanceof Vehicle )
        {
            Vehicle vec = ( Vehicle ) that;
            return vec.getEntityId() == vehicle.getEntityId();
        }
        return false;
    }

    public Vehicle getVehicle()
    {
        return this.vehicle;
    }

    public boolean changedSpeed()
    {
        return this.changedSpeed;
    }

    public void speedChanged()
    {
        this.changedSpeed = true;
    }
}