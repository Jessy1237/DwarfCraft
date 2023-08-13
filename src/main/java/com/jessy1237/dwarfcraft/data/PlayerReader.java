package com.jessy1237.dwarfcraft.data;

import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.PlayerManager;
import com.jessy1237.dwarfcraft.RaceManager;
import com.jessy1237.dwarfcraft.models.DwarfPlayer;
import com.jessy1237.dwarfcraft.models.DwarfRace;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public
class PlayerReader {
    private final DwarfCraft plugin;

    public
    PlayerReader( DwarfCraft plugin, PlayerManager manager ) {
        this.plugin = plugin;

        parsePlayers();
    }

    private
    void parsePlayers() {
        String content;
        File directory = new File( plugin.getDataFolder().getAbsolutePath() + "/data/players/" );

        if ( directory.list() != null && Objects.requireNonNull( directory.list() ).length > 0 )
        {
            for (String file_name : Objects.requireNonNull( directory.list() ) ) {
                try
                {
                    content = new String( Files.readAllBytes( Paths.get( directory + "/" + file_name ) ) );
                    if ( !file_name.endsWith( ".json" ) ) continue;

                    JsonReader reader = new JsonReader( new StringReader( content.trim() ) );
                    reader.setLenient( true );
                    JsonElement element = new JsonParser().parse( reader );
                    if (!element.isJsonObject()) continue;
                    JsonObject json = element.getAsJsonObject();

                    // UUID
                    String uuid = file_name.split( "\\." )[0].toLowerCase();
                    Player player = plugin.getServer().getPlayer(uuid);

                    // Race
                    String raceString = json.get( "race" ).getAsString();
                    DwarfRace race = plugin.getRaceManager().getRace(raceString);

                    // Race Master
                    Boolean isRaceMaster = json.get( "isRaceMaster" ).getAsBoolean();

                    DwarfPlayer dwarfPlayer = new DwarfPlayer( plugin, player, race, isRaceMaster );
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
