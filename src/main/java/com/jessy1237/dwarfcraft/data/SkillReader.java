package com.jessy1237.dwarfcraft.data;

import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

import org.bukkit.Material;

import com.jessy1237.dwarfcraft.DwarfCraft;
import com.jessy1237.dwarfcraft.SkillManager;
import com.jessy1237.dwarfcraft.models.DwarfItemHolder;
import com.jessy1237.dwarfcraft.models.DwarfRace;
import com.jessy1237.dwarfcraft.models.DwarfSkill;
import com.jessy1237.dwarfcraft.models.DwarfTrainingItem;
import com.jessy1237.dwarfcraft.models.effects.DwarfEffect;
import com.jessy1237.dwarfcraft.models.effects.DwarfEffectType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public
class SkillReader
{
    private final DwarfCraft plugin;
    private final SkillManager manager;

    public
    SkillReader( DwarfCraft plugin, SkillManager manager ) {
        this.plugin = plugin;
        this.manager = manager;
        parseSkills();
    }

    private
    void parseSkills()
    {
        String content;
        File directory = new File( plugin.getDataFolder().getAbsolutePath() + "/data/dwarfcraft/skills/" );
        File customSkills = new File( plugin.getDataFolder().getAbsolutePath() + "/data/custom/skills/" );
        ArrayList<String> overrideNames = new ArrayList<>( Arrays.asList( Objects.requireNonNull( customSkills.list() ) ) );
        overrideNames.removeIf( override->!override.endsWith( ".json" ) );

        if ( directory.list() != null && Objects.requireNonNull( directory.list() ).length > 0 )
        {
            for (String file_name : Objects.requireNonNull( directory.list() ) ) {
                try
                {
                    if ( overrideNames.contains( file_name ) ) {
                        content = new String( Files.readAllBytes( Paths.get( customSkills + "/" + file_name ) ) );
                    } else {
                        content = new String( Files.readAllBytes( Paths.get( directory + "/" + file_name ) ) );
                        if ( !file_name.endsWith( ".json" ) ) continue;
                    }

                    JsonReader reader = new JsonReader( new StringReader( content.trim() ) );
                    reader.setLenient( true );
                    JsonElement element = new JsonParser().parse( reader );
                    JsonObject json = element.getAsJsonObject();

                    // Skill ID
                    String skill_id = file_name.split( "\\." )[0].toLowerCase();

                    // Display Name
                    String display_name = json.get( "display_name" ).getAsString();

                    // Race Specializations
                    JsonArray race_ids = json.get( "race_specialization" ).getAsJsonArray();
                    LinkedHashMap<String, DwarfRace> races = new LinkedHashMap<>();
                    for (int i = 0; i < race_ids.size(); i++ ) {
                        String race_id = race_ids.get( i ).getAsString();
                        DwarfRace dcRace = plugin.getRaceManager().getRace( race_id );

                        if ( dcRace != null )
                            races.put( race_id, dcRace );
                        else
                            plugin.getUtil().consoleLog( "Invalid race id " + race_id + " given for race specialization for skill " + skill_id, Level.WARNING );
                    }

                    if (race_ids.size() <= 0) {
                        // Warn for no race specializations
                        plugin.getUtil().consoleLog( "No race specializations were provided for skill " + skill_id, Level.WARNING );
                    }

                    // Effects
                    ArrayList<DwarfEffect> effects = parseEffects( json, skill_id );

                    // Training Items
                    JsonArray training_items = json.get( "training_items" ).getAsJsonArray();
                    DwarfTrainingItem item1 = parseTrainingItem( training_items, 0 );
                    DwarfTrainingItem item2 = parseTrainingItem( training_items, 1 );
                    DwarfTrainingItem item3 = parseTrainingItem( training_items, 2 );

                    // Held Item
                    Material held = Material.matchMaterial( json.get( "held_item" ).getAsString() );

                    DwarfSkill skill = new DwarfSkill(plugin, skill_id, display_name, races, 0, effects, item1, item2, item3, held);
                    manager.addSkill(skill);
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings( "unused" )
    private
    ArrayList<DwarfEffect> parseEffects( JsonObject object, String skill_id )
    {
        ArrayList<DwarfEffect> effectList = new ArrayList<>();
        JsonArray effects = object.getAsJsonArray("effects");

        if ( effects.size() <= 0 ) {
            //Warn for no effects provided
            plugin.getUtil().consoleLog( "No effects provided for skill: " + skill_id, Level.WARNING );
        } else {
            for (JsonElement effect : effects) {
                DwarfEffect dcEffect = null;
                
                DwarfEffectType type = DwarfEffectType.getEffectType( effect.getAsJsonObject().get("type").getAsString() ); //todo remove when we have a effect registry
                if (type == null) continue;
                
                if ( !DwarfEffectType.has( type.toString() ) ) {
                    plugin.getUtil().consoleLog( "Unsupported effect type " + type + " for skill: " + skill_id, Level.WARNING );
                    continue;
                }
    
                dcEffect = new DwarfEffect( effect, skill_id, plugin );
                effectList.add( dcEffect );
                plugin.getEffectRegistry().registerEffect( type, dcEffect );
            }
        }
        return effectList;
    }

    private DwarfTrainingItem parseTrainingItem( JsonArray element, int index ) {
        DwarfItemHolder item_holder;
        double base;
        int max;

        if ( index >= 0 && index < element.size() && element.get( index ) != null )
        {
            JsonElement item = element.get( index );
            JsonObject json = item.getAsJsonObject();
            item_holder = plugin.getUtil().getDwarfItemHolder( json, "id" );
            base = json.get( "base" ).getAsDouble();
            max = json.get( "max" ).getAsInt();
        } else {
            // If element couldn't be found return a empty DwarfTrainingItem
            item_holder = new DwarfItemHolder( new HashSet<>( Collections.singleton( Material.AIR ) ), null, "" );
            base = 0;
            max = 0;
            return new DwarfTrainingItem( item_holder, base, max );
        }

       return new DwarfTrainingItem( item_holder, base, max );
    }
}
