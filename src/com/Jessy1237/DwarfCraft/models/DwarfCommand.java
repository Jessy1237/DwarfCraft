package com.Jessy1237.DwarfCraft.models;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import net.milkbowl.vault.permission.Permission;

import com.Jessy1237.DwarfCraft.DwarfCraft;

public class DwarfCommand extends Command {
    protected final DwarfCraft plugin;
    protected String name;
    
    public DwarfCommand(String name) {
        super(name);
        this.plugin = DwarfCraft.getInstance();
        this.name = name;
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        return true;
    }

    public boolean hasPermission( CommandSender sender ) {
        if ( plugin.getCommandManager().getPermission() == null )
            return false;

        Permission perms = plugin.getCommandManager().getPermission();
        if ( sender instanceof Player)
        {
            if ( perms.has( ( Player ) sender, "dwarfcraft.*".toLowerCase() ) ) { return true; }
            if ( isOp() ) {
                return perms.has((Player) sender, ("dwarfcraft.op." + getName()).toLowerCase()) || sender.isOp();
            } else {
                return perms.has( ( Player ) sender, ( "dwarfcraft." + getName() ).toLowerCase() ) || sender.isOp();
            }
        } else return sender instanceof ConsoleCommandSender;
    }

    public boolean isOp() {
        return false;
    }

    public String getUsage() {
        return "";
    }

}
