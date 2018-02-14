package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

public final class Messages
{

    private static DwarfCraft plugin;

    public Messages( DwarfCraft plugin )
    {
        Messages.plugin = plugin;
    }

    // String messages fixed for DwarfCraft, and backup messages when loading
    // fails.
    public enum Fixed
    {
        TUTORIAL1( "&5&lWelcome to DwarfCraft!\n\n&0You have a set of skills that let you do certain tasks better. When you first start, things may be more difficult than you are used to, but as you level your skills up you will be much more productive." ),
        TUTORIAL2( "[{\"text\":\"Your skillsheet lists all skills that are affecting you. Lets find out more about the &bPickaxe Use&0 skill.\\n\\n\"},{\"text\":\"Type /dc skillinfo pickaxe_use\",\"color\":\"dark_red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/dc skillinfo pickaxe_use\"}}]" ),
        TUTORIAL3( "&0The skill info shows that your low level Pickaxe Use skill will cause your pickaxe to have lower durability causing it to break easier. If you increase this skill enough, you'll be able to mine much longer before your pickaxe breaks. " +
                "Below that, it shows what you need to train the skill. Find a nearby trainer (NPC) in the world and left click them to get more information about the skill they teach or right click to begin training"),
        TUTORIAL4( "&0When you tried to train the skill, it showed what training cost was missing. All skills train for a cost in relevant materials. The first few levels cost little," + "but becoming a master is very challenging." ),
        TUTORIAL5( "&0Most trainers can only take you to a limited level, you'll need to seek out the best trainers in the world to eventually reach level " + plugin.getConfigManager().getMaxSkillLevel() + " in a skill. Go gather some dirt, stone, or logs and " +
                "try to train up a relevant skill, using what you have learned" ),
        TUTORIAL6( "&0You now know the basic commands you need to succeed and develop. To find out more, use &4/dc help&0" ),
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
    public static String welcomePrefix = "&6[DC]         ";
    public static String welcome = "&fWelcome, &9%playerrace% &6%playername%";
    public static String skillSheetPrefix = "&6[&dSS&6] ";
    public static String skillSheetHeader = "&6Skill Sheet for &9%playername% &6[&9%playerrace% &6- &6Lvl &3%playerlevel%&6]";
    public static String skillSheetSkillLine = "&6[&3%skilllevel%&6] &b%skillname%";
    public static String skillSheetUntrainedSkillHeader = "&6Untrained Skills%colon%";
    public static String skillSheetUntrainedSkillLine = "|&7%skillname%&6| ";
    public static String skillInfoHeader = "&6Skill Info: &b%skillname%&6 [&b%skillid%&6] || Your level &3%skilllevel%/%maxskilllevel%";
    public static String skillInfoMinorHeader = "&6[&5EffectID&6]&f------&6[Effect]&f------";
    public static String skillInfoEffectIDPrefix = "&6[&5%effectid%&6] ";
    public static String skillInfoMaxSkillLevel = "&6---This skill is maximum level, no training available---";
    public static String skillInfoAtTrainerLevel = "&6---You're as skilled as me, you need a more advanced trainer!--";
    public static String skillInfoTrainCostHeader = "&6---Train costs for level &3%nextskilllevel%";
    public static String skillInfoTrainCost = " &6--  &2%depositedamount% of %totalcost% %itemtype%&6  --";
    public static String effectInfoPrefix = "&6[&5%effectid%&6] ";
    public static String raceCheck = "You are a %playerrace%";
    public static String adminRaceCheck = "%playername% is a %playerrace%";
    public static String alreadyRace = "You are already a %playerrace%";
    public static String resetRace = "You are once again a fresh new %racename%";
    public static String changedRace = "You are now a %racename%";
    public static String confirmRace = "You need to confirm this command with confirm at the end. (Note: This will reset all your skills)";
    public static String raceDoesNotExist = "The race %racename% doesn't exist";
    public static String chooseARace = "&cPlease choose a race!";
    public static String trainSkillPrefix = "&6[Train &b%skillid%&6] ";
    public static String raceDoesNotContainSkill = "&cYour race doesn't have this skill!";
    public static String raceDoesNotSpecialize = "&cYour race doesn't specialize in this skill! Max level is (%racelevellimit%)!";
    public static String maxSkillLevel = "&cYour skill is max level (%maxskilllevel%)!";
    public static String trainerMaxLevel = "&cI can't teach you any more, find a higher level trainer";
    public static String trainerLevelTooHigh = "&cI can't teach a low level like you, find a lower level trainer";
    public static String noMoreItemNeeded = "&aNo more &2%itemname% &ais needed";
    public static String moreItemNeeded = "&cAn additional &2%costamount% %itemname% &cis required";
    public static String trainingSuccessful = "&6Training Successful!";
    public static String depositSuccessful = "&6Deposit Successful!";
    public static String trainerGUITitle = "&8%skillname%&6 [&b%skillid%&6] || &3%skilllevel%/%maxskilllevel%";
    public static String trainerOccupied = "&6Please wait. I am talking to someone else.";
    public static String trainerCooldown = "&6Sorry, I need time to recuperate.";
    public static String describeGeneral = "Effect Block Trigger: %initiator% Block Output: %output%. Effect value ranges from %effectamountlow% - %effectamounthigh% for levels 0 to 30. Non specialists have the effect %minoramount% , as if they were level %normallevel%. Tools affected: %tooltype%.";
    public static String describeLevelBlockdrop = "&6Break a &2%initiator% &6and %effectlevelcolor%%effectamount% &2%output%&6 are created";
    public static String describeLevelMobdrop = "&6%creaturename% drop about %effectlevelcolor%%effectamount% &2%output%";
    public static String describeLevelMobdropNoCreature = "&6Enemies that drop &2%output% &6leave about %effectlevelcolor%%effectamount%&6";
    public static String describeLevelSwordDurability = "&6Using &2%tooltype% &6removes about %effectlevelcolor%%effectamount% &6durability";
    public static String describeLevelPVPDamage = "&6You do %effectlevelcolor%%effectdamage%&6% &6of normal &2%tooltype% &6damage when fighting players";
    public static String describeLevelPVEDamage = "&6You do %effectlevelcolor%%effectdamage%&6% &6of normal &2%tooltype% &6damage when fighting mobs";
    public static String describeLevelExplosionDamageMore = "&6You take %effectlevelcolor%%effecttakedamage%% more &6damage from explosions";
    public static String describeLevelExplosionDamageLess = "&6You take %effectlevelcolor%%effecttakedamage%% less &6damage from explosions";
    public static String describeLevelFireDamageMore = "&6You take %effectlevelcolor%%effecttakedamage%% more &6damage from fire";
    public static String describeLevelFireDamageLess = "&6You take %effectlevelcolor%%effecttakedamage%% less &6damage from fire";
    public static String describeLevelFallingDamageMore = "&6You take %effectlevelcolor%%effecttakedamage%%% more &6damage from falling";
    public static String describeLevelFallingDamageLess = "&6You take %effectlevelcolor%%effecttakedamage%% less &6damage from falling";
    public static String describeLevelFallThreshold = "&6Fall damage less than %effectlevelcolor%%effectamountint% &6does not affect you.";
    public static String describeLevelPlowDurability = "&6Using &2%tooltype% &6removes about %effectlevelcolor%%effectamount% &6durability";
    public static String describeLevelToolDurability = "&6Using &2%tooltype% &6removes about %effectlevelcolor%%effectamount% &6durability";
    public static String describeLevelRodDurability = "&6Using &2%tooltype% &6removes about %effectlevelcolor%%effectamount% &6durability";
    public static String describeLevelEat = "&6You gain %effectlevelcolor%%effectamountfood% &6Hunger instead of &e%originalfoodlevel%&6 when you eat &2%initiator%";
    public static String describeLevelCraft = "&6You craft %effectlevelcolor%%.0%effectamount% &2%output% &6instead of &e%minoramount%";
    public static String describeLevelPlow = "&6You gain %effectlevelcolor%%effectamount% &6seeds instead of &e%minoramount% &6when you plow grass";
    public static String describeLevelFish = "&6You catch %effectlevelcolor%%effectamount% &6fish instead of &e%minoramount% &6when you fish";
    public static String describeLevelBrew = "&6You brew %effectlevelcolor%%effectamount% &6potion(s) instead of &e%minoramount% &6when you're brewing potions";
    public static String describeLevelDigTime = "&a%effectamountdig%%&6 of the time &2%tooltype% &6break &2%initiator% &6instantly";
    public static String describeLevelBowAttack = "&6Your Arrows (Fully Charge Bow) do %effectlevelcolor%%effectbowdamage% &6hp damage (half hearts)";
    public static String describeLevelVehicleDrop = "&6When you break a boat &6approx. %effectlevelcolor%%effectamount% &2%output%&6 are created";
    public static String describeLevelVehicleMove = "&6Your boat travels %effectlevelcolor%%effecttakedamage%% &6faster than normal";
    public static String describeLevelSmelt = "&6Smelt a &2%initiator% &6and %effectlevelcolor%%effectamount% &2%output%&6 are created as well";
    public static String describeLevelShear = "&6Shear a %creaturename% and %effectlevelcolor%%effectamount% &6%output% are dropped instead of &e%minoramount%";
    public static String effectLevelColorGreaterThanNormal = "&a";
    public static String effectLevelColorEqualToNormal = "&e";
    public static String effectLevelColorLessThanNormal = "&c";
    public static String vanillaRace = "Sorry but you are the vanilla race. Change your race to use DwarfCraft";
}