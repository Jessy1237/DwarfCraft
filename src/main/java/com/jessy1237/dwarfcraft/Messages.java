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

import java.util.ArrayList;
@Deprecated
public final class Messages
{

    public Messages()
    {
        Messages.tutorial.clear();
        Messages.tutorial.add( "&5&lWelcome to DwarfCraft!\n\n&0You have a set of skills that let you do certain tasks better. When you first start things may be more difficult than you're used to, but as you level your skills up you will be much more productive." );
        Messages.tutorial.add( "&5The Races of DwarfCraft\n\n&0There are four basic races in DwarfCraft. It is up to you to choose a race that best fits your play style. You will not be able to level any skills in DwarfCraft until you pick a race.\n" );
        Messages.tutorial.add( "{\"text\":\"Read about the four races on the pages that follow, then type '/dc race' or click here to begin your journey.\",\"color\":\"dark_red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/dc race\"}}" );
        Messages.tutorial.add( "&3The Dwarf\n\n&0The Dwarf race specializes in mining. The skills in this race will allow you to mine more efficiently, gather additional ores, and excavate terrain. The Dwarf plays an important role in providing resources for tools and equipment needed by the other races. Their race relies on you providing resources. If you enjoy mining, this race would be great for you." );
        Messages.tutorial.add( "&9The Gnome\n\n&0The Gnome race specializes in crafting and building. They will provide structures for you, craft your tools and equipment, and are all around masters of the crafting table. If you enjoy casually and creatively building or are a  master in redstone contraptions, this race would be great for you." );
        Messages.tutorial.add( "&4The Elf\n\n&0The Elf race specializes in all things combat. They deal extra damage to animals and mobs and get extra drops. They are great at gaining experience levels and protecting your fellow members. They are also great archers and have great exploration skills whether it be on foot or boat. If you enjoy combat and exploring, the elven race is all about you." );
        Messages.tutorial.add( "&8The Human\n\n&0The Human race specializes in various support and utility skills such as smelting, cooking, fishing, and farming. They are great lumberjacks and are great for gathering wood and processing resources from the Dwarf for the Gnome and Elf. If you enjoy playing a casual support role, the Human race best fits you." );
        Messages.tutorial.add( "&5Skilling in DwarfCraft\n\n&0Now that you have learned about the various races to choose from and hopefully found one that fits you, it is time to learn how you can advance your character in that race. If you want to become the best of your class you will need to train by visiting trainers in the world. We will learn more about trainers in a bit. First lets talk about your skills and skillsheet." );
        Messages.tutorial.add( "&0At first your skills will be very poor which means you will perform tasks worse than normal gameplay. You will find your low level skills dropping less resources, crafting less items, taking longer, or requiring more tools. As you level you will begin to exceed the normal rates and excel in some tasks. Normal gameplay rates take effect around level %racelevellimit%. You also wont be able to excel in every skill to the highest level. " +
                "The skills in which your race specializes in can be levelled to level %maxskilllevel% while all other skills outside your race specialty will cap out at the normal rates." );
        Messages.tutorial.add( "[{\"text\":\"You can access your current skill levels at anytime with '/dc skillsheet'.\\n\",\"color\":\"dark_red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/dc skillsheet\"}},{\"text\":\"\\nYou can find all the skills which your character has yet to train by typing '/dc skillsheet full'.\",\"color\":\"dark_red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/dc skillsheet full\"}}]" );
        Messages.tutorial.add( "&0Now that you know the basics of how skilling works in DwarfCraft. Lets discuss the final topic in DwarfCraft, which is how you train those skills." );
        Messages.tutorial.add( "&5DwarfCraft Trainers\n\n&0Trainers (NPCs) are experts in the skill they teach. They will share their knowledge with you in return for specific resources related to that skill and can be found in various places throughout the world." );
        Messages.tutorial.add( "&0As they teach you, the requirements will become increasingly harder to meet. Also, the more skill levels you gain as a player the harder the difficulty. So the skills you choose early on will be easier than the ones you put aside. Each level up affects all other skills you have yet to master. You achieve mastery in a skill when you have reached the maximum level." );
        Messages.tutorial.add( "&0You may also find some trainers that simply have limits to what they can teach you. Some trainers will only be able to teach you up to a certain level in that skill and you may have to look elsewhere to train further. Exploring to find trainers in the world is a must to fully master every skill." );
        Messages.tutorial.add( "&5Working Together\n\n&0DwarfCraft was designed in a way in which players have to interact and work with each other to accomplish goals. Each race needs the other three races in order to be as effective. You will find that not working together or trying to be a Jack of All Trades will greatly hinder progression. Find other players that need the skills and services you provide. They will need you.\n\nLastly, remember to have fun. We hope you enjoy DwarfCraft." );
    }

    // String messages fixed for DwarfCraft, and backup messages when loading
    // fails.
    protected enum Fixed
    {
        WELCOME( "Welcome to a DwarfCraft world! Things are bit different here as this world focuses heavily on skilling. To learn more about what you can do here, type '&4/dc tutorial&2' to get started." ),
        GENERALINFO( "&dWelcome to DwarfCraft. You are a player with a set of skills that let you do certain tasks better. For more information see &4/dc tutorial&d. Original Authors: smartaleq, LexManos and RCarretta Authors: Jessy1237" );

        private String message;

        Fixed( String message )
        {
            this.message = message;
        }

        public String getMessage()
        {
            return message;
        }
    }

    // String messages loaded from messages.config
    public static String welcomePrefix = "&6[DwarfCraft] ";
    public static String welcome = "&fWelcome, &9<player.race> &6<player.name>";
    public static String announcementMessage = "<player.name> has just leveled <skill.name> to level <skill.level>!";
    public static String skillSheetHeader = "&6Skillsheet for &9<player.name> &6[&9<player.race> &6- &6Lvl &3<player.level>&6]";
    public static String skillSheetSkillLine = "&6[&3<skill.level>&6] &b<skill.name>";
    public static String skillSheetUntrainedSkillHeader = "&6Untrained Skills:\n";
    public static String skillSheetUntrainedSkillLine = "&7<skill.name>&6";
    public static String skillInfoHeader = "&6Skill Info for &b<player.name>&6 || &b<skill.name>&6 || Your level &3<skill.level>/<skill.max.level>";
    public static String skillInfoMinorHeader = "&f------&6[Effect]&f------";
    public static String skillInfoEffectIDPrefix = "&6[&5*&6] ";
    public static String skillInfoMaxSkillLevel = "&6---This skill is maximum level, no training available---";
    public static String skillInfoAtTrainerLevel = "&6---You're as skilled as me, you need a more advanced trainer!--";
    public static String skillInfoTrainCostHeader = "&6---Train costs for level &3<skill.level.next>";
    public static String skillInfoTrainCost = " &6--  &2<skill.deposit.amount> of <skill.cost.total> <skill.item.type>&6  --";
    public static String raceCheck = "You are a <player.race>";
    public static String adminRaceCheck = "<player.name> is a <player.race>";
    public static String alreadyRace = "You are already a <player.race>";
    public static String changedRace = "You are now a <race.name>";
    public static String confirmRace = "You need to confirm this command with confirm at the end. (Note: This will reset all their skills)";
    public static String raceDoesNotExist = "The race <race.name> doesn't exist";
    public static String chooseARace = "&cPlease choose a race! Use /dc race";
    public static String trainSkillPrefix = "&6[Train &b<skill.name>&6] ";
    public static String raceDoesNotContainSkill = "&cYour race doesn't have this skill!";
    public static String raceDoesNotSpecialize = "&cYour race doesn't specialize in this skill! Max level is (<race.level.limit>)!";
    public static String maxSkillLevel = "&cYour skill is max level (<skill.max.level>)!";
    public static String trainerMaxLevel = "&cI can't teach you any more, find a higher level trainer";
    public static String trainerLevelTooHigh = "&cI can't teach a low level like you, find a lower level trainer";
    public static String noMoreItemNeeded = "&aNo more &2<item.name> &ais needed";
    public static String moreItemNeeded = "&cAn additional &2<skill.cost.amount> <item.name> &cis required";
    public static String trainingSuccessful = "&6Training Successful!";
    public static String depositSuccessful = "&6Deposit Successful!";
    public static String trainerGUITitle = "&8<skill.name>&6 || &3<skill.level>/<skill.max.level>";
    public static String trainerOccupied = "&6Please wait. I am talking to someone else.";
    public static String trainerCooldown = "&6Sorry, I need time to recuperate.";
    public static String describeGeneral = "Effect Block Trigger: <effect.initiator> Block Output: <effect.output>. Effect value ranges from <effect.amount.low> - <effect.amount.high> for levels 0 to 30. Non specialists have the effect <effect.minor.amount> , as if they were level <effect.normal.level>. Tools affected: <effect.tool.type>.";
    public static String describeLevelExplosionDamage = "&6You take <effect.level.color><effect.damage.taken>% <effect.damage.suffix> &6damage from explosions";
    public static String describeLevelFireDamage = "&6You take <effect.level.color><effect.damage.taken>% <effect.damage.suffix> &6damage from fire";
    public static String describeLevelFallingDamage = "&6You take <effect.level.color><effect.damage.taken>% <effect.damage.suffix> &6damage from falling";
    public static String vanillaRace = "Sorry but you are the vanilla race. Change your race to use DwarfCraft";
    public static ArrayList<String> tutorial = new ArrayList<>();
}
