/*
 * Copyright (c) 2023.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.md_5.bungee.api.ChatColor;

import com.jessy1237.dwarfcraft.ConfigManager;
import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.models.DwarfCommand;
import com.jessy1237.dwarfcraft.models.DwarfPlayer;
import com.jessy1237.dwarfcraft.models.DwarfSkill;

public class CommandSkill extends DwarfCommand implements TabCompleter
{
    public CommandSkill( String name, DwarfCraft plugin )
    {
        super( name, plugin );
        setDescription("Displays a description of a dwarf's skill and training costs.");
    }

    @Override
    public boolean execute( CommandSender sender, String commandLabel, String[] args )
    {
        plugin.getUtil().debugLog( 1, Level.FINE, "Started command 'skill'" );

        if ( args.length == 0 || args == null )
        {
            plugin.getOut().sendMessage( sender, getUsage() );
        }
        else if ( args[0].equalsIgnoreCase( "?" ) )
        {
            plugin.getOut().sendMessage( sender, description );
        }
        else
        {
            String playerName;
            if (args.length == 1 && (!(sender instanceof Player))) {
                // Sender is console and player not provided
                sender.sendMessage("Player must be specified when run from console");
                sender.sendMessage( getUsage() );
                return true;
            }

            if (args.length == 1) {
                playerName = sender.getName();
            } else {
                playerName = args[0];
            }

            Player player = sender.getServer().getPlayer( playerName );
            if (player == null) {
                plugin.getOut().sendMessage( sender, "Unable to locate player");
                return true;
            }
            DwarfPlayer dwarfPlayer = new DwarfPlayer(plugin, player);

            DwarfSkill skill;
            if (args.length == 1 && player != null)
                skill = dwarfPlayer.getSkill(args[0]);
            else 
                skill = dwarfPlayer.getSkill(args[1]);

            if ( dwarfPlayer.getRace() == null )
            {
                if ((sender instanceof Player)) {
                    plugin.getOut().sendMessage( sender, ConfigManager.getMessage("Trainer Messages.Choose Race") );
                } else {
                    plugin.getOut().sendMessage(sender, "Player has no data");
                }
                return true;
            }

            plugin.getOut().printSkillInfo( sender, skill, dwarfPlayer, plugin.getConfigManager().getMaxSkillLevel() );
            return true;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete( CommandSender commandSender, Command command, String s, String[] args )
    {
        if ( !command.getName().equalsIgnoreCase( "dwarfcraft" ) || !plugin.isEnabled() )
            return null;

        ArrayList<String> completions = new ArrayList<>();
        ArrayList<String> matches = new ArrayList<>();
        if ( args.length == 2 )
        {
            completions.clear();
            matches.clear();

            for ( Player player : plugin.getServer().getOnlinePlayers() )
            {
                // Strip Colours from Display Names from silly plugins that add it to the Player Name instead of using prefixes
                completions.add( ChatColor.stripColor( player.getPlayerListName() ) );
            }

            return StringUtil.copyPartialMatches( args[1], completions, matches );
        }
        else if ( args.length == 3 )
        {
            completions.clear();
            matches.clear();

            // Gets a list of all possible skill names
            completions.addAll( plugin.getSkillManager().getAllSkills().keySet() );
            return StringUtil.copyPartialMatches( args[2], completions, matches );
        }

        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "Displays a description of a skill and training costs\n/dwarfcraft skill <player name> [skill_id]\n" +
                "Example: /dwarfcraft skill pickaxe_use - Prints details about the Pickaxe Use skill\nExample: "
                + "/dwarfcraft skill smartaleq pickaxe_use - Prints details about smartaleq's Pickaxe Use skill";
    }
}
