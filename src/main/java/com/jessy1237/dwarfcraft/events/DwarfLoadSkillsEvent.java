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

import java.util.HashMap;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.jessy1237.dwarfcraft.models.DwarfSkill;

public class DwarfLoadSkillsEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private HashMap<String, DwarfSkill> skills;

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    /**
     * The event for when DwarfCraft loads all the skills from the csv file but before it is set into the plugins memory. So you can inject, remove or edit skills into the plugin via this event
     * 
     * @param skills the skills that were loaded by DwarfCraft from the csv file. The key is the skill ID, the value is the skill.
     */
    public DwarfLoadSkillsEvent( HashMap<String, DwarfSkill> skills )
    {
        this.skills = skills;
    }

    /**
     * Gets the skills HashMap, the key is the skillID and the value is the DwarfSkill
     * 
     * @return DwarfPlayer
     */
    @SuppressWarnings( "unchecked" )
    public HashMap<Integer, DwarfSkill> getSkills()
    {
        return ( HashMap<Integer, DwarfSkill> ) skills.clone();
    }

    /**
     * Sets the skills that will be stored in DwarfCrafts memory.
     * 
     * @param skills The skills HashMap, The key is the skillID, the value is the DwarfSkill.
     */
    public void setSkills( HashMap<String, DwarfSkill> skills )
    {
        this.skills = skills;
    }

    /**
     * Adds a skill to the DwarfSkill HashMap that is stored in the DwarfCraft memory.
     * 
     * @param skill A skill to be added to the skills HashMap
     */
    public void addSkill( DwarfSkill skill )
    {
        skills.put( skill.getId(), skill );
    }

    /**
     * Removes a skill from the DwarfSkill HashMap that is stored in the DwarfCraft memory.
     * 
     * @param skill A skill to be removed from the skills HashMap
     */
    public void removeSkill( DwarfSkill skill )
    {
        skills.remove( skill.getId() );
    }
}
