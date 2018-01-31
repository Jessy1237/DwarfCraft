package com.Jessy1237.DwarfCraft.listeners;

import com.Jessy1237.DwarfCraft.DwarfSkill;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.Jessy1237.DwarfCraft.DwarfPlayer;
import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.events.DwarfCraftLevelUpEvent;

public class DwarfListener implements Listener
{

    private final DwarfCraft plugin;

    public DwarfListener( final DwarfCraft plugin )
    {
        this.plugin = plugin;
    }

    @EventHandler( priority = EventPriority.NORMAL, ignoreCancelled = true )
    public void onDwarfCraftLevelUp( DwarfCraftLevelUpEvent event )
    {
        DwarfPlayer player = event.getDCPlayer();
        DwarfSkill skill = event.getSkill();

        if ( skill.getLevel() % plugin.getConfigManager().getAnnouncementInterval() == 0 && plugin.getConfigManager().announce )
        {
            String name = plugin.getChat().getPlayerPrefix( player.getPlayer() ) + player.getPlayer().getName() + plugin.getChat().getPlayerSuffix( player.getPlayer() );
            plugin.getOut().sendBroadcast( plugin.getConfigManager().getAnnouncementMessage().replace( "%playername%", name ).replace( "%skillname%", skill.getDisplayName() ).replace( "%level%", "" + skill.getLevel() ) );
        }
    }
}
