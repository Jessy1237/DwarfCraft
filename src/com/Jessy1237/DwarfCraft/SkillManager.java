package com.Jessy1237.DwarfCraft;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.ChatColor;

import com.Jessy1237.DwarfCraft.data.SkillReader;
import com.Jessy1237.DwarfCraft.events.DwarfLoadSkillsEvent;
import com.Jessy1237.DwarfCraft.models.DwarfSkill;

public
class SkillManager
{
    private final DwarfCraft plugin;
    private HashMap<String, DwarfSkill> skills = new HashMap<>();

    public SkillManager() {
        this.plugin = DwarfCraft.getInstance();
    }

    @SuppressWarnings( "unchecked" )
    public void init() {
        createSkillFiles();
        new SkillReader( plugin, this );
        DwarfLoadSkillsEvent e = new DwarfLoadSkillsEvent( ( HashMap<String, DwarfSkill> ) skills.clone() );
        plugin.getServer().getPluginManager().callEvent( e );
        skills = getAllSkills();
        plugin.getUtil().consoleLog( Level.INFO, "Loaded " + ChatColor.AQUA + skills.values().size() + ChatColor.WHITE + " Skill(s)");
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
            String path = "data/dwarfcraft/skills/" + file_name;
            File destFile = new File( plugin.getDataFolder() + File.separator + path );
            InputStream source = plugin.getResource( path );
            if ( source != null && file_name.endsWith( ".json" ) && !destFile.exists() )
            {
                plugin.saveResource( path, true );
                plugin.getUtil().consoleLog( Level.INFO, "Writing data file: " + ChatColor.AQUA + path );
            }
        }

        File customDir = new File( plugin.getDataFolder().getAbsolutePath() + "/data/custom/skills/" );
        if ( !customDir.exists() ) customDir.mkdirs();
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
