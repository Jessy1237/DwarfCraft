package com.Jessy1237.DwarfCraft.listeners;

import com.Jessy1237.DwarfCraft.models.DwarfTrainer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.PlaceHolderParser.PlaceHolder;
import com.Jessy1237.DwarfCraft.events.DwarfEffectEvent;
import com.Jessy1237.DwarfCraft.events.DwarfLevelUpEvent;
import com.Jessy1237.DwarfCraft.models.DwarfPlayer;
import com.Jessy1237.DwarfCraft.models.DwarfSkill;

public class DwarfListener implements Listener
{

    private final DwarfCraft plugin;

    public DwarfListener( final DwarfCraft plugin )
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
            String name = plugin.getChat().getPlayerPrefix( player.getPlayer() ) + player.getPlayer().getName() + plugin.getChat().getPlayerSuffix( player.getPlayer() );
            String message = plugin.getConfigManager().getAnnouncementMessage().replace( PlaceHolder.PLAYER_NAME.getPlaceHolder(), name ).replace( PlaceHolder.SKILL_NAME.getPlaceHolder(), skill.getDisplayName() ).replace( PlaceHolder.SKILL_LEVEL.getPlaceHolder(), "" + skill.getLevel() ).replace( PlaceHolder.LEVEL.getPlaceHolder(), "" + skill.getLevel() );

            plugin.getOut().sendBroadcast( message );
            player.getPlayer().getWorld().spawnParticle( Particle.ENCHANTMENT_TABLE, player.getPlayer().getLocation(), 100 );
        }
    }

    public void onDwarfEffectEvent( DwarfEffectEvent event )
    {
        DwarfPlayer player = event.getDwarfPlayer();
        if ( player == null )
            return;

        if ( player.getRace().equalsIgnoreCase( "Vanilla" ) )
        {
            event.setCancelled( true );
        }
    }
}
