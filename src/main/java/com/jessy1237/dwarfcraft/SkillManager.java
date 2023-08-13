package com.jessy1237.dwarfcraft;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import org.bukkit.ChatColor;

import com.jessy1237.dwarfcraft.data.SkillReader;
import com.jessy1237.dwarfcraft.events.DwarfLoadSkillsEvent;
import com.jessy1237.dwarfcraft.models.DwarfSkill;

public
class SkillManager
{
    private final DwarfCraft plugin;
    private HashMap<String, DwarfSkill> skills = new HashMap<>();

    public SkillManager( DwarfCraft plugin ) {
        this.plugin = plugin;
    }

    @SuppressWarnings( "unchecked" )
    public void init() {
        createSkillFiles();
        new SkillReader( plugin, this );
        DwarfLoadSkillsEvent e = new DwarfLoadSkillsEvent( ( HashMap<String, DwarfSkill> ) skills.clone() );
        plugin.getServer().getPluginManager().callEvent( e );
        skills = getAllSkills();
        plugin.getUtil().consoleLog( "Loaded " + ChatColor.AQUA + skills.values().size() + ChatColor.WHITE + " Skill(s)" );
    }

    private
    void createSkillFiles() {
        File root = new File( plugin.getDataFolder().getAbsolutePath() );

        if ( !root.exists() )
        {
            if ( !root.mkdirs() )
            {
                return;
            }
        }

        for ( String file_name : Registration.getSkillFiles() )
        {
            String path = "data/skills/" + file_name;
            File destFile = new File( plugin.getDataFolder() + File.separator + path );
            InputStream source = plugin.getResource( path );
            if ( source != null && file_name.endsWith( ".json" ) && !destFile.exists() )
            {
                plugin.saveResource( path, true );
                plugin.getUtil().consoleLog( "Writing data file: " + ChatColor.AQUA + path );
            }
        }
    }

    public void addSkill(DwarfSkill skill) {
        skills.put( skill.getId(), skill );
    }

    public
    DwarfSkill getSkill(String skill_id)
    {
        return skills.get( skill_id );
    }

    public
    HashMap<String, DwarfSkill> getAllSkills()
    {
        HashMap<String, DwarfSkill> newSkillsArray = new HashMap<>();
        for ( DwarfSkill s : skills.values() )
        {
            if ( newSkillsArray.containsKey( s.getId() ) ) continue;
            newSkillsArray.put( s.getId(), s.clone() );
        }
        return newSkillsArray;
    }
}
