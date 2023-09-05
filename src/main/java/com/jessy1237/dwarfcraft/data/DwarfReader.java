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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import com.jessy1237.dwarfcraft.DwarfCraft;
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

    public DwarfReader( DwarfCraft plugin, DwarfManager manager ) {
        this.plugin = plugin;
        this.manager = manager;
        dataDir = new File( plugin.getDataFolder().getAbsolutePath() + "/data/players/" );

        //Convert existing dwarves
        if (!isConverted()) convertDwarves();
    }

    private boolean isConverted() {
        return dataDir.exists();
    }

    private void createDirectory() {
        if ( !isConverted() ) dataDir.mkdirs();
    }

    protected HashMap<UUID, DwarfPlayer> parseDwarves() {
        HashMap<UUID, DwarfPlayer> dwarves = new HashMap<UUID, DwarfPlayer>();
        if ( dataDir.list() != null && Objects.requireNonNull( dataDir.list() ).length > 0 )
        {
            for (String file_name : Objects.requireNonNull( dataDir.list() ) ) {
                if (!file_name.endsWith(".json")) continue;
                String uuidString = file_name.split("\\.")[0];
                UUID uuid = UUID.fromString(uuidString);
                
                DwarfPlayer dwarfPlayer = parseDwarf(new DwarfPlayer(plugin, uuid));
                dwarves.put(uuid, dwarfPlayer);
            }
        }
        return dwarves;
    }

    protected DwarfPlayer parseDwarf( DwarfPlayer player ) {
        try
        {
            String uuidString = player.getUuid().toString();
            String file_name = uuidString + ".json";
            String content = new String( Files.readAllBytes( Paths.get( dataDir + "/" + file_name ) ) );
            if ( !file_name.endsWith( ".json" ) ) return null;

            JsonReader reader = new JsonReader( new StringReader( content.trim() ) );
            reader.setLenient( true );
            JsonElement element = new JsonParser().parse( reader );
            if (!element.isJsonObject()) return null;
            JsonObject json = element.getAsJsonObject();
            DwarfPlayer dwarfPlayer;

            // UUID
            UUID uuid = UUID.fromString( file_name.split( "\\." )[0].toLowerCase() );

            // Race
            String raceString = json.get( "race" ).getAsString();
            DwarfRace race = plugin.getRaceManager().getRace(raceString);

            // Race Master
            boolean isRaceMaster = json.get( "raceMaster" ).getAsBoolean();

            // Skills
            JsonObject skillsObject = json.get("skills").getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entries = skillsObject.entrySet();
            dwarfPlayer = new DwarfPlayer( plugin, uuid, race, isRaceMaster );

            for (Map.Entry<String, JsonElement> entry: entries) {
                String skill_id = entry.getKey();
                DwarfSkill skill = plugin.getSkillManager().getSkill(skill_id);

                JsonObject skillObject = json.get("skills").getAsJsonObject().get(skill_id).getAsJsonObject();
                skill.setLevel(skillObject.get("level").getAsInt());
                skill.setDeposit(skillObject.get("deposit1").getAsInt(), 1);
                skill.setDeposit(skillObject.get("deposit2").getAsInt(), 2);
                skill.setDeposit(skillObject.get("deposit3").getAsInt(), 3);
                dwarfPlayer.setSkill(skill);
            }
            
            if (!manager.getDwarves().containsKey(uuid)) {
                if (plugin.getServer().getOfflinePlayer(uuid).isOnline()) {
                    dwarfPlayer.setPlayer(plugin.getServer().getPlayer(uuid));
                }
            }

            return dwarfPlayer;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return null;
        }
    }

    protected void createDwarf( DwarfPlayer player )
    {
        if (player.getPlayer() == null) return;
        createDwarf( player.getPlayer() );
    }

    protected DwarfPlayer createDwarf( OfflinePlayer player )
    {
        String path = "/data/players/" + player.getUniqueId() + ".json";
        File playerData = new File( plugin.getDataFolder().getAbsolutePath() + path );
        if (playerData.exists()) return null;
        
        try ( FileOutputStream fos = new FileOutputStream( playerData ); OutputStreamWriter isr = new OutputStreamWriter( fos, StandardCharsets.UTF_8 ) )
        {
            DwarfPlayer newDwarf = null;
            DwarfPlayer dwarf = plugin.getDataManager().findOffline(player.getUniqueId());
            if (dwarf == null) {
                if (player.isOnline()) {
                    newDwarf = new DwarfPlayer( plugin, player.getPlayer() );
                } else {
                    newDwarf = new DwarfPlayer (plugin, player.getUniqueId());
                }
                plugin.getUtil().consoleLog( "Creating data file: " + ChatColor.AQUA + path );
            } else {
                newDwarf = dwarf;
                plugin.getUtil().consoleLog( "Found player data, creating data file: " + ChatColor.AQUA + path );
            }

            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
            gson.toJson( newDwarf, isr );
            return newDwarf;
        } catch (IOException e) {
            return null;
        } 
    }

    protected boolean saveDwarf( DwarfPlayer player ) {
        String path = "/data/players/" + player.getUuid() + ".json";
        File playerData = new File( plugin.getDataFolder().getAbsolutePath() + path );
        if (!playerData.exists()) return false;
        
        try ( FileOutputStream fos = new FileOutputStream( playerData ); OutputStreamWriter isr = new OutputStreamWriter( fos, StandardCharsets.UTF_8 ) )
        {
            plugin.getUtil().consoleLog( "Saving data file: " + ChatColor.AQUA + path );
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
            gson.toJson( player, isr );
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void convertDwarves()
    {
        File database = new File( plugin.getDataFolder(), "dwarfcraft.db" );
        if ( database.exists() ) {
            plugin.getUtil().consoleLog( ChatColor.YELLOW + "Found legacy database. Converting player data..." );
            plugin.getUtil().consoleLog( ChatColor.RED + "Please do not shutdown server, converting database." );
            createDirectory();
            OfflinePlayer[] players = plugin.getServer().getOfflinePlayers();
            for (OfflinePlayer player : players) {
                DwarfPlayer dwarf = plugin.getDataManager().findOffline(player.getUniqueId());
                createDwarf( dwarf );
            }
            plugin.getUtil().consoleLog( ChatColor.GREEN + "Conversion complete!" );
        }
    }
}
