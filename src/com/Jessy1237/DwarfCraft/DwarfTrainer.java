package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.ArrayList;
import java.util.List;

import net.citizensnpcs.api.npc.AbstractNPC;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.Jessy1237.DwarfCraft.events.DwarfCraftLevelUpEvent;

public final class DwarfTrainer
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
        else if ( that instanceof HumanEntity )
            return ( mEntity.getId() == ( ( HumanEntity ) that ).getEntityId() );
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

    protected int getMaterial()
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

    protected String getMessage()
    {
        return mEntity.getTrait( DwarfTrainerTrait.class ).getMessage();
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

    public boolean isGreeter()
    {
        return mEntity.getTrait( DwarfTrainerTrait.class ).isGreeter();
    }

    public void printLeftClick( Player player )
    {
        GreeterMessage msg = plugin.getDataManager().getGreeterMessage( getMessage() );
        if ( msg != null )
        {
            plugin.getOut().sendMessage( player, msg.getLeftClickMessage() );
        }
        else
        {
            System.out.println( String.format( "[DC] Error: Greeter %s has no left click message. Check your configuration file for message ID %d", getUniqueId(), getMessage() ) );
        }
        return;
    }

    public void printRightClick( Player player )
    {
        GreeterMessage msg = plugin.getDataManager().getGreeterMessage( getMessage() );
        if ( msg != null )
        {
            plugin.getOut().sendMessage( player, msg.getRightClickMessage() );
        }
        return;
    }

    @SuppressWarnings( { "unused", "deprecation" } )
    public void trainSkill( DCPlayer dCPlayer )
    {
        Skill skill = dCPlayer.getSkill( getSkillTrained() );
        Player player = dCPlayer.getPlayer();
        String tag = Messages.trainSkillPrefix.replaceAll( "%skillid%", "" + skill.getId() );

        if ( dCPlayer.getRace().equalsIgnoreCase( "NULL" ) )
        {
            plugin.getOut().sendMessage( player, Messages.chooseARace );
            setWait( false );
            return;
        }

        if ( skill == null )
        {
            plugin.getOut().sendMessage( player, Messages.raceDoesNotContainSkill, tag );
            setWait( false );
            return;
        }

        if ( skill.getLevel() >= plugin.getConfigManager().getRaceLevelLimit() && !plugin.getConfigManager().getAllSkills( dCPlayer.getRace() ).contains( skill.getId() ) )
        {
            plugin.getOut().sendMessage( player, Messages.raceDoesNotSpecialize.replaceAll( "%racelevellimit%", "" + plugin.getConfigManager().getRaceLevelLimit() ), tag );
            setWait( false );
            return;
        }

        if ( skill.getLevel() >= plugin.getConfigManager().getMaxSkillLevel() )
        {
            plugin.getOut().sendMessage( player, Messages.maxSkillLevel.replaceAll( "%maxskilllevel%", "" + plugin.getConfigManager().getMaxSkillLevel() ), tag );
            setWait( false );
            return;
        }

        if ( skill.getLevel() >= getMaxSkill() )
        {
            plugin.getOut().sendMessage( player, Messages.trainerMaxLevel, tag );
            setWait( false );
            return;
        }

        if ( skill.getLevel() < getMinSkill() )
        {
            plugin.getOut().sendMessage( player, Messages.trainerLevelTooHigh, tag );
            setWait( false );
            return;
        }

        List<List<ItemStack>> costs = dCPlayer.calculateTrainingCost( skill );
        List<ItemStack> trainingCostsToLevel = costs.get( 0 );
        // List<ItemStack> totalCostsToLevel = costs.get(1);

        boolean hasMats = true;
        boolean deposited = false;

        final PlayerInventory oldInv = player.getInventory();

        int cost1 = 0, cost2 = 0, cost3 = 0;

        for ( ItemStack costStack : trainingCostsToLevel )
        {
            if ( costStack == null )
            {
                continue;
            }
            if ( costStack.getAmount() == 0 )
            {
                plugin.getOut().sendMessage( player, Messages.noMoreItemNeeded.replaceAll( "%itemname%", plugin.getUtil().getCleanName( costStack ) ), tag );
                continue;
            }
            if ( !player.getInventory().contains( costStack.getTypeId() ) )
            {
                if ( plugin.getUtil().checkEquivalentBuildBlocks( costStack.getTypeId(), -1 ) != null )
                {
                    ArrayList<Integer> i = plugin.getUtil().checkEquivalentBuildBlocks( costStack.getTypeId(), -1 );
                    boolean contains = false;
                    for ( int id : i )
                    {
                        if ( player.getInventory().contains( id ) )
                        {
                            contains = true;
                        }
                    }
                    if ( !contains )
                    {
                        hasMats = false;
                        plugin.getOut().sendMessage( player, Messages.moreItemNeeded.replaceAll( "%costamount%", "" + costStack.getAmount() ).replaceAll( "%itemname%", plugin.getUtil().getCleanName( costStack ) ), tag );
                        continue;
                    }
                }
                else
                {
                    hasMats = false;
                    plugin.getOut().sendMessage( player, Messages.moreItemNeeded.replaceAll( "%costamount%", "" + costStack.getAmount() ).replaceAll( "%itemname%", plugin.getUtil().getCleanName( costStack ) ), tag );
                    continue;
                }
            }

            for ( ItemStack invStack : player.getInventory().getContents() )
            {
                if ( invStack == null )
                    continue;
                if ( ( invStack.getTypeId() == costStack.getTypeId() && ( invStack.getDurability() == costStack.getDurability() || ( plugin.getUtil().isTool( invStack.getTypeId() ) && invStack.getDurability() == invStack.getType().getMaxDurability() ) ) )
                        || plugin.getUtil().checkEquivalentBuildBlocks( invStack.getTypeId(), costStack.getTypeId() ) != null )
                {
                    deposited = true;
                    int inv = invStack.getAmount();
                    int cost = costStack.getAmount();
                    int delta;

                    if ( costStack.getType().equals( skill.Item1.Item.getType() ) )
                    {
                        cost1 = costStack.getAmount();
                    }
                    if ( costStack.getType().equals( skill.Item2.Item.getType() ) )
                    {
                        cost2 = costStack.getAmount();
                    }
                    if ( costStack.getType().equals( skill.Item3.Item.getType() ) )
                    {
                        cost3 = costStack.getAmount();
                    }

                    if ( cost - inv >= 0 )
                    {
                        costStack.setAmount( cost - inv );
                        player.getInventory().removeItem( invStack );
                        delta = inv;
                    }
                    else
                    {
                        costStack.setAmount( 0 );
                        invStack.setAmount( inv - cost );
                        delta = cost;
                    }

                    if ( costStack.getType().equals( skill.Item1.Item.getType() ) )
                    {
                        skill.setDeposit1( skill.getDeposit1() + delta );
                    }
                    else if ( costStack.getType().equals( skill.Item2.Item.getType() ) )
                    {
                        skill.setDeposit2( skill.getDeposit2() + delta );
                    }
                    else
                    {
                        skill.setDeposit3( skill.getDeposit3() + delta );
                    }
                }
            }
            if ( costStack.getAmount() == 0 )
            {
                plugin.getOut().sendMessage( player, Messages.noMoreItemNeeded.replaceAll( "%itemname%", plugin.getUtil().getCleanName( costStack ) ), tag );
            }
            else
            {
                plugin.getOut().sendMessage( player, Messages.moreItemNeeded.replaceAll( "%costamount%", "" + costStack.getAmount() ).replaceAll( "%itemname%", plugin.getUtil().getCleanName( costStack ) ), tag );
                hasMats = false;
                deposited = true;
            }

        }

        DwarfCraftLevelUpEvent e = null;
        final int dep1 = skill.getDeposit1(), dep2 = skill.getDeposit2(), dep3 = skill.getDeposit3();
        if ( hasMats )
        {
            skill.setLevel( skill.getLevel() + 1 );
            skill.setDeposit1( skill.getDeposit1() - cost1 );
            skill.setDeposit2( skill.getDeposit2() - cost2 );
            skill.setDeposit3( skill.getDeposit3() - cost3 );

            e = new DwarfCraftLevelUpEvent( dCPlayer, this, skill );

            plugin.getServer().getPluginManager().callEvent( e );
        }
        if ( deposited || hasMats )
        {

            if ( e != null )
            {
                if ( e.isCancelled() )
                {
                    skill.setLevel( skill.getLevel() - 1 );
                    skill.setDeposit1( dep1 );
                    skill.setDeposit2( dep2 );
                    skill.setDeposit3( dep3 );

                    player.getInventory().setContents( oldInv.getContents() );
                    player.getInventory().setExtraContents( oldInv.getExtraContents() );

                    setWait( false );

                    return;
                }
                else
                {
                    plugin.getOut().sendMessage( player, Messages.trainingSuccessful, tag );
                }
            }

            Skill[] dCSkills = new Skill[1];
            dCSkills[0] = skill;
            plugin.getDataManager().saveDwarfData( dCPlayer, dCSkills );
        }

        setWait( false );
    }

    public boolean isWaiting()
    {
        return this.wait;
    }

    public long getLastTrain()
    {
        return this.lastTrain;
    }

    public void setWait( boolean wait )
    {
        this.wait = wait;
    }

    public void setLastTrain( long lastTrain )
    {
        this.lastTrain = lastTrain;
    }

    public String getType()
    {
        return mEntity.getEntity().getType().toString();
    }
}