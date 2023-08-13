/*
 * Copyright (c) 2018.
 *
 * DwarfCraft is an RPG plugin that allows players to improve their characters
 * skills and capabilities through training, not experience.
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos and RCarretta
 */

package com.jessy1237.dwarfcraft;

import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.jessy1237.dwarfcraft.models.DwarfPlayer;
import com.jessy1237.dwarfcraft.models.DwarfSkill;
import com.jessy1237.dwarfcraft.models.effects.DwarfEffect;

public class Out
{
    /*
     * Messaging Statics
     */
    private final int lineLength = 320; // pixels
    private final int maxLines = 20;
    private final DwarfCraft plugin;

    protected Out( DwarfCraft plugin )
    {
        this.plugin = plugin;
    }

    private String consoleLinePrinter( String line, String prefix )
    {
        if ( plugin != null )
        {
            plugin.getServer().getConsoleSender().sendMessage( prefix + line );
        }

        return null;
    }

    public void info( CommandSender sender )
    {
        sendMessage( sender, Messages.Fixed.GENERALINFO.getMessage(), "&6[&d?&6]" );
    }

    /**
     * Removes carriage returns from strings and passes separate
     *
     * @param message
     * @param prefix
     */
    private void messagePrinter( String message, String prefix )
    {
        String[] lines = message.split( "\\n" );
        String lastColor = "";
        for ( String line : lines )
            lastColor = consoleLinePrinter( lastColor.concat( line ), prefix );
    }

    /**
     * Removes carriage returns from strings and passes separate
     * 
     * @param player
     * @param message
     * @param prefix
     */
    private void messagePrinter( Player player, String message, String prefix )
    {
        String[] lines = message.split( "\\n" );
        String lastColor = "";
        for ( String line : lines )
            lastColor = playerLinePrinter( player, lastColor.concat( line ), prefix );
    }

    /**
     * Used to parse and send multiple line messages Sends actual output commands
     */
    private String playerLinePrinter( Player player, String message, String prefix )
    {
        int messageSectionLength = lineLength - plugin.getUtil().msgLength( prefix );
        String currentLine = "";
        String words[] = message.split( " " );
        String lastColor = "";
        int lineTotal = 0;

        for ( String word : words )
        {
            if ( plugin.getUtil().msgLength( currentLine ) + plugin.getUtil().msgLength( word ) <= messageSectionLength )
            {
                currentLine = currentLine.concat( word + " " );
            }
            else
            {
                player.sendMessage( parseColors( prefix.concat( lastColor + currentLine ).trim() ) );
                lineTotal++;

                if ( lineTotal >= maxLines )
                    return lastColor;

                lastColor = lastColor( lastColor + currentLine );
                currentLine = word + " ";
            }
        }

        player.sendMessage( parseColors( prefix.concat( lastColor + currentLine ).trim() ) );
        return lastColor = lastColor( lastColor + currentLine );
    }

    public void printSkillInfo(CommandSender sender, DwarfSkill skill, DwarfPlayer dCPlayer, int maxTrainLevel )
    {
        // general line
        sendMessage( sender, skill.description( Messages.skillInfoHeader, dCPlayer ) );

        // effects lines
        for ( DwarfEffect effect : skill.getEffects() )
        {
            if ( effect != null )
                sendMessage( sender, effect.description( dCPlayer ), Messages.skillInfoEffectIDPrefix );
        }

        // training lines
        if ( skill.getLevel() >= skill.getMaxLevel(dCPlayer) )
        {
            sendMessage( sender, Messages.skillInfoMaxSkillLevel );
            return;
        }

        if ( skill.getLevel() >= maxTrainLevel )
        {
            sendMessage( sender, Messages.skillInfoAtTrainerLevel );
            return;
        }

        sendMessage( sender, skill.description( Messages.skillInfoTrainCostHeader ) );
        List<List<ItemStack>> costsTurnins = dCPlayer.calculateTrainingCost( skill );
        List<ItemStack> remaining = costsTurnins.get( 0 );
        List<ItemStack> total = costsTurnins.get( 1 );
        for ( int i = 0; i < remaining.size(); i++ )
        {
            ItemStack r = remaining.get( i );
            ItemStack t = total.get( i );
            if ( r != null && t != null )
            {
                int totalCost = t.getAmount();
                int deposited = t.getAmount() - r.getAmount();
                sendMessage( sender, parseForTrainCosts( Messages.skillInfoTrainCost, deposited, r.getAmount(), totalCost, plugin.getUtil().getCleanName( skill.getItem( i+1 ).getDwarfItemHolder() ) ) );
            }

        }
    }

    public void printSkillSheet( DwarfPlayer dCPlayer, CommandSender sender, boolean printFull )
    {
        String message1;
        String message2 = "";

        message1 = parseSkillSheet( Messages.skillSheetHeader, dCPlayer, null );
        sendMessage( sender, message1, "" );

        boolean odd = true;
        String untrainedSkills = Messages.skillSheetUntrainedSkillHeader;
        String seperator = ChatColor.GOLD + " | ";
        Iterator<DwarfSkill> iter = dCPlayer.getSkills().values().iterator();
        while (iter.hasNext())
        {
            DwarfSkill s = iter.next();
            if ( s.getLevel() == 0 )
            {
                if (!iter.hasNext()) seperator = "";
                untrainedSkills = untrainedSkills.concat( parseSkillSheet( Messages.skillSheetUntrainedSkillLine, dCPlayer, s ) ).concat(seperator);
                continue;
            }
            odd = !odd;
            // the goal here is for every skill sheet line to be 60 characters
            // long.
            // each skill should take 30 characters - no more, no less
            String interim = parseSkillSheet( Messages.skillSheetSkillLine, dCPlayer, s );

            if ( !odd )
            {
                int interimLen = plugin.getUtil().msgLength( interim );
                int numSpaces = ( ( 116 - interimLen ) / 4 ) - 1;
                for ( int i = 0; i < numSpaces; i++ )
                    interim = interim.concat( " " );
                interimLen = 116 - interimLen - numSpaces * 4;
                // 4 possible cases - need 4, 5, 6, or 7
                if ( interimLen == 4 )
                    interim = interim.concat( "&0| &b" );
                else if ( interimLen == 5 )
                    interim = interim.concat( "&0| &b" );
                else if ( interimLen == 6 )
                    interim = interim.concat( "&0 | &b" );
                else if ( interimLen == 7 )
                    interim = interim.concat( "&0  | &b" );
            }

            message2 = message2.concat( interim );
            if ( odd )
            {
                sendMessage( sender, message2, "" );
                message2 = "";
            }

        }
        if ( !message2.equals( "" ) )
            sendMessage( sender, message2, "" );
        if ( printFull )
            sendMessage( sender, untrainedSkills, "" );
    }

    /**
     * Used to send messages to all players on a server
     */
    public void sendBroadcast( String message )
    {
        sendBroadcast( message, "" );
    }

    /**
     * Used to send messages to all players on a server with a prefix
     */
    protected void sendBroadcast( String message, String prefix )
    {
        if ( plugin != null )
        {
            plugin.getServer().broadcastMessage( parseColors( prefix + message ) );
        }
    }

    /**
     * Used to send messages to one player or console
     */
    public void sendMessage( CommandSender sender, String message )
    {
        sendMessage( sender, message, "" );
    }

    /**
     * Used to send messages to one player with a prefix
     *
     */
    public void sendMessage( CommandSender sender, String message, String prefix )
    {
        if ( sender instanceof Player )
        {
            messagePrinter( ( Player ) sender, message, prefix );
        }
        else
        {
            message = stripColors( ChatColor.translateAlternateColorCodes('&', message ) );
            prefix = stripColors( ChatColor.translateAlternateColorCodes('&', prefix ) );
            messagePrinter( message, prefix );
        }
    }

    protected void sendMessage( DwarfPlayer dCPlayer, String message )
    {
        sendMessage( dCPlayer.getPlayer(), message );
    }

    private String stripColors( String message )
    {
        message = ChatColor.stripColor( message );
        return message;
    }

    /**
     * Finds &0-F in a string and replaces it with the color symbol
     */
    public String parseColors( String message )
    {
        return ChatColor.translateAlternateColorCodes( '&', message );
    }

    private String lastColor( String currentLine )
    {
        String lastColor = "";
        int lastIndex = Math.max( currentLine.lastIndexOf( "&" ), currentLine.lastIndexOf( "§" ) );
        if ( lastIndex == currentLine.length() )
            return "";
        if ( lastIndex != -1 )
        {
            lastColor = currentLine.substring( lastIndex, lastIndex + 2 );
        }

        return lastColor;
    }

    /**
     * Sends a welcome message based on race of player joining. Broadcasts to the whole server
     *
     * @param dCPlayer DwarfPlayer
     */
    public void welcome( DwarfPlayer dCPlayer )
    {
        try
        {
            if ( plugin.getConfigManager().sendGreeting )
                sendBroadcast( dCPlayer.toString( Messages.welcome ), Messages.welcomePrefix );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    public void race( CommandSender sender, Player player )
    {
        sendMessage( sender, parseRace( Messages.raceCheck, plugin.getDataManager().find( player ), null ) );
    }

    public void adminRace( CommandSender sender, DwarfPlayer player )
    {
        sendMessage( sender, parseRace( Messages.adminRaceCheck, player, null ) );
    }

    public void alreadyRace( CommandSender sender, DwarfPlayer dCPlayer, String newRace )
    {
        sendMessage( sender, parseRace( Messages.alreadyRace, dCPlayer, newRace ) );
    }

    public void changedRace( CommandSender sender, DwarfPlayer dCPlayer, String newRace )
    {
        sendMessage( sender, parseRace( Messages.changedRace, dCPlayer, newRace ) );
    }

    public void confirmRace( CommandSender sender, DwarfPlayer dCPlayer, String newRace )
    {
        sendMessage( sender, parseRace( Messages.confirmRace, dCPlayer, newRace ) );
    }

    public void dExistRace( CommandSender sender, DwarfPlayer dCPlayer, String newRace )
    {
        sendMessage( sender, parseRace( Messages.raceDoesNotExist, dCPlayer, newRace ) );
    }

    public String parseRace( String message, DwarfPlayer dCPlayer, String newRace )
    {
        String out = dCPlayer.toString(message);
        if ( newRace != null )
            out = out.replaceAll( Placeholder.RACE_NAME.value(), newRace );

        return out;
    }

    public String parseSkillSheet( String message, DwarfPlayer dCPlayer, DwarfSkill skill )
    {
        String out = dCPlayer.toString( message ).replaceAll( Placeholder.SKILL_MAX_LEVEL.value(), "" + plugin.getConfigManager().getMaxSkillLevel() ).replaceAll( Placeholder.RACE_LEVEL_LIMIT.value(), "" + plugin.getConfigManager().getRaceLevelLimit() );;
        if ( skill != null )
        {
            out = out.replaceAll( Placeholder.SKILL_LEVEL.value(), String.format( "%02d", skill.getLevel() ) );
            out = out.replaceAll( Placeholder.SKILL_NAME.value(), String.format( "%.18s", skill.getDisplayName() ) );
        }
        return out;
    }
    
    public String parseForTrainCosts( String text, int deposited, int costAmount, int totalCost, String itemType )
    {
        return text.replaceAll( Placeholder.SKILL_DEPOSIT_AMOUNT.value(), "" + deposited ).replaceAll( Placeholder.SKILL_TOTAL_COST.value(), "" + totalCost ).replaceAll( Placeholder.SKILL_ITEM_TYPE.value(), itemType )
                .replaceAll( Placeholder.SKILL_COST_AMOUNT.value(), "" + costAmount ).replaceAll( Placeholder.ITEM_NAME.value(), itemType );
    }

}
