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

import com.jessy1237.dwarfcraft.commands.*;
import com.jessy1237.dwarfcraft.data.DataManager;
import com.jessy1237.dwarfcraft.listeners.*;
import com.jessy1237.dwarfcraft.models.DwarfTrainerTrait;

public class DwarfCraft extends JavaPlugin
{
    private NPCRegistry npc_registry;
    private ConfigManager config_manager;
    private DataManager data_manager;
    private CommandManager command_manager;
    private DwarfManager dwarf_manager;
    private SkillManager skill_manager;
    private EffectRegistry effect_registry;
    private RaceManager race_manager;
    private Out out;
    private Util util;
    private Chat chat = null;
    public boolean isAuraActive = false;
    public int debugMessagesThreshold = 5;
    
    private final DwarfInventoryListener inventoryListener = new DwarfInventoryListener( this );
    private final DwarfEntityListener entityListener = new DwarfEntityListener( this );
    
    public NPCRegistry getNPCRegistry()
    {
        return npc_registry;
    }

    public ConfigManager getConfigManager()
    {
        return config_manager;
    }

    @Deprecated
    public DataManager getDataManager()
    {
        return data_manager;
    }

    public CommandManager getCommandManager() 
    { 
        return command_manager; 
    }

    public DwarfManager getDwarfManager()
    {
        return dwarf_manager;
    }

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
    
    @Override
    public void onDisable()
    {
        if(data_manager != null)
        data_manager.dbFinalize();
    }
    
    @Override
    public void onEnable()
    {
        onEnable( false );
    }

    public void onEnable( boolean reload ) {
        PluginManager pm = getServer().getPluginManager();
        util = new Util( this ); //Need to initialise Util earlier if going to use it in the enabling method

        if ( !checkDependencies() ) onDisable();
        if ( isEnabled() ) {
            config_manager = new ConfigManager( this, getDataFolder().getAbsolutePath() );
            dwarf_manager = new DwarfManager( this );
            race_manager = new RaceManager( this );
            effect_registry = new EffectRegistry();
            skill_manager = new SkillManager( this );

            race_manager.init(); // Races must be loaded before skills for validation
            skill_manager.init();

            data_manager = new DataManager( this, config_manager.dbType );
            data_manager.dbInitialize();
            dwarf_manager.init();
            
            command_manager = new CommandManager( this );
            out = new Out( this );
            
            // Creates the citizen trait for the DwarfTrainers
            if ( !reload )
            {
                pm.registerEvents( new DwarfPlayerListener( this ), this );
                pm.registerEvents( entityListener, this );
                pm.registerEvents( new DwarfBlockListener( this ), this );
                pm.registerEvents( new DwarfVehicleListener( this ), this );
                pm.registerEvents( inventoryListener, this );
                pm.registerEvents( new DwarfListener( this ), this );
        
                TraitInfo trainerTrait = TraitInfo.create(DwarfTrainerTrait.class).withName("DwarfTrainer");
                CitizensAPI.getTraitFactory().registerTrait(trainerTrait);
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

            command_manager.init();

            getUtil().consoleLog( String.format( "%s %s is enabled!", getDescription().getName(), getDescription().getVersion()), ChatColor.GREEN );

            // Log warning if the build is a Snapshot/Development build
            if ( this.getDescription().getVersion().contains("-SNAPSHOT") )
                getUtil().consoleLog( "*** WARNING: This is a development build. Please keep backups and update frequently. ***", Level.SEVERE );
        }
    }

    private boolean checkDependencies() {
        PluginManager pm = getServer().getPluginManager();
        if ( pm.getPlugin( "Vault" ) == null || !pm.getPlugin( "Vault" ).isEnabled() )
        {
            getUtil().consoleLog( "Something went wrong! Couldn't find Vault!", Level.SEVERE );
            getUtil().consoleLog( "Disabling DwarfCraft...", Level.SEVERE );
            return false;
        }

        if ( setupChat() )
            getUtil().consoleLog( "Success! Hooked into a Vault chat plugin!", ChatColor.GREEN );

        if ( pm.getPlugin( "Citizens" ) == null || !pm.getPlugin( "Citizens" ).isEnabled() )
        {
            getUtil().consoleLog( "Something went wrong! Couldn't find Citizens!", Level.SEVERE );
            getUtil().consoleLog( "Disabling DwarfCraft...", Level.SEVERE );
            return false;
        }

        getUtil().consoleLog( "Success! Hooked into Citizens!", ChatColor.GREEN );
        npc_registry = CitizensAPI.getNPCRegistry();

        if ( pm.getPlugin( "PlaceholderAPI" ) != null )
        {
            new PlaceholderHook().register();
            getUtil().consoleLog( "Success! Hooked into PlaceholderAPI!", ChatColor.GREEN );
        }

        return true;
    }
}
