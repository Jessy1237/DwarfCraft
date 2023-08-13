/*
 * Copyright (c) 2023.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ConfigManager
{

    private final DwarfCraft plugin;

    private final String configDirectory;

    public String dbType;
    public String host;
    public int port;
    public String database;
    public String username;
    public String password;
    
    private String dbpath;
    private Integer trainDelay;
    private Integer announcementInterval;
    private ArrayList<String> skillLevelCommands;
    private ArrayList<String> skillMasteryCommands;
    private ArrayList<String> skillMaxCapeCommands;
    private Integer maxLevel;
    private Integer raceLevelLimit;
    private String prefixStr;
    public String defaultRace;

    public ArrayList<World> worlds = new ArrayList<>();

    public boolean sendGreeting = false;
    public boolean disableCacti = true;
    public boolean worldBlacklist = false;
    public boolean silkTouch = true;
    public boolean vanilla = true;
    public boolean prefix = false;
    public boolean announce = false;
    public boolean byID = true;
    public boolean hardcorePenalty = true;
    public boolean spawnTutorialBook = true;

    protected ConfigManager( DwarfCraft plugin, String directory )
    {
        this.plugin = plugin;
        if ( !directory.endsWith( File.separator ) )
            directory += File.separator;
        configDirectory = directory;
        checkFiles( configDirectory );

        if ( !readLocaleFile() )
        {
            plugin.getUtil().consoleLog( "Failed to read locale file", Level.SEVERE );
            plugin.getServer().getPluginManager().disablePlugin( plugin );
        }
    }

    public String getDatabasePath()
    {
        return plugin.getDataFolder().getAbsolutePath() + "/dwarfcraft.db";
    }

    private void checkFiles( String path )
    {
        File root = new File( path );
        if ( !root.exists() )
            root.mkdirs();
        try
        {
            // Create Main Config File
            plugin.saveDefaultConfig();
            if ( !readConfigFile() )
            {
                plugin.getUtil().consoleLog( "Failed to read config file", Level.SEVERE );
                plugin.getServer().getPluginManager().disablePlugin( plugin );
            }

            // Create Data Files
            File locale = new File( root + "/data/dwarfcraft/locale/", "en_US.yml" );
            if ( !locale.exists() )
                plugin.saveResource( "data/dwarfcraft/locale/en_US.yml", false );
        }
        catch ( Exception e )
        {
            plugin.getUtil().consoleLog( "Could not verify files: " + e, Level.SEVERE );
            e.printStackTrace();
        }
    }

    private boolean readConfigFile()
    {
        plugin.getUtil().consoleLog( String.format("Reading config file: %sconfig.yml", ChatColor.AQUA + configDirectory) );

        skillLevelCommands = new ArrayList<>();
        skillMasteryCommands = new ArrayList<>();
        skillMaxCapeCommands = new ArrayList<>();

        FileConfiguration config = plugin.getConfig();

        dbType = config.getString( "Database Type" );
        host = config.getString( "MySQL Hostname" );
        port = config.getInt( "MySQL Port" );
        database = config.getString( "MySQL Database" );
        username = config.getString( "MySQL Username" );
        password = config.getString( "MySQL Password" );
    
        dbpath = config.getString( "Database File Name" );
        DwarfCraft.debugMessagesThreshold = config.getInt( "Debug Level" );
        sendGreeting = config.getBoolean( "Send Login Greet" );
        disableCacti = config.getBoolean( "Disable Farm Exploits" );
        worldBlacklist = config.getBoolean( "World Blacklist" );
        trainDelay = config.getInt( "Train Delay" );
        silkTouch = config.getBoolean( "Silk Touch" );
        defaultRace = config.getString( "Default Race" );
        vanilla = config.getBoolean( "Vanilla Race Enabled" );
        prefix = config.getBoolean( "Prefix Enabled" );
        prefixStr = config.getString( "Prefix" );
        maxLevel = config.getInt( "Max Skill Level" );
        raceLevelLimit = config.getInt( "Non-Racial Level Limit" );
        announce = config.getBoolean( "Announce Level Up" );
        announcementInterval = config.getInt( "Announcement Interval" );
        byID = config.getBoolean( "Sort DwarfTrainers by Unique ID" );
        hardcorePenalty = config.getBoolean( "Hardcore Race Change Penalty" );
        spawnTutorialBook = config.getBoolean( "Spawn Tutorial Book" );

        List<String> worldStrings = config.getStringList( "Disabled Worlds" );
        for ( String world : worldStrings )
            worlds.add( Bukkit.getServer().getWorld( world ) );

        clearCommands();

        skillLevelCommands.addAll( config.getStringList( "Skill Level Commands" ) );
        skillMasteryCommands.addAll( config.getStringList( "Skill Mastery Commands" ) );
        skillMaxCapeCommands.addAll( config.getStringList( "Skill Max Cape Commands" ) );

        return true;
    }

    private boolean readLocaleFile()
    {
        plugin.getUtil().consoleLog( "Reading locale file: " + ChatColor.AQUA + configDirectory + "data/dwarfcraft/locale/" + "en_US.yml" );

        FileConfiguration localeConfig = YamlConfiguration.loadConfiguration( new File( plugin.getDataFolder() + "/data/dwarfcraft/locale/en_US.yml" ));

        // Welcome Messages
        Messages.welcomePrefix = localeConfig.getString( "Welcome prefix" );
        Messages.welcome = localeConfig.getString( "Welcome" );
        Messages.announcementMessage = localeConfig.getString( "Announcement Message" );

        // Skillsheet Messages
        Messages.skillSheetHeader = localeConfig.getString( "Skillsheet.Header" );
        Messages.skillSheetSkillLine = localeConfig.getString( "Skillsheet.Skill Line" );
        Messages.skillSheetUntrainedSkillHeader = localeConfig.getString( "Skillsheet.Untrained Skill Header" );
        Messages.skillSheetUntrainedSkillLine = localeConfig.getString( "Skillsheet.Untrained Skill Line" );

        // Skill Info Messages
        Messages.skillInfoHeader = localeConfig.getString( "Skill Info.Header" );
        Messages.skillInfoMinorHeader = localeConfig.getString( "Skill Info.Subheader" );
        Messages.skillInfoMaxSkillLevel = localeConfig.getString( "Skill Info.Max Skill Level" );
        Messages.skillInfoAtTrainerLevel = localeConfig.getString( "Skill Info.Max Trainer Level" );
        Messages.skillInfoTrainCostHeader = localeConfig.getString( "Skill Info.Train Cost Header" );
        Messages.skillInfoTrainCost = localeConfig.getString( "Skill Info.Train Cost" );

        // Race Messages
        Messages.raceCheck = localeConfig.getString( "Race Messages.Race Info" );
        Messages.adminRaceCheck = localeConfig.getString( "Race Messages.Admin Race Info" );
        Messages.alreadyRace = localeConfig.getString( "Race Messages.Already Race" );
        Messages.changedRace = localeConfig.getString( "Race Messages.Changed Race" );
        Messages.confirmRace = localeConfig.getString( "Race Messages.Confirm Race" );
        Messages.raceDoesNotExist = localeConfig.getString( "Race Messages.Race Failed" );

        // Trainer Messages
        Messages.chooseARace = localeConfig.getString( "Trainer Messages.Choose Race" );
        Messages.trainSkillPrefix = localeConfig.getString( "Trainer Messages.Train Skill Prefix" );
        Messages.raceDoesNotContainSkill = localeConfig.getString( "Trainer Messages.Skill Blocked" );
        Messages.raceDoesNotSpecialize = localeConfig.getString( "Trainer Messages.Non-Racial Skill" );
        Messages.maxSkillLevel = localeConfig.getString( "Trainer Messages.Max Skill Level" );
        Messages.trainerMaxLevel = localeConfig.getString( "Trainer Messages.Max Level" );
        Messages.trainerLevelTooHigh = localeConfig.getString( "Trainer Messages.Level Too High" );
        Messages.noMoreItemNeeded = localeConfig.getString( "Trainer Messages.No More Item Needed" );
        Messages.moreItemNeeded = localeConfig.getString( "Trainer Messages.More Item Needed" );
        Messages.trainingSuccessful = localeConfig.getString( "Trainer Messages.Training Successful" );
        Messages.depositSuccessful = localeConfig.getString( "Trainer Messages.Deposit Successful" );
        Messages.trainerGUITitle = localeConfig.getString( "Trainer Messages.GUI Title" );
        Messages.trainerOccupied = localeConfig.getString( "Trainer Messages.Occupied" );
        Messages.trainerCooldown = localeConfig.getString( "Trainer Messages.Cooldown" );

        // Effect Messages
        Messages.describeGeneral = localeConfig.getString( "Effect Descriptions.General" );
        Messages.describeLevelExplosionDamageMore = localeConfig.getString( "Effect Descriptions.Explosion Damage (more)" );
        Messages.describeLevelExplosionDamageLess = localeConfig.getString( "Effect Descriptions.Explosion Damage (less)" );
        Messages.describeLevelFireDamageMore = localeConfig.getString( "Effect Descriptions.Fire Damage (more)" );
        Messages.describeLevelFireDamageLess = localeConfig.getString( "Effect Descriptions.Fire Damage (less)" );
        Messages.describeLevelFallingDamageMore = localeConfig.getString( "Effect Descriptions.Fall Damage (more)" );
        Messages.describeLevelFallingDamageLess = localeConfig.getString( "Effect Descriptions.Fall Damage (less)" );

        List<String> tutorialPages = localeConfig.getStringList( "Tutorial Pages" );
        // If there is at least one tutorial page, reset default messages and add custom messages
        if ( tutorialPages.size() > 0 )
        {
            Messages.tutorial.clear();
            Messages.tutorial.addAll( tutorialPages );
        }

        return true;
    }

    public String getPrefix()
    {
        return prefixStr;
    }

    public int getTrainDelay()
    {
        return trainDelay;
    }

    public int getMaxSkillLevel()
    {
        return maxLevel;
    }

    public int getRaceLevelLimit()
    {
        return raceLevelLimit;
    }

    public int getAnnouncementInterval()
    {
        return announcementInterval;
    }

    public ArrayList<String> getSkillLevelCommands()
    {
        return this.skillLevelCommands;
    }

    public ArrayList<String> getSkillMasteryCommands()
    {
        return this.skillMasteryCommands;
    }

    public ArrayList<String> getSkillMaxCapeCommands()
    {
        return this.skillMaxCapeCommands;
    }

    public void clearCommands()
    {
        this.skillLevelCommands = new ArrayList<>();
        this.skillMasteryCommands = new ArrayList<>();
        this.skillMaxCapeCommands = new ArrayList<>();
    }
}
