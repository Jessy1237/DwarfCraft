/*
 * Copyright (c) 2018.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.jessy1237.dwarfcraft.models.DwarfPlayer;
import com.jessy1237.dwarfcraft.models.DwarfSkill;
import com.jessy1237.dwarfcraft.models.DwarfTrainer;

public class DwarfDepositEvent extends Event implements Cancellable
{

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private DwarfPlayer player;
    private DwarfTrainer trainer;
    private DwarfSkill skill;

    public HandlerList getHandlers()
    {
        return handlers;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }

    public void setCancelled( boolean cancel )
    {
        cancelled = cancel;
    }

    /**
     * The event for when a DwarfPlayer deposits into their skill. This event is fired after the skill is levelled but before the data is saved.
     * 
     * @param player the player that levelled up a skill
     * @param trainer the trainer that was used to level up the skill
     * @param skill the skill that was levelled up
     */
    public DwarfDepositEvent( DwarfPlayer player, DwarfTrainer trainer, DwarfSkill skill )
    {
        this.player = player;
        this.trainer = trainer;
        this.skill = skill;
    }

    /**
     * Gets the DwarfPlayer that levelled up a skill.
     * 
     * @return DwarfPlayer
     */
    public DwarfPlayer getDwarfPlayer()
    {
        return player;
    }

    /**
     * Gets the trainer that was used to level up the skill.
     * 
     * @return Trainer
     */
    public DwarfTrainer getTrainer()
    {
        return trainer;
    }

    /**
     * Gets the skill that was deposited into.
     * 
     * @return DwarfSkill
     */
    public DwarfSkill getSkill()
    {
        return skill;
    }
}
