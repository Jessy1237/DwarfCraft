package com.Jessy1237.DwarfCraft.models;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import com.Jessy1237.DwarfCraft.DwarfCraft;

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
    private int mSkillID;
    @Persist( required = true )
    private int mMaxLevel;
    @Persist( required = true )
    private int mMinLevel;

    @Override
    public void load( DataKey key )
    {
        if ( mSkillID == 0 )
            this.mSkillID = key.getInt( "mSkillID" );
        if ( mSkillID == 0 )
            this.mMaxLevel = key.getInt( "mMaxLevel" );
        if ( mSkillID == 0 )
            this.mMinLevel = key.getInt( "mMinLevel" );
        loadHeldItem();

        // Adding the trainer to DwarfCraft DB
        DwarfTrainer trainer = new DwarfTrainer( plugin, ( AbstractNPC ) npc );
        plugin.getDataManager().trainerList.put( npc.getId(), trainer );
    }

    @Override
    public void onSpawn()
    {
        if ( this.mHeldItem != Material.AIR && this.mHeldItem != null )
        {
            ( ( LivingEntity ) getNPC().getEntity() ).getEquipment().setItemInMainHand( new ItemStack( this.mHeldItem, 1 ) );
        }
    }

    @Override
    public void onRemove()
    {
        plugin.getDataManager().trainerList.remove( this.npc.getId() );
    }

    public DwarfTrainerTrait()
    {
        super( "DwarfTrainer" );
        this.plugin = ( DwarfCraft ) Bukkit.getServer().getPluginManager().getPlugin( "DwarfCraft" );
    }

    public DwarfTrainerTrait( DwarfCraft plugin, Integer ID, Integer skillID, Integer maxLevel, Integer minLevel )
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

    public int getSkillTrained()
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

    private void loadHeldItem()
    {
        this.mHeldItem = plugin.getConfigManager().getGenericSkill( getSkillTrained() ).getTrainerHeldMaterial();

        if ( this.mHeldItem == null )
        {
            this.mHeldItem = Material.AIR;
        }
    }
}
