/*
 * Copyright (c) 2018. DwarfCraft is an RPG plugin that allows players to improve their characters skills and capabilities through training, not experience. Authors: Jessy1237 and Drekryan Original
 * Authors: smartaleq, LexManos and RCarretta
 */

package com.Jessy1237.DwarfCraft.models;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Messages;
import com.Jessy1237.DwarfCraft.events.DwarfDepositEvent;
import com.Jessy1237.DwarfCraft.events.DwarfLevelUpEvent;
import com.Jessy1237.DwarfCraft.guis.TrainerGUI;

import net.citizensnpcs.api.npc.AbstractNPC;

public final class DwarfTrainer implements Comparable<DwarfTrainer>
{
    private AbstractNPC mEntity;
    private final DwarfCraft plugin;
    private boolean wait;
    private long lastTrain;

    public DwarfTrainer( final DwarfCraft plugin, AbstractNPC mEntity )
    {
        this.plugin = plugin;
        this.mEntity = mEntity;
        this.wait = false;
        this.lastTrain = 0;
    }

    @Override
    public boolean equals( Object that )
    {
        if ( this == that )
            return true;
        else if ( that instanceof DwarfTrainer )
            return ( getUniqueId() == ( ( DwarfTrainer ) that ).getUniqueId() );
        return false;
    }

    public AbstractNPC getEntity()
    {
        return mEntity;
    }

    public Location getLocation()
    {
        return mEntity.getStoredLocation();
    }

    protected Material getMaterial()
    {
        return mEntity.getTrait( DwarfTrainerTrait.class ).getMaterial();
    }

    public World getWorld()
    {
        return mEntity.getStoredLocation().getWorld();
    }

    public int getMaxSkill()
    {
        return mEntity.getTrait( DwarfTrainerTrait.class ).getMaxSkill();
    }

    public Integer getMinSkill()
    {
        return mEntity.getTrait( DwarfTrainerTrait.class ).getMinSkill();
    }

    public String getName()
    {
        return mEntity.getName();
    }

    public int getSkillTrained()
    {
        return mEntity.getTrait( DwarfTrainerTrait.class ).getSkillTrained();
    }

    public int getUniqueId()
    {
        return mEntity.getId();
    }

    public void depositOne( DwarfPlayer dCPlayer, ItemStack clickedItemStack, TrainerGUI trainerGUI )
    {
        DwarfSkill skill = dCPlayer.getSkill( getSkillTrained() );
        final int dep1 = skill.getDeposit( 1 ), dep2 = skill.getDeposit( 2 ), dep3 = skill.getDeposit( 3 );
        Player player = dCPlayer.getPlayer();
        List<ItemStack> trainingCostsToLevel = dCPlayer.calculateTrainingCost( skill ).get( 0 );
        String prefix = plugin.getPlaceHolderParser().parseByDwarfSkill( Messages.trainSkillPrefix, skill );

        boolean deposited = false;
        final PlayerInventory oldInv = player.getInventory();

        for ( int i = 0; i < trainingCostsToLevel.size(); i++ )
        {
            boolean isTag = skill.getItem( i ).isTag() && skill.getItem( i ).getMaterials().contains( clickedItemStack.getType() );
            if ( isTag || clickedItemStack.getType().equals( trainingCostsToLevel.get( i ).getType() ) )
            {
                    deposited = depositItem( trainingCostsToLevel.get( i ), skill.getItem( i + 1 ).getMaterials(), dCPlayer, trainerGUI, skill )[1];
                break;
            }
        }

        DwarfDepositEvent e = new DwarfDepositEvent( dCPlayer, this, skill );
        plugin.getServer().getPluginManager().callEvent( e );

        if ( e.isCancelled() )
        {
            skill.setDeposit( dep1, 1 );
            skill.setDeposit( dep2, 2 );
            skill.setDeposit( dep3, 3 );

            player.getInventory().setContents( oldInv.getContents() );
            player.getInventory().setExtraContents( oldInv.getExtraContents() );

            return;
        }

        if ( deposited )
        {
            plugin.getOut().sendMessage( player, Messages.depositSuccessful, prefix );
            DwarfSkill[] dCSkills = { skill };
            plugin.getDataManager().saveDwarfData( dCPlayer, dCSkills );
            player.getWorld().playSound( player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1.0f, 1.0f );
        }
    }

    public void depositAll( DwarfPlayer dCPlayer, TrainerGUI trainerGUI )
    {
        // TODO 1.13: Update method to support tags
        DwarfSkill skill = dCPlayer.getSkill( getSkillTrained() );
        final int dep1 = skill.getDeposit( 1 ), dep2 = skill.getDeposit( 2 ), dep3 = skill.getDeposit( 3 );
        Player player = dCPlayer.getPlayer();
        List<List<ItemStack>> costs = dCPlayer.calculateTrainingCost( skill );
        List<ItemStack> trainingCostsToLevel = costs.get( 0 );
        String tag = plugin.getPlaceHolderParser().parseByDwarfSkill( Messages.trainSkillPrefix, skill );

        final PlayerInventory oldInv = player.getInventory();
        boolean deposited = false;

        for ( int i = 0; i < trainingCostsToLevel.size(); i++ )
        {
                deposited = depositItem( trainingCostsToLevel.get( i ), skill.getItem( i + 1 ).getMaterials(), dCPlayer, trainerGUI, skill )[1];
        }

        DwarfDepositEvent e = new DwarfDepositEvent( dCPlayer, this, skill );
        plugin.getServer().getPluginManager().callEvent( e );

        if ( e.isCancelled() )
        {
            skill.setDeposit( dep1, 1 );
            skill.setDeposit( dep2, 2 );
            skill.setDeposit( dep3, 3 );

            player.getInventory().setContents( oldInv.getContents() );
            player.getInventory().setExtraContents( oldInv.getExtraContents() );

            return;
        }

        if ( deposited )
        {
            plugin.getOut().sendMessage( player, Messages.depositSuccessful, tag );
            DwarfSkill[] dCSkills = { skill };
            plugin.getDataManager().saveDwarfData( dCPlayer, dCSkills );
            player.getWorld().playSound( player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1.0f, 1.0f );
        }
    }

    public boolean trainSkill( DwarfPlayer dCPlayer, TrainerGUI trainerGUI )
    {
        // TODO 1.13: Update method to support tags
        DwarfSkill skill = dCPlayer.getSkill( getSkillTrained() );
        Player player = dCPlayer.getPlayer();
        List<List<ItemStack>> costs = dCPlayer.calculateTrainingCost( skill );
        List<ItemStack> trainingCostsToLevel = costs.get( 0 );
        String tag = plugin.getPlaceHolderParser().parseByDwarfSkill( Messages.trainSkillPrefix, skill );

        final PlayerInventory oldInv = player.getInventory();
        boolean hasMatsOrDeposits[] = { true, false };

        for ( int i = 0; i < trainingCostsToLevel.size(); i++ )
        {
                hasMatsOrDeposits = depositItem( trainingCostsToLevel.get( i ), skill.getItem( i + 1 ).getMaterials(), dCPlayer, trainerGUI, skill );
        }

        DwarfLevelUpEvent e = null;
        final int dep1 = skill.getDeposit( 1 ), dep2 = skill.getDeposit( 2 ), dep3 = skill.getDeposit( 3 );
        if ( hasMatsOrDeposits[0] )
        {
            skill.setLevel( skill.getLevel() + 1 );
            skill.setDeposit( 0, 1 );
            skill.setDeposit( 0, 2 );
            skill.setDeposit( 0, 3 );

            e = new DwarfLevelUpEvent( dCPlayer, this, skill );

            plugin.getServer().getPluginManager().callEvent( e );
            dCPlayer.runLevelUpCommands( skill );
        }
        if ( hasMatsOrDeposits[1] || hasMatsOrDeposits[0] )
        {

            if ( e != null )
            {
                if ( e.isCancelled() )
                {
                    skill.setLevel( skill.getLevel() - 1 );
                    skill.setDeposit( dep1, 1 );
                    skill.setDeposit( dep2, 2 );
                    skill.setDeposit( dep3, 3 );

                    player.getInventory().setContents( oldInv.getContents() );
                    player.getInventory().setExtraContents( oldInv.getExtraContents() );

                    return hasMatsOrDeposits[0];
                }
                else
                {
                    plugin.getOut().sendMessage( player, Messages.trainingSuccessful, tag );
                }
            }

            DwarfSkill[] dCSkills = new DwarfSkill[1];
            dCSkills[0] = skill;
            plugin.getDataManager().saveDwarfData( dCPlayer, dCSkills );
            trainerGUI.updateTitle();
            player.getWorld().playSound( player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.MASTER, 1.0f, 1.0f );
        }

        return hasMatsOrDeposits[0];

    }

    public boolean isWaiting()
    {
        return this.wait;
    }

    public long getLastTrain()
    {
        return this.lastTrain;
    }

    /**
     * Sets whether the player has to wait for the trainer to not be busy. A trainer is busy when another player is already talking to the trainer (i.e. has an inventory open)
     * 
     * @param wait True if the trainer will make the player wait
     */
    public void setWait( boolean wait )
    {
        this.wait = wait;
    }

    /**
     * Sets the last the time the trainer has leveled someone's skill up. This is allows for a cooldown between leveling players to allow the server to catchup.
     * 
     * @param lastTrain The time in milliseconds
     */
    public void setLastTrain( long lastTrain )
    {
        this.lastTrain = lastTrain;
    }

    public String getType()
    {
        return mEntity.getEntity().getType().toString();
    }

    /**
     * Checks if the player's inventory contains the required item or any equivalent item to train
     * 
     * @param costStack The cost stack to check against
     * @param player The player who is trying to skill up
     * @param skill DwarfSkill to check against as items may be tags
     * @return true if the player contains enough of the item or its equivalents otherwise false
     */
    private boolean containsItem( ItemStack costStack, Player player, DwarfSkill skill )
    {
        for ( int i = 0; i < 3; i++ )
        {
            boolean isTag = skill.getItem( i ).isTag() && skill.getItem( i ).getMaterials().contains( costStack.getType() );
            if ( isTag )
            {
                Set<Material> matches = skill.getItem( i ).getMaterials();
                for ( Material mat : matches )
                {
                    if ( player.getInventory().contains( mat ) )
                    {
                        return true;
                    }
                }
            }
            else
            {
                if ( player.getInventory().contains( costStack ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Cycles through he players inventory and removes the same or equivalent item to the costStack and removes that amount.
     * 
     * @param player The player to check the inventory
     * @param costStack The item to check against and remove
     * @param mats The set of materials to check against
     * @return The amount of removed from the players inventory
     */
    private int removeItem( Player player, ItemStack costStack, Set<Material> mats )
    {
        int amountTaken = 0;
        for ( ItemStack invStack : player.getInventory().getContents() )
        {
            if ( invStack == null )
                continue;
            if ( ( invStack.getType().equals( costStack.getType() ) && ( invStack.getDurability() == costStack.getDurability() || ( plugin.getUtil().isTool( invStack.getType() ) && invStack.getDurability() == invStack.getType().getMaxDurability() ) ) )
                    || ( mats.contains( costStack.getType() ) && mats.contains( invStack.getType() ) ) )
            {
                int inv = invStack.getAmount();
                int cost = costStack.getAmount();

                if ( cost - inv >= 0 )
                {
                    amountTaken += inv;
                    costStack.setAmount( cost - inv );
                    player.getInventory().removeItem( invStack );
                }
                else
                {
                    amountTaken += cost;
                    invStack.setAmount( inv - cost );
                    costStack.setAmount( 0 );
                }
            }
        }

        return amountTaken;
    }

    /**
     * Removes the required amount of the item and deposits into the trainer
     * 
     * @param costStack The item type and amount to be removed
     * @param dCPlayer The player that is depositing
     * @param trainerGUI The guis that flagged this deposit event
     * @param skill The skill that is being deposited into
     * @return True for [0] if the player had enough of the required item otherwise [0] false and True for [1] if any items were deposited into the skill otherwise false for [1]
     */
    private boolean[] depositItem( ItemStack costStack, Set<Material> mats, DwarfPlayer dCPlayer, TrainerGUI trainerGUI, DwarfSkill skill )
    {
        // TODO 1.13: Update method to support tags
        boolean[] hasMatsOrDeposits = { true, false };
        final int origCost = costStack.getAmount();
        Player player = dCPlayer.getPlayer();
        String prefix = plugin.getPlaceHolderParser().parseByDwarfSkill( Messages.trainSkillPrefix, skill );

        // Checks if the trainer has already accepted the required item
        if ( costStack.getAmount() == 0 )
        {
            plugin.getOut().sendMessage( player, Messages.noMoreItemNeeded.replaceAll( "%itemname%", plugin.getUtil().getCleanName( costStack ) ), prefix );
        }
        else
        {
            if ( containsItem( costStack, player, skill ) )
            {

                int amountTaken = removeItem( player, costStack, mats );

                // Settings true for items being deposited
                if ( amountTaken > 0 )
                    hasMatsOrDeposits[1] = true;

                trainerGUI.updateItem( costStack, origCost - amountTaken );

                if ( skill.getItem( 1 ).getMaterials().contains( costStack.getType() ) )
                {
                    skill.setDeposit( skill.getDeposit( 1 ) + amountTaken, 1 );
                }
                else if ( skill.getItem( 2 ).getMaterials().contains( costStack.getType() ) )
                {
                    skill.setDeposit( skill.getDeposit( 2 ) + amountTaken, 2 );
                }
                else if ( skill.getItem( 3 ).getMaterials().contains( costStack.getType() ) )
                {
                    skill.setDeposit( skill.getDeposit( 3 ) + amountTaken, 3 );
                }
            }
            else
            {
                hasMatsOrDeposits[0] = false;
                plugin.getOut().sendMessage( player, plugin.getPlaceHolderParser().parseForTrainCosts( Messages.moreItemNeeded, 0, costStack.getAmount(), 0, plugin.getUtil().getCleanName( costStack ) ), prefix );
                return hasMatsOrDeposits;
            }

            if ( costStack.getAmount() == 0 )
            {
                plugin.getOut().sendMessage( player, plugin.getPlaceHolderParser().parseForTrainCosts( Messages.noMoreItemNeeded, 0, costStack.getAmount(), 0, plugin.getUtil().getCleanName( costStack ) ), prefix );
            }
            else
            {
                plugin.getOut().sendMessage( player, plugin.getPlaceHolderParser().parseForTrainCosts( Messages.moreItemNeeded, 0, costStack.getAmount(), 0, plugin.getUtil().getCleanName( costStack ) ), prefix );
                hasMatsOrDeposits[0] = false;
                hasMatsOrDeposits[1] = true;
            }
        }

        return hasMatsOrDeposits;
    }

    /**
     * Compares the trainers by unique ID if configured to true otherwise compares by name
     */
    @Override
    public int compareTo( DwarfTrainer trainer )
    {
        return plugin.getConfigManager().byID ? ( getUniqueId() - trainer.getUniqueId() ) : ( getName().compareTo( trainer.getName() ) == 0 ? ( getUniqueId() - trainer.getUniqueId() ) : getName().compareTo( trainer.getName() ) );
    }
}