/*
 * Copyright (c) 2018.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.Jessy1237.DwarfCraft.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Placeholder;

public class DwarfPlayer
{
    private final DwarfCraft plugin;
    private HashMap<String, DwarfSkill> skills;
    private Player player;
    private DwarfRace race;
    private boolean raceMaster;

    public void setPlayer( Player player )
    {
        this.player = player;
    }

    public DwarfPlayer( final DwarfCraft plugin, Player player )
    {
        this.plugin = plugin;
        this.player = player;
        this.race = plugin.getRaceManager().getDefaultRace();
        this.skills = plugin.getSkillManager().getAllSkills();
        this.raceMaster = false;
    }

    public DwarfPlayer( final DwarfCraft plugin, Player player, DwarfRace race, boolean raceMaster )
    {
        this.plugin = plugin;
        this.player = player;
        this.race = race;
        this.skills = plugin.getSkillManager().getAllSkills();
        this.raceMaster = raceMaster;
    }

    public List<List<ItemStack>> calculateTrainingCost( DwarfSkill skill )
    {
        int highSkills = countHighSkills();
        int dwarfLevel = getDwarfLevel();
        int quartileSize = Math.min( 4, highSkills / 4 );
        int quartileNumber = 1; // 1 = top, 2 = 2nd, etc.
        int[] levelList = new int[highSkills + 1];
        List<ItemStack> costToLevelStack = new ArrayList<>();
        List<ItemStack> totalCostStack = new ArrayList<>();
        int i = 0;

        // Creates an ordered list of skill levels and finds where in that
        // list the skill is (what quartile)
        plugin.getUtil().debugLog( 0, Level.FINE, "Starting skill ordering for quartiles" );
        for ( DwarfSkill s : getSkills().values() )
        {
            if ( s.getLevel() > plugin.getConfigManager().getRaceLevelLimit() )
            {
                levelList[i] = s.getLevel();
                i++;
            }
        }
        Arrays.sort( levelList );
        if ( levelList[highSkills - quartileSize] <= skill.getLevel() )
            quartileNumber = 1;
        else if ( levelList[highSkills - 2 * quartileSize] <= skill.getLevel() )
            quartileNumber = 2;
        else if ( levelList[highSkills - 3 * quartileSize] <= skill.getLevel() )
            quartileNumber = 3;
        if ( skill.getLevel() < plugin.getConfigManager().getRaceLevelLimit() )
            quartileNumber = 1; // low skills train full speed

        // calculate quartile penalties for 2nd/3rd/4th quartile
        double multiplier = Math.max( 1, Math.pow( 1.072, ( skill.getLevel() - plugin.getConfigManager().getRaceLevelLimit() ) ) );
        if ( quartileNumber == 2 )
            multiplier *= ( 1 + 1 * dwarfLevel / ( 100 + 3 * dwarfLevel ) );
        if ( quartileNumber == 3 )
            multiplier *= ( 1 + 2 * dwarfLevel / ( 100 + 3 * dwarfLevel ) );
        if ( quartileNumber == 4 )
            multiplier *= ( 1 + 3 * dwarfLevel / ( 100 + 3 * dwarfLevel ) );

        // create output item stack of new items
        int item1Amount = ( ( int ) Math.min( Math.ceil( ( skill.getLevel() + 1 ) * skill.getItem( 1 ).getBase() * multiplier - .01 ), skill.getItem( 1 ).getMax() ) );
        int item2Amount = ( ( int ) Math.min( Math.ceil( ( skill.getLevel() + 1 ) * skill.getItem( 2 ).getBase() * multiplier - .01 ), skill.getItem( 2 ).getMax() ) );
        int item3Amount = ( ( int ) Math.min( Math.ceil( ( skill.getLevel() + 1 ) * skill.getItem( 3 ).getBase() * multiplier - .01 ), skill.getItem( 3 ).getMax() ) );

        if ( skill.getItem( 1 ).getDwarfItemHolder().getItemStack().getType() != Material.AIR )
        {
            totalCostStack.add( 0, new ItemStack( skill.getItem( 1 ).getDwarfItemHolder().getItemStack().getType(), item1Amount ) );
            costToLevelStack.add( 0, new ItemStack( skill.getItem( 1 ).getDwarfItemHolder().getItemStack().getType(), item1Amount - skill.getDeposit( 1 ) ) );
        }

        if ( skill.getItem( 2 ).getDwarfItemHolder().getItemStack().getType() != Material.AIR )
        {
            totalCostStack.add( 1, new ItemStack( skill.getItem( 2 ).getDwarfItemHolder().getItemStack().getType(), item2Amount ) );
            costToLevelStack.add( 1, new ItemStack( skill.getItem( 2 ).getDwarfItemHolder().getItemStack().getType(), item2Amount - skill.getDeposit( 2 ) ) );
        }
        if ( skill.getItem( 3 ).getDwarfItemHolder().getItemStack().getType() != Material.AIR )
        {
            totalCostStack.add( 2, new ItemStack( skill.getItem( 3 ).getDwarfItemHolder().getItemStack().getType(), item3Amount ) );
            costToLevelStack.add( 2, new ItemStack( skill.getItem( 3 ).getDwarfItemHolder().getItemStack().getType(), item3Amount - skill.getDeposit( 3 ) ) );
        }
        List<List<ItemStack>> costs = new ArrayList<>();

        costs.add( 0, costToLevelStack );
        costs.add( 1, totalCostStack );
        return costs;
    }

    /**
     * Counts skills greater than level the race level limit, used for training costs
     */
    private int countHighSkills()
    {
        int highCount = 0;
        for ( DwarfSkill s : getSkills().values() )
        {
            if ( s.getLevel() > plugin.getConfigManager().getRaceLevelLimit() )
                highCount++;
        }
        return highCount;
    }

    /**
     * Calculates the dwarf's total level for display. Value is the total of all skill level above the race level limit, or the highest skill level when none are above the race level limit.
     * 
     * @return
     */
    public int getDwarfLevel()
    {
        int playerLevel = plugin.getConfigManager().getRaceLevelLimit();
        int highestSkill = 0;
        for ( DwarfSkill s : getSkills().values() )
        {
            if ( s.getLevel() > highestSkill )
                highestSkill = s.getLevel();

            if ( s.getLevel() > plugin.getConfigManager().getRaceLevelLimit() )
                playerLevel = playerLevel + s.getLevel() - plugin.getConfigManager().getRaceLevelLimit();
        }
        if ( playerLevel == plugin.getConfigManager().getRaceLevelLimit() )
            playerLevel = highestSkill;
        return playerLevel;
    }

    public Player getPlayer()
    {
        return player;
    }

    /**
     * Gets a dwarf's skill from an effect
     * 
     * @param effect (does not have to be this dwarf's effect, only used for ID#)
     * @return DwarfSkill or null if none found
     */
//    public DwarfSkill getSkill( DwarfEffect effect )
//    {
//        for ( DwarfSkill skill : skills.values() )
//        {
//            if ( skill.getId() == effect.getId() / 10 )
//                return skill;
//        }
//        return null;
//    }

    /**
     * Gets a dwarf's skill by skill_id
     * 
     * @param skill_id
     * @return DwarfSkill or null if none found
     */
    public DwarfSkill getSkill( String skill_id )
    {
        DwarfSkill skill = skills.get( skill_id );
        return skill;
    }

    public HashMap<String, DwarfSkill> getSkills()
    {
        return skills;
    }

    /**
     * Calculates the Dwarf's total Level
     * 
     * @return total level
     */
    public int level()
    {
        int playerLevel = plugin.getConfigManager().getRaceLevelLimit();
        int highestSkill = 0;
        for ( DwarfSkill s : getSkills().values() )
        {
            if ( s.getLevel() > highestSkill )
                highestSkill = s.getLevel();
            if ( s.getLevel() > plugin.getConfigManager().getRaceLevelLimit() )
                playerLevel += s.getLevel() - plugin.getConfigManager().getRaceLevelLimit();
        }
        if ( playerLevel == plugin.getConfigManager().getRaceLevelLimit() )
            playerLevel = highestSkill;
        return playerLevel;
    }

    /**
     * @param skills the skills to set
     */
    public void setSkills( HashMap<String, DwarfSkill> skills )
    {
        this.skills = skills;
    }

    public int getSkillLevel( String skill_id )
    {
        for ( DwarfSkill s : getSkills().values() )
            if ( s.getId().equals( skill_id ) )
                return s.getLevel();
        return 0;
    }

    public void changeRace( String race )
    {
        final DwarfRace oldRace = this.race;
        this.race = plugin.getRaceManager().getRace( race );
        DwarfSkill[] dCSkills = new DwarfSkill[skills.size()];

        int I = 0;
        // Resets the players skills
        for ( DwarfSkill skill : skills.values() )
        {
            if ( plugin.getConfigManager().hardcorePenalty )
            {
                if ( race.equalsIgnoreCase( "Vanilla" ) )
                {
                    skill.setLevel( 5 );
                }
                else
                {
                    skill.setLevel( 0 );
                }

                skill.setDeposit( 0, 1 );
                skill.setDeposit( 0, 2 );
                skill.setDeposit( 0, 3 );
            }
            else
            {
                if ( race.equalsIgnoreCase( "Vanilla" ) )
                {
                    skill.setLevel( 5 );
                }
                else
                {
                    if ( !plugin.getSkillManager().getSkill( skill.getId() ).getRaces().containsValue( race ) && skill.getLevel() > plugin.getConfigManager().getRaceLevelLimit() )
                    {
                        skill.setLevel( plugin.getConfigManager().getRaceLevelLimit() );
                        skill.setDeposit( 0, 1 );
                        skill.setDeposit( 0, 2 );
                        skill.setDeposit( 0, 3 );
                    }
                }
            }
            dCSkills[I] = skill;
            I++;
        }

        // Resets the players prefix
        if ( plugin.isChatEnabled() )
            if ( !oldRace.getId().isEmpty() )
                if ( plugin.getChat().getPlayerPrefix( getPlayer() ).contains( plugin.getUtil().getPlayerPrefix( oldRace.getName() ) ) )
                    plugin.getChat().setPlayerPrefix( getPlayer(), plugin.getChat().getPlayerPrefix( getPlayer() ).replace( plugin.getUtil().getPlayerPrefix( oldRace.getName() ), plugin.getUtil().getPlayerPrefix( this ) ) );

        plugin.getDataManager().saveDwarfData( this, dCSkills );
    }

    public DwarfRace getRace()
    {
        return race;
    }

    public void setRace( String race )
    {
        if (race.isEmpty() )
            this.race = new DwarfRace("", "");
        else
            this.race = plugin.getRaceManager().getRace( race );
    }

    public boolean isRaceMaster()
    {
        return raceMaster;
    }

    public void setRaceMaster( boolean raceMaster )
    {
        this.raceMaster = raceMaster;
    }

    public boolean isMax()
    {
        boolean isMax = true;
        for ( DwarfSkill s : skills.values() )
        {
            if ( s.getLevel() < s.getMaxLevel( this ) )
            {
                isMax = false;
            }
        }

        return isMax;
    }

    public boolean isDwarfCraftDev()
    {
        ArrayList<UUID> uuids = new ArrayList<>();
        uuids.add( UUID.fromString( "83a00245-b186-4cda-a11d-c0c5fff4da1f" ) );
        uuids.add( UUID.fromString( "193fef41-cfe9-4d35-b1f5-40fa23410e93" ) );
        return uuids.contains( this.player.getUniqueId() );
    }

    public void runLevelUpCommands( DwarfSkill skill )
    {
        if ( plugin.getConfigManager().getSkillLevelCommands().size() > 0 )
        {
            ArrayList<String> commands;

            if ( !plugin.getSkillManager().getAllSkills().containsValue( skill ) )
            {
                return;
            }

            if ( isMax() )
            {
                commands = plugin.getConfigManager().getSkillMaxCapeCommands();
            }
            else if ( skill.getLevel() >= skill.getMaxLevel( this ) )
            {
                commands = plugin.getConfigManager().getSkillMasteryCommands();
            }
            else
            {
                commands = plugin.getConfigManager().getSkillLevelCommands();
            }

            for ( String command : commands )
            {
                String playerPosition = player.getLocation().getX() + " " + player.getLocation().getY() + " " + player.getLocation().getZ();

                command = command.replaceAll( "<player.pos>", playerPosition ).replaceAll( "<world.name>", player.getWorld().getName() );
                command = skill.description( command, this );
                command = ChatColor.translateAlternateColorCodes( '&', command );

                plugin.getServer().dispatchCommand( plugin.getServer().getConsoleSender(), command );
            }

            commands.clear();
        }
    }
    
    public String toString( String text )
    {
        return text.replaceAll( Placeholder.PLAYER_LEVEL.value(), "" + this.getDwarfLevel() ).replaceAll( Placeholder.PLAYER_NAME.value(), this.getPlayer().getDisplayName() )
                .replaceAll( Placeholder.PLAYER_RACE.value(), this.getRace().getName());
    }
}
