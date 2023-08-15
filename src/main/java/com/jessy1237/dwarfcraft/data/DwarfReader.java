package com.jessy1237.dwarfcraft.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.DwarfManager;
import com.jessy1237.dwarfcraft.models.DwarfPlayer;
import com.jessy1237.dwarfcraft.models.DwarfRace;
import com.jessy1237.dwarfcraft.models.DwarfSkill;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public
class DwarfReader {
    private final DwarfCraft plugin;
    private final DwarfManager manager;
    private File dataDir;

    public
    DwarfReader( DwarfCraft plugin, DwarfManager manager ) {
        this.plugin = plugin;
        this.manager = manager;
        dataDir = new File( plugin.getDataFolder().getAbsolutePath() + "/data/players/" );

        //Convert existing dwarves
        if (!isConverted()) convertDwarves();

        //Load all Dwarf data files
        parseDwarves();
    }

    private boolean isConverted() {
        return dataDir.exists();
    }

    private void createDirectory() {
        if ( !isConverted() ) dataDir.mkdirs();
    }

    private void parseDwarves() {
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
                    UUID uuid = UUID.fromString( file_name.split( "\\." )[0].toLowerCase() );
                    Player player = plugin.getServer().getPlayer(uuid);

                    // Race
                    //TODO: Load race data
                    //String raceString = json.get( "race" ).getAsString();
                    DwarfRace race = plugin.getRaceManager().getRace("");

                    // Race Master
                    Boolean isRaceMaster = json.get( "raceMaster" ).getAsBoolean();

                    DwarfPlayer dwarfPlayer = new DwarfPlayer( plugin, player, race, isRaceMaster );
                    manager.addDwarf(uuid, dwarfPlayer);
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void createDwarf( OfflinePlayer player )
    {
        String path = "/data/players/" + player.getUniqueId() + ".json";
        File playerData = new File( plugin.getDataFolder().getAbsolutePath() + path );
        //if (playerData.exists()) return; //TODO: Add back this check when loading from data files
        
        try {
            try ( FileOutputStream fos = new FileOutputStream( playerData ); OutputStreamWriter isr = new OutputStreamWriter( fos, StandardCharsets.UTF_8 ) )
            {
                DwarfPlayer newDwarf = null;
                DwarfPlayer dwarf = plugin.getDataManager().findOffline(player.getUniqueId());
                if (dwarf == null) {
                    if (player.isOnline()) {
                        newDwarf = new DwarfPlayer( plugin, player.getPlayer() );
                        HashMap<String, DwarfSkill> skills = new HashMap<>();

                        newDwarf.setRace( plugin.getRaceManager().getDefaultRace().getId() );
                        newDwarf.setRaceMaster( false );
                        newDwarf.setSkills( skills );

                        plugin.getUtil().consoleLog( "Creating data file: " + ChatColor.AQUA + path );
                    }
                } else {
                    newDwarf = dwarf;
                    plugin.getUtil().consoleLog( "Found player data, creating data file: " + ChatColor.AQUA + path );
                }

                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
                gson.toJson( newDwarf, isr );
            }
        } catch (IOException e) {

        }
    }

    public void convertDwarves()
    {
        File database = new File( plugin.getDataFolder(), "dwarfcraft.db" );
        if ( database.exists() ) {
            plugin.getUtil().consoleLog( ChatColor.YELLOW + "Found legacy database. Converting player data..." );
            plugin.getUtil().consoleLog( ChatColor.RED + "Please do not shutdown server, converting database." );
            createDirectory();
            OfflinePlayer[] players = plugin.getServer().getOfflinePlayers();
            for (OfflinePlayer player : players) {
                createDwarf( player );
            }
            plugin.getUtil().consoleLog( ChatColor.GREEN + "Conversion complete!" );
        }
    }
}
