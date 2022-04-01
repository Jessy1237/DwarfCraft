/*
 * Copyright (c) 2018.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.Jessy1237.DwarfCraft;

import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.TraitInfo;
import net.milkbowl.vault.chat.Chat;

import com.Jessy1237.DwarfCraft.commands.*;
import com.Jessy1237.DwarfCraft.data.DataManager;
import com.Jessy1237.DwarfCraft.listeners.*;
import com.Jessy1237.DwarfCraft.models.DwarfTrainerTrait;

public class DwarfCraft extends JavaPlugin
{
    private PluginManager pm;
    private NPCRegistry npc_registry;
    private ConfigManager config_manager;
    private DataManager data_manager;
    private CommandManager command_manager;
    private SkillManager skill_manager;
    private EffectRegistry effect_registry;
    private RaceManager race_manager;
    private Out out;
    private Util util;
    private Chat chat = null;
    private TraitInfo trainerTrait;
    public boolean isAuraActive = false;
    public static int debugMessagesThreshold = 10;

    private final DwarfBlockListener blockListener = new DwarfBlockListener( this );
    private final DwarfPlayerListener playerListener = new DwarfPlayerListener( this );
    private final DwarfEntityListener entityListener = new DwarfEntityListener( this );
    private final DwarfVehicleListener vehicleListener = new DwarfVehicleListener( this );
    private final DwarfInventoryListener inventoryListener = new DwarfInventoryListener( this );
    private final DwarfListener dwarfListener = new DwarfListener( this );

    public NPCRegistry getNPCRegistry()
    {
        return npc_registry;
    }

    public ConfigManager getConfigManager()
    {
        return config_manager;
    }

    public DataManager getDataManager()
    {
        return data_manager;
    }

    public CommandManager getCommandManager() { return command_manager; }

    public SkillManager getSkillManager()
    {
        return skill_manager;
    }

    public RaceManager getRaceManager()
    {
        return race_manager;
    }

    public Out getOut()
    {
        return out;
    }

    public Util getUtil()
    {
        return util;
    }
    
    public EffectRegistry getEffectRegistry()
    {
        return effect_registry;
    }
    
    public DwarfEntityListener getDwarfEntityListener()
    {
        return entityListener;
    }

    public DwarfInventoryListener getDwarfInventoryListener()
    {
        return inventoryListener;
    }

    boolean setupChat()
    {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration( Chat.class );
        chat = rsp.getProvider();
        return chat != null;
    }

    public boolean isChatEnabled()
    {
        return chat != null;
    }

    public Chat getChat()
    {
        return chat;
    }

    public TraitInfo getTrainerTrait()
    {
        return trainerTrait;
    }

    /**
     * Called upon disabling the plugin.
     */
    @Override
    public void onDisable()
    {
        if(data_manager != null)
        data_manager.dbFinalize();
    }

    /**
     * Called upon enabling the plugin
     */
    @Override
    public void onEnable()
    {
        onEnable( false );
    }

    public void onEnable( boolean reload ) {
        pm = getServer().getPluginManager();
        util = new Util( this ); //Need to initialise Util earlier if going to use it in the enabling method
        race_manager = new RaceManager( this );
        effect_registry = new EffectRegistry( this );
        skill_manager = new SkillManager( this );

        if ( !checkDependencies() ) onDisable();

        config_manager = new ConfigManager( this, getDataFolder().getAbsolutePath() );
        Registration.init();
        race_manager.init(); // Races must be loaded before skills for validation
        skill_manager.init();

        data_manager = new DataManager( this, config_manager.dbType );
        data_manager.dbInitialize();

        command_manager = new CommandManager( this );
        out = new Out( this );
        
        // Creates the citizen trait for the DwarfTrainers
        if ( !reload )
        {
            pm.registerEvents( playerListener, this );
            pm.registerEvents( entityListener, this );
            pm.registerEvents( blockListener, this );
            pm.registerEvents( vehicleListener, this );
            pm.registerEvents( inventoryListener, this );
            pm.registerEvents( dwarfListener, this );

            trainerTrait = TraitInfo.create( DwarfTrainerTrait.class ).withName( "DwarfTrainer" );
            CitizensAPI.getTraitFactory().registerTrait( trainerTrait );
        }
        else
        {
            util.reloadTrainers();
            this.getConfigManager().clearCommands();
        }

        getServer().getScheduler().runTaskAsynchronously( this, () -> {
            getUtil().removePlayerPrefixes();
            for (Player player : getServer().getOnlinePlayers()) {
                getUtil().setPlayerPrefix(player);
            }
        });

        if ( isEnabled() ) {
            Objects.requireNonNull(this.getCommand("dwarfcraft")).setExecutor( new DwarfCommandExecutor( this ) );
            Objects.requireNonNull(this.getCommand("dwarfcraft")).setTabCompleter( new DwarfCommandExecutor( this ) );
            command_manager.registerCommand( new CommandSkillSheet( this, "skillsheet" ) );
            command_manager.registerCommand( new CommandTutorial( this, "tutorial" ) );
            command_manager.registerCommand( new CommandInfo( this, "info" ) );
            command_manager.registerCommand( new CommandSkill( this, "skill" ) );
            command_manager.registerCommand( new CommandRace( this, "race" ) );
            command_manager.registerCommand( new CommandHelp( this, "help" ) );
            command_manager.registerCommand( new CommandDebug( this, "debug" ) );
            command_manager.registerCommand( new CommandList( this, "list" ) );
            command_manager.registerCommand( new CommandSetSkill( this, "setskill" ) );
            command_manager.registerCommand( new CommandCreate( this, "create" ) );
            command_manager.registerCommand( new CommandReload( this, "reload" ) );
        }

        getUtil().consoleLog( Level.INFO, ChatColor.GREEN + getDescription().getName() + " " + getDescription().getVersion() + " is enabled!" );

        // Log warning if the build is a Snapshot/Development build
        if ( this.getDescription().getVersion().contains("-SNAPSHOT") )
            getUtil().consoleLog( Level.SEVERE, "*** WARNING: This is a development build. Please keep backups and update frequently. ***" );
    }

    private boolean checkDependencies() {
        if ( pm.getPlugin( "Vault" ) == null || !pm.getPlugin( "Vault" ).isEnabled() )
        {
            getUtil().consoleLog( Level.SEVERE, "Something went wrong! Couldn't find Vault!" );
            getUtil().consoleLog( Level.SEVERE, "Disabling DwarfCraft..." );
            return false;
        }

        if ( setupChat() )
            getUtil().consoleLog( Level.INFO, ChatColor.GREEN + "Success! Hooked into a Vault chat plugin!" );

        if ( pm.getPlugin( "Citizens" ) == null || !pm.getPlugin( "Citizens" ).isEnabled() )
        {
            getUtil().consoleLog( Level.SEVERE, "Something went wrong! Couldn't find Citizens!" );
            getUtil().consoleLog( Level.SEVERE, "Disabling DwarfCraft..." );
            return false;
        }

        getUtil().consoleLog( Level.INFO, ChatColor.GREEN + "Success! Hooked into Citizens!" );
        npc_registry = CitizensAPI.getNPCRegistry();

        if ( pm.getPlugin( "PlaceholderAPI" ) != null )
        {
            new PlaceholderHook().register();
            getUtil().consoleLog( Level.INFO, ChatColor.GREEN + "Success! Hooked into PlaceholderAPI!" );
        }

        return true;
    }
}
