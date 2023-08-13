package com.jessy1237.dwarfcraft;

import java.util.ArrayList;
import java.util.List;

public
class Registration
{
    private static final List<String> skill_files = new ArrayList<>();
    private static final List<String> race_files = new ArrayList<>();

    public static void init() {
        registerSkill( "pickaxe_use.json" );
        registerSkill( "shovel_use.json" );
        registerSkill( "axe_use.json" );
        registerSkill( "seed_gatherer.json" );
        registerSkill( "melon_farmer.json" );
        registerSkill( "fishing.json" );
        registerSkill( "stone_shaper.json" );
        registerSkill( "smelter.json" );
        registerSkill( "iron_forger.json" );
        registerSkill( "diamond_forger.json" );
        registerSkill( "excavator.json" );
        registerSkill( "quarry_worker.json" );
        registerSkill( "ore_miner.json" );
        registerSkill( "exotic_miner.json" );
        registerSkill( "wood_carver.json" );
        registerSkill( "gold_forger.json" );
        registerSkill( "nether_miner.json" );
        registerSkill( "sand_digger.json" );
        registerSkill( "gravel_digger.json" );
        registerSkill( "dirt_digger.json" );
        registerSkill( "lumberjack.json" );
        registerSkill( "carpenter.json" );
        registerSkill( "wheat_farmer.json" );
        registerSkill( "exotic_farmer.json" );
        registerSkill( "vegetable_farmer.json" );
        registerSkill( "mason.json" );
        registerSkill( "glass_worker.json" );
        registerSkill( "wood_crafter.json" );
        registerSkill( "bookmaker.json" );
        registerSkill( "brickmaker.json" );
        registerSkill( "demolitionist.json" );
        registerSkill( "fire_starter.json" );
        registerSkill( "railworker.json" );
        registerSkill( "baker.json" );
        registerSkill( "fletcher.json" );
        registerSkill( "butcher.json" );
        registerSkill( "sailor.json" );
        registerSkill( "climber.json" );
        registerSkill( "survivalist.json" );
        registerSkill( "florist.json" );
        registerSkill( "dungeon_delver.json" );
        registerSkill( "nether_hunter.json" );
        registerSkill( "shearer.json" );
        registerSkill( "huntsman.json" );
        registerSkill( "monster_hunter.json" );
        registerSkill( "scout.json" );
        registerSkill( "soldier.json" );
        registerSkill( "archer.json" );
        registerSkill( "exotic_armour.json" );
        registerSkill( "swordsman.json" );
        registerSkill( "sign_maker.json" );
        registerSkill( "torch_maker.json" );
        registerSkill( "alchemist.json" );
        registerSkill( "noble.json" );
        registerSkill( "axe_swinger.json" );

        registerRace( "dwarf.json" );
        registerRace( "elf.json" );
        registerRace( "gnome.json" );
        registerRace( "human.json" );
    }

    public static
    void registerSkill( String file_name ) {
        skill_files.add( file_name );
    }

    public static
    void registerRace( String file_name ) {
        race_files.add( file_name );
    }

    public static
    List<String> getSkillFiles() {
        return skill_files;
    }

    public static
    List<String> getRaceFiles() {
        return race_files;
    }
}
