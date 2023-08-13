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

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import com.jessy1237.dwarfcraft.DwarfCraft;

import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.AbstractNPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class DwarfTrainerTrait extends Trait
{

    private DwarfCraft plugin;
    private Material mHeldItem;
    @Persist( required = true )
    private String mSkillID = "";
    @Persist( required = true )
    private int mMaxLevel;
    @Persist( required = true )
    private int mMinLevel;

    @Override
    public void load( DataKey key )
    {
        if ( mSkillID.equals( "" ) )
            this.mSkillID = key.getString( "mSkillID" );
        if ( mSkillID.equals( "" ) )
            this.mMaxLevel = key.getInt( "mMaxLevel" );
        if ( mSkillID.equals( "" ) )
            this.mMinLevel = key.getInt( "mMinLevel" );
        loadHeldItem();

        // Adding the trainer to DwarfCraft DB
        DwarfTrainer trainer = new DwarfTrainer( plugin, ( AbstractNPC ) npc );
        plugin.getDataManager().trainerList.put( getNPC().getId(), trainer );
    }

    @Override
    public void onSpawn()
    {
        loadHeldItem();
    }

    @Override
    public void onRemove()
    {
        plugin.getDataManager().trainerList.remove( this.getNPC().getId() );
    }

    public DwarfTrainerTrait()
    {
        super( "DwarfTrainer" );
        this.plugin = ( DwarfCraft ) Bukkit.getServer().getPluginManager().getPlugin( "DwarfCraft" );
    }

    public DwarfTrainerTrait( DwarfCraft plugin, String skillID, Integer maxLevel, Integer minLevel )
    {
        super( "DwarfTrainer" );
        this.plugin = plugin;
        this.mSkillID = skillID;
        this.mMaxLevel = maxLevel;
        this.mMinLevel = minLevel;
        loadHeldItem();
    }

    @EventHandler
    public void onNPCLeftClick( NPCLeftClickEvent event )
    {
        if ( event.getNPC().hasTrait( DwarfTrainerTrait.class ) && event.getNPC().getId() == getNPC().getId() )
        {
            plugin.getDwarfEntityListener().onNPCLeftClickEvent( event );
        }
    }

    @EventHandler
    public void onNPCRightClick( NPCRightClickEvent event )
    {
        if ( event.getNPC().hasTrait( DwarfTrainerTrait.class ) && event.getNPC().getId() == getNPC().getId() )
        {
            plugin.getDwarfEntityListener().onNPCRightClickEvent( event );
        }
    }

    public int getMaxSkill()
    {
        return this.mMaxLevel;
    }

    public int getMinSkill()
    {
        return this.mMinLevel;
    }

    public String getSkillTrained()
    {
        return this.mSkillID;
    }

    public Material getMaterial()
    {
        if ( this.mHeldItem != null )
            return this.mHeldItem;
        else
            return Material.AIR;
    }

    public void loadHeldItem() {
        try {
            this.mHeldItem = plugin.getSkillManager().getSkill( this.mSkillID ).getTrainerHeldMaterial();
        }
        catch (NullPointerException e) {
            // NOP
        }

        if (getNPC() != null) {
            if (getNPC().isSpawned()) {
                if (this.mHeldItem != Material.AIR && this.mHeldItem != null) {
                    ((LivingEntity) getNPC().getEntity()).getEquipment().setItemInMainHand(new ItemStack(this.mHeldItem, 1));
                }
            }
        }
    }
}
