/*
 * Copyright (c) 2023.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft.data;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.jessy1237.dwarfcraft.DwarfCraft;

public final class ConfigManager
{
    private final DwarfCraft plugin;

    private final String configDirectory;
    
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

    protected static final HashMap<String, String> messages = new HashMap<>();
    protected static final ArrayList<String> tutorialBookPages = new ArrayList<String>();

    public ConfigManager( DwarfCraft plugin, String directory )
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
            File locale = new File( root + "/data/locale/", "en_US.yml" );
            if ( !locale.exists() )
                plugin.saveResource( "data/locale/en_US.yml", false );
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
    
        plugin.debugMessagesThreshold = config.getInt( "Debug Level" );
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
        plugin.getUtil().consoleLog( "Reading locale file: " + ChatColor.AQUA + configDirectory + "data/locale/" + "en_US.yml" );

        FileConfiguration localeConfig = YamlConfiguration.loadConfiguration( new File( plugin.getDataFolder() + "/data/locale/en_US.yml" ));

        // Welcome Messages
        addMessage("Welcome prefix", localeConfig);
        addMessage("Welcome", localeConfig);
        addMessage("Announcement Message", localeConfig);

        // Skillsheet Messages
        addMessage("Skillsheet.Header", localeConfig);
        addMessage("Skillsheet.Skill Line", localeConfig);
        addMessage("Skillsheet.Untrained Skill Header", localeConfig);
        addMessage("Skillsheet.Untrained Skill Line", localeConfig);

        // Skill Info Messages
        addMessage("Skill Info.Header", localeConfig);
        addMessage("Skill Info.Subheader", localeConfig);
        addMessage("Skill Info.Max Skill Level", localeConfig);
        addMessage("Skill Info.Max Trainer Level", localeConfig);
        addMessage("Skill Info.Train Cost Header", localeConfig);
        addMessage("Skill Info.Train Cost", localeConfig);

        // Race Messages
        addMessage("Race Messages.Race Info", localeConfig);
        addMessage("Race Messages.Admin Race Info", localeConfig);
        addMessage("Race Messages.Already Race", localeConfig);
        addMessage("Race Messages.Changed Race", localeConfig);
        addMessage("Race Messages.Confirm Race", localeConfig);
        addMessage("Race Messages.Race Failed", localeConfig);

        // Trainer Messages
        addMessage("Trainer Messages.Choose Race", localeConfig);
        addMessage("Trainer Messages.Train Skill Prefix", localeConfig);
        addMessage("Trainer Messages.Skill Blocked", localeConfig);
        addMessage("Trainer Messages.Non-Racial Skill", localeConfig);
        addMessage("Trainer Messages.Max Skill Level", localeConfig);
        addMessage("Trainer Messages.Max Level", localeConfig);
        addMessage("Trainer Messages.Level Too High", localeConfig);
        addMessage("Trainer Messages.No More Item Needed", localeConfig);
        addMessage("Trainer Messages.More Item Needed", localeConfig);
        addMessage("Trainer Messages.Training Successful", localeConfig);
        addMessage("Trainer Messages.Deposit Successful", localeConfig);
        addMessage("Trainer Messages.GUI Title", localeConfig);
        addMessage("Trainer Messages.Occupied", localeConfig);
        addMessage("Trainer Messages.Cooldown", localeConfig);

        // Effect Messages
        addMessage("Effect Descriptions.General", localeConfig);
        addMessage("Effect Descriptions.Explosion Damage", localeConfig);
        addMessage("Effect Descriptions.Fire Damage", localeConfig);
        addMessage("Effect Descriptions.Fall Damage", localeConfig);

        // Tutorial Book Pages
        tutorialBookPages.clear();
        tutorialBookPages.addAll( localeConfig.getStringList( "Tutorial Pages" ) );

        plugin.getUtil().consoleLog("Loaded " + ChatColor.AQUA + messages.size() + ChatColor.WHITE + " Message(s)");

        return true;
    }

    public void addMessage(String key, FileConfiguration localeConfig) {
        boolean hasKey = localeConfig.contains(key);
        if (hasKey) {
            messages.put(key, localeConfig.get(key).toString());
            plugin.getUtil().debugLog(4, Level.INFO, "Loaded locale message: " + ChatColor.AQUA + key);
        }
    }

    public static String getMessage(String key) {
        if (!messages.containsKey(key)) {
            System.out.println("Warning: Unable to find message with key: " + key);
        }
        return messages.get(key);
    }

    public static ArrayList<String> getTutorialBookPages() {
        return tutorialBookPages;
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
