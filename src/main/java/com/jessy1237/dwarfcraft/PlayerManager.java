/*
 * Copyright (c) 2023.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jessy1237.dwarfcraft.data.PlayerReader;
import com.jessy1237.dwarfcraft.models.DwarfPlayer;
import com.jessy1237.dwarfcraft.models.DwarfSkill;

public class PlayerManager 
{
    private final DwarfCraft plugin;
    private HashMap<String, DwarfPlayer> dwarves = new HashMap<>();

    public PlayerManager( DwarfCraft plugin ) {
        this.plugin = plugin;
    }

    public void init() {
        createDirectory();
        new PlayerReader( plugin, this );
    }

    void createDirectory() {
        File root = new File( plugin.getDataFolder().getAbsolutePath() + "/data/players/" );

        if ( !root.exists() )
        {
            if ( !root.mkdirs() )
            {
                return;
            }
        }
    }

    public void createDwarf( Player player )
    {
        File playerData = new File( plugin.getDataFolder().getAbsolutePath() + "/data/players/" + player.getUniqueId() + ".json" );
        try {
            Boolean created = playerData.createNewFile();
            if (created) {
                plugin.getUtil().consoleLog( "Creating data file for player: " + ChatColor.AQUA + player.getUniqueId() );
            }

            try ( FileOutputStream fos = new FileOutputStream( playerData ); OutputStreamWriter isr = new OutputStreamWriter( fos, StandardCharsets.UTF_8 ) )
            {
                DwarfPlayer newDwarf = new DwarfPlayer( plugin, player );
                LinkedHashMap<String, Object> playerMap = new LinkedHashMap<>();
                ArrayList<DwarfSkill> skills = new ArrayList<>();

                playerMap.put( "race", plugin.getRaceManager().getDefaultRace().getId() );
                playerMap.put( "isRaceMaster", false );
                playerMap.put("skills", skills);

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson( playerMap, isr );
            }
        } catch (IOException e) {

        }
    }
   
}
