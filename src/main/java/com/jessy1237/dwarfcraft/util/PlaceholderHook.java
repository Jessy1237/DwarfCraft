package com.jessy1237.dwarfcraft.util;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import static com.jessy1237.dwarfcraft.util.Placeholder.generalParse;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.models.DwarfPlayer;

public class PlaceholderHook extends PlaceholderExpansion
{
    @Override
    public boolean canRegister()
    {
        return true;
    }
    
    @Override
    public @NotNull String getIdentifier()
    {
        return "DwarfCraft";
    }
    
    @Override
    public @NotNull String getAuthor()
    {
        DwarfCraft plugin = (DwarfCraft) Bukkit.getPluginManager().getPlugin( "DwarfCraft" );
        if (plugin == null) return "";
    
        return plugin.getDescription().getAuthors().toString();
    }
    
    @Override
    public @NotNull String getVersion()
    {
        DwarfCraft plugin = (DwarfCraft) Bukkit.getPluginManager().getPlugin( "DwarfCraft" );
        if (plugin == null) return "";
        
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier )
    {
        DwarfCraft plugin = (DwarfCraft) Bukkit.getPluginManager().getPlugin( "DwarfCraft" );
        if (plugin == null) return null;
        
        String out = generalParse( "<" + identifier + ">", plugin );
        DwarfPlayer dwarfPlayer = plugin.getDwarfManager().getDwarf( player );
        
        if ( dwarfPlayer != null )
            out = dwarfPlayer.toString(out);
        
        // If we didn't change the text then it wasn't out identifier so return null as per API wiki
        if ( out.equals( identifier ) )
            out = null;
        
        return out;
    }
}
