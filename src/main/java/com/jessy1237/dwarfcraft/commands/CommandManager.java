package com.jessy1237.dwarfcraft.commands;

import java.util.LinkedHashMap;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.permission.Permission;

import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.models.DwarfCommand;

public class CommandManager {
    private final DwarfCraft plugin;
    private Permission perms = null;
    private final LinkedHashMap<String, DwarfCommand> commands = new LinkedHashMap<>();

    public CommandManager( DwarfCraft plugin ) {
        this.plugin = plugin;
        try
        {
            if ( setupPermissions() )
                plugin.getUtil().consoleLog( "Hooked into a Vault permissions plugin!", ChatColor.GREEN );
        }
        catch ( Exception e )
        {
            plugin.getUtil().consoleLog( "Something went wrong! Unable to find a permissions plugin.", Level.SEVERE );
            plugin.onDisable();
        }
    }

    public void init() {
        plugin.getCommand("dwarfcraft").setExecutor( new DwarfCommandExecutor( plugin ) );
        plugin.getCommand("dwarfcraft").setTabCompleter( new DwarfCommandExecutor( plugin ) );
        registerCommands();
    }

    public void registerCommands() {
        registerCommand( new CommandSkillSheet( "skillsheet", plugin ) );
        registerCommand( new CommandTutorial( "tutorial", plugin ) );
        registerCommand( new CommandInfo( "info", plugin ) );
        registerCommand( new CommandSkill( "skill", plugin ) );
        registerCommand( new CommandRace( "race", plugin ) );
        registerCommand( new CommandHelp( "help", plugin ) );
        registerCommand( new CommandDebug( "debug", plugin ) );
        registerCommand( new CommandList( "list", plugin ) );
        registerCommand( new CommandSetSkill( "set_skill", plugin ) );
        registerCommand( new CommandCreate( "create", plugin ) );
        registerCommand( new CommandReload( "reload", plugin ) );
    }

    public void registerCommand( DwarfCommand command ) {
        commands.put( command.getName(), command );
    }

    public DwarfCommand getCommand( String name ) {
        return commands.get(name);
    }

    public LinkedHashMap<String, DwarfCommand> getAllCommands() {
        return commands;
    }

    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration( Permission.class );
        perms = rsp.getProvider();
        return perms != null;
    }

    private boolean isPermissionEnabled()
    {
        return perms != null;
    }

    public Permission getPermission()
    {
        return perms;
    }
}
