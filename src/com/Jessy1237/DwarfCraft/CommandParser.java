package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Jessy1237.DwarfCraft.DCCommandException.Type;

public final class CommandParser
{
    private final DwarfCraft plugin;
    private CommandSender sender;
    private String[] input;
    private DwarfPlayer target = null;

    public CommandParser( final DwarfCraft plugin, CommandSender sender, String[] args )
    {
        this.plugin = plugin;
        this.sender = sender;
        this.input = args;
    }

    public List<Object> parse( List<Object> desiredArguments, boolean ignoreSize ) throws DCCommandException
    {
        List<Object> output = new ArrayList<Object>();
        int arrayIterator = 0;
        try
        {
            for ( Object o : desiredArguments )
            {
                if ( o instanceof DwarfPlayer)
                    output.add( parseDwarf( arrayIterator ) );
                else if ( o instanceof Player )
                    output.add( parsePlayer( arrayIterator ) );
                else if ( o instanceof DwarfSkill)
                    output.add( parseSkill( arrayIterator ) );
                else if ( o instanceof DwarfEffect)
                    output.add( parseEffect( arrayIterator ) );
                else if ( o instanceof Boolean )
                    output.add( parseConfirm( arrayIterator ) );
                else if ( o instanceof String && ( ( String ) o ).equalsIgnoreCase( "SkillLevelInt" ) )
                    output.add( parseSkillLevel( arrayIterator ) );
                else if ( o instanceof String && ( ( String ) o ).equalsIgnoreCase( "UniqueIdAdd" ) )
                    output.add( parseUniqueId( arrayIterator, true ) );
                else if ( o instanceof String && ( ( String ) o ).equalsIgnoreCase( "UniqueIdRmv" ) )
                    output.add( parseUniqueId( arrayIterator, false ) );
                else if ( o instanceof String && ( ( String ) o ).equalsIgnoreCase( "Name" ) )
                    output.add( parseName( arrayIterator ) );
                else if ( o instanceof String && ( ( String ) o ).equalsIgnoreCase( "GreeterMessage" ) )
                    output.add( parseGreeterMessage( arrayIterator ) );
                else if ( o instanceof String && ( ( String ) o ).equalsIgnoreCase( "Type" ) )
                    output.add( parseType( arrayIterator ) );
                else if ( o instanceof Integer )
                    output.add( parseInteger( arrayIterator ) );
                else if ( o instanceof String )
                    output.add( o );
                arrayIterator++;
            }
        }
        catch ( ArrayIndexOutOfBoundsException e )
        {
            throw new DCCommandException( plugin, Type.TOOFEWARGS );
        }
        if ( input.length > output.size() && !ignoreSize )
            throw new DCCommandException( plugin, Type.TOOMANYARGS );
        if ( input.length < output.size() && !ignoreSize )
            throw new DCCommandException( plugin, Type.TOOFEWARGS );
        return output;
    }

    private Object parseConfirm( int arrayIterator )
    {
        try
        {
            if ( input[arrayIterator].equalsIgnoreCase( "confirm" ) )
                return true;
        }
        catch ( IndexOutOfBoundsException e )
        {
            return false;
        }
        return false;
    }

    @SuppressWarnings( "deprecation" )
    private DwarfPlayer parseDwarf(int argNumber ) throws DCCommandException
    {
        Player player;
        DwarfPlayer dCPlayer = null;
        try
        {
            String dwarf = input[argNumber];
            player = sender.getServer().getPlayer( dwarf );
            if ( player != null && player.isOnline() )
                dCPlayer = plugin.getDataManager().find( player );
            else if ( player == null || !player.isOnline() )
            {
                System.out.println( "looking for offline player" );
                dCPlayer = plugin.getDataManager().findOffline( plugin.getServer().getOfflinePlayer( dwarf ).getUniqueId() );
            }
            if ( dCPlayer == null )
            {
                throw new DCCommandException( plugin, Type.PARSEDWARFFAIL );
            }
            this.target = dCPlayer;
            return dCPlayer;
        }
        catch ( ArrayIndexOutOfBoundsException e )
        {
            if ( sender instanceof Player )
            {
                String[] fakeInput = new String[input.length + 1];
                for ( int i = 0; i < input.length; i++ )
                    fakeInput[i] = input[i];
                input = fakeInput;
                this.target = plugin.getDataManager().find( ( Player ) sender );
                return target;
            }
            else
                throw new DCCommandException( plugin, Type.CONSOLECANNOTUSE );
        }

    }

    private DwarfEffect parseEffect(int argNumber ) throws DCCommandException
    {
        String inputString = input[argNumber];
        DwarfEffect effect;
        int effectId;

        try
        {
            effectId = Integer.parseInt( inputString );
            effect = target.getEffect( effectId );
        }
        catch ( NullPointerException npe )
        {
            throw new DCCommandException( plugin, Type.EMPTYPLAYER );
        }
        catch ( NumberFormatException nfe )
        {
            throw new DCCommandException( plugin, Type.PARSEEFFECTFAIL );
        }
        if ( effect == null )
            throw new DCCommandException( plugin, Type.PARSEEFFECTFAIL );
        return effect;
    }

    private String parseGreeterMessage( int arrayIterator ) throws DCCommandException
    {
        if ( plugin.getDataManager().getGreeterMessage( input[arrayIterator] ) == null )
            throw new DCCommandException( plugin, Type.NOGREETERMESSAGE );
        String greeterMessage = input[arrayIterator];
        return greeterMessage;
    }

    private Object parseInteger( int argNumber ) throws DCCommandException
    {
        int i;
        try
        {
            i = Integer.parseInt( input[argNumber] );
        }
        catch ( NumberFormatException nfe )
        {
            throw new DCCommandException( plugin, Type.PARSEINTFAIL );
        }
        return i;
    }

    private Object parseName( int arrayIterator )
    {
        String name = input[arrayIterator];
        return name;
    }

    private Object parseType( int arrayIterator )
    {
        String type = input[arrayIterator];
        return type;
    }

    private Object parsePlayer( int arrayIterator ) throws DCCommandException
    {
        Player player = sender.getServer().getPlayer( input[arrayIterator] );
        if ( player == null )
            throw new DCCommandException( plugin, Type.PARSEPLAYERFAIL );
        return null;
    }

    private DwarfSkill parseSkill(int argNumber ) throws DCCommandException
    {
        DwarfSkill skill = null;
        String inputString = input[argNumber];
        if ( inputString.equalsIgnoreCase( "all" ) )
            return null;
        int skillID;

        if ( target == null )
            target = plugin.getDataManager().find( ( Player ) sender );
        if ( !( sender instanceof Player ) )
        {
            try
            {
                skillID = Integer.parseInt( inputString );
                return plugin.getConfigManager().getAllSkills().get( skillID );
            }
            catch ( NumberFormatException nfe )
            {
                if ( inputString.length() < 8 )
                    throw new DCCommandException( plugin, Type.PARSESKILLFAIL );
                for ( int i : plugin.getConfigManager().getAllSkills( target.getRace() ) )
                {
                    DwarfSkill s = plugin.getConfigManager().getGenericSkill( i );
                    if ( s.getDisplayName().regionMatches( 0, inputString, 0, 8 ) )
                        return s;
                }
            }
            throw new DCCommandException( plugin, Type.PARSESKILLFAIL );
        }
        try
        {
            try
            {
                skillID = Integer.parseInt( inputString );
                skill = target.getSkill( skillID );
            }
            catch ( NumberFormatException nfe )
            {
                skill = target.getSkill( inputString );
            }
        }
        catch ( NullPointerException npe )
        {
            throw new DCCommandException( plugin, Type.EMPTYPLAYER );
        }
        if ( skill == null )
            throw new DCCommandException( plugin, Type.PARSESKILLFAIL );
        return skill;
    }

    private int parseSkillLevel( int argNumber ) throws DCCommandException
    {
        String inputString = input[argNumber];
        int level;
        try
        {
            level = Integer.parseInt( inputString );
        }
        catch ( NumberFormatException nfe )
        {
            throw new DCCommandException( plugin, Type.PARSELEVELFAIL );
        }
        if ( level > 30 || level < -1 )
        {
            throw new DCCommandException( plugin, Type.LEVELOUTOFBOUNDS );
        }
        return level;
    }

    private Object parseUniqueId( int arrayIterator, boolean add ) throws DCCommandException
    {
        String uniqueId = input[arrayIterator];
        if ( plugin.getDataManager().getTrainer( uniqueId ) != null && add )
            throw new DCCommandException( plugin, Type.NPCIDINUSE );
        if ( plugin.getDataManager().getTrainer( uniqueId ) == null && !add )
            throw new DCCommandException( plugin, Type.NPCIDNOTFOUND );
        return uniqueId;
    }
}
