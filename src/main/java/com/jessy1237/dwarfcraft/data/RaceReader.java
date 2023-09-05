package com.jessy1237.dwarfcraft.data;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.models.DwarfRace;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public
class RaceReader {

    private final DwarfCraft plugin;
    private final RaceManager manager;
    private static final List<String> race_files = new ArrayList<>();
    private File dataDir;
    private boolean vanillaEnabled = true;

    public
    RaceReader( DwarfCraft plugin, RaceManager manager ) {
        this.plugin = plugin;
        this.manager = manager;
        dataDir = new File( plugin.getDataFolder().getAbsolutePath() + "/data/races/" );
        this.vanillaEnabled = plugin.getConfigManager().vanilla;

        registerRaces();
        createRaceFiles();
        parseRaces();
    }

    private void registerRaces() {
        registerRace( "dwarf" );
        registerRace( "elf" );
        registerRace( "gnome" );
        registerRace( "human" );
    }

    public static
    void registerRace( String race_id ) {
        race_files.add( race_id.toLowerCase() + ".json" );
    }

    private void createRaceFiles() {
        if ( !dataDir.exists() ) dataDir.mkdirs();
        for ( String file_name : race_files )
        {
            String path = "data/races/" + "/" + file_name;
            InputStream source = plugin.getResource( path );

            File destFile = new File( dataDir.toString() + file_name );
            if ( source != null && file_name.endsWith( ".json" ) && !destFile.exists() )
            {
                plugin.saveResource( path, false );
                plugin.getUtil().consoleLog( "Writing data file: " + ChatColor.AQUA + path );
            }
        }
    }

    private void parseRaces() {
        String content;
        File directory = new File( plugin.getDataFolder().getAbsolutePath() + "/data/races/" );

        if ( vanillaEnabled ) {
            System.out.println("Adding vanilla race");
            manager.addRace( new DwarfRace( "vanilla", "Vanilla", "The all around balanced race (vanilla).", Material.GRASS ) );
        }

        int maxAllowed = vanillaEnabled ? 44 : 45;
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
                    JsonObject json = element.getAsJsonObject();

                    // Race ID
                    String race_id = file_name.split( "\\." )[0].toLowerCase();

                    // Display Name
                    String display_name = json.get( "display_name" ).getAsString();

                    // Description
                    String description = json.get( "description" ).getAsString();

                    // Prefix Colour
                    String prefix_colour = json.get( "prefix_colour" ).getAsString();

                    // Icon
                    Material icon = Material.matchMaterial( json.get( "material_icon" ).getAsString() );

                    DwarfRace race = new DwarfRace(race_id, display_name, description, icon);
                    if ( manager.count() < maxAllowed ) {
                        race.setPrefixColour(prefix_colour);
                        manager.addRace(race);
                    } else {
                        plugin.getUtil().consoleLog( "Did not load race: " + race.getName() + " as already at cap of " + maxAllowed + " races", Level.WARNING );
                    }
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
