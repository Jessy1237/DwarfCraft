/*
 * Copyright (c) 2023.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft.listeners;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.data.ConfigManager;
import com.jessy1237.dwarfcraft.events.DwarfEffectEvent;
import com.jessy1237.dwarfcraft.events.DwarfLevelUpEvent;
import com.jessy1237.dwarfcraft.models.DwarfPlayer;
import com.jessy1237.dwarfcraft.models.DwarfSkill;
import com.jessy1237.dwarfcraft.util.Placeholder;

public class DwarfListener implements Listener
{
    private final DwarfCraft plugin;

    public DwarfListener( DwarfCraft plugin )
    {
        this.plugin = plugin;
    }

    @EventHandler( priority = EventPriority.NORMAL, ignoreCancelled = true )
    public void onDwarfLevelUp( DwarfLevelUpEvent event )
    {
        DwarfPlayer player = event.getDwarfPlayer();
        DwarfSkill skill = event.getSkill();

        if ( skill.getLevel() % plugin.getConfigManager().getAnnouncementInterval() == 0 && plugin.getConfigManager().announce )
        {
            String prefix = plugin.getChat().getPlayerPrefix( player.getPlayer() );
            String suffix = plugin.getChat().getPlayerSuffix( player.getPlayer() );
            String name = player.getPlayer().getName();

            if ( prefix != null )
                name = prefix.concat( name );

            if (suffix != null)
                name = name.concat( suffix );

            String message = ConfigManager.getMessage("Announcement Message").replaceAll( Placeholder.PLAYER_NAME.value(), name ).replaceAll( Placeholder.SKILL_NAME.value(), skill.getDisplayName() ).replaceAll( Placeholder.SKILL_LEVEL.value(), String.valueOf(skill.getLevel()) ).replaceAll( Placeholder.LEVEL.value(), String.valueOf(skill.getLevel()) );
            player.getPlayer().playSound( player.getPlayer().getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 0.5f, 1.0f );

            plugin.getServer().broadcastMessage( message );
            player.getPlayer().getWorld().spawnParticle( Particle.ENCHANTMENT_TABLE, player.getPlayer().getLocation(), 100 );
        }

        // Run level up commands from config
        if ( plugin.getConfigManager().getSkillLevelCommands().size() > 0 )
        {
            ArrayList<String> commands;

            if ( !plugin.getSkillManager().getAllSkills().containsValue( skill ) )
            {
                return;
            }

            if ( player.isMax() )
            {
                commands = plugin.getConfigManager().getSkillMaxCapeCommands();
            }
            else if ( skill.getLevel() >= skill.getMaxLevel( player ) )
            {
                commands = plugin.getConfigManager().getSkillMasteryCommands();
            }
            else
            {
                commands = plugin.getConfigManager().getSkillLevelCommands();
            }

            for ( String command : commands )
            {
                String playerPosition = player.getPlayer().getLocation().getX() + " " + player.getPlayer().getLocation().getY() + " " + player.getPlayer().getLocation().getZ();

                command = command.replaceAll( "<player.pos>", playerPosition ).replaceAll( "<world.name>", player.getPlayer().getWorld().getName() );
                command = skill.description( command, player );
                command = ChatColor.translateAlternateColorCodes( '&', command );

                plugin.getServer().dispatchCommand( plugin.getServer().getConsoleSender(), command );
            }

            commands.clear();
        }
    }

    @EventHandler
    public void onDwarfEffectEvent( DwarfEffectEvent event )
    {
    }
}
