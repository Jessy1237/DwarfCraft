/*
 * Copyright (c) 2018.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft.listeners;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.Messages;
import com.jessy1237.dwarfcraft.Placeholder;
import com.jessy1237.dwarfcraft.events.DwarfEffectEvent;
import com.jessy1237.dwarfcraft.events.DwarfLevelUpEvent;
import com.jessy1237.dwarfcraft.models.DwarfPlayer;
import com.jessy1237.dwarfcraft.models.DwarfSkill;

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

            String message = Messages.announcementMessage.replaceAll( Placeholder.PLAYER_NAME.value(), name ).replaceAll( Placeholder.SKILL_NAME.value(), skill.getDisplayName() ).replaceAll( Placeholder.SKILL_LEVEL.value(), String.valueOf(skill.getLevel()) ).replaceAll( Placeholder.LEVEL.value(), String.valueOf(skill.getLevel()) );
            player.getPlayer().playSound( player.getPlayer().getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 0.5f, 1.0f );

            plugin.getServer().broadcastMessage( message );
            player.getPlayer().getWorld().spawnParticle( Particle.ENCHANTMENT_TABLE, player.getPlayer().getLocation(), 100 );
        }
    }

    @EventHandler
    public void onDwarfEffectEvent( DwarfEffectEvent event )
    {
    }
}
