package com.Jessy1237.DwarfCraft.guis;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.models.DwarfPlayer;
import com.Jessy1237.DwarfCraft.models.DwarfSkill;
import com.Jessy1237.DwarfCraft.models.DwarfTrainer;

public class ListTrainersGUI extends DwarfGUI
{

    private ArrayList<DwarfTrainer[]> trainers = new ArrayList<DwarfTrainer[]>();
    private int page = 0; // TODO Support multiple pages
    private final int inventorySize = 54;
    private int numPages = 0;

    public ListTrainersGUI( DwarfCraft plugin, DwarfPlayer dwarfPlayer )
    {
        super( plugin, dwarfPlayer );
    }

    @Override
    public void init()
    {

        initTrainers();
        inventory = plugin.getServer().createInventory( dwarfPlayer.getPlayer(), inventorySize, "Trainers List" );
        initItems();
    }

    public DwarfTrainer getTrainerAtSlot( int slot )
    {
        if ( trainers == null )
            return null;

        DwarfTrainer trainer = null;
        if ( slot < trainers.get( page ).length )
            trainer = trainers.get( page )[slot];
        return trainer;
    }

    @Override
    public void remove()
    {
    }

    @Override
    public void click( InventoryClickEvent event )
    {
        if ( event.getRawSlot() < 45 )
        {
            DwarfTrainer trainer = getTrainerAtSlot( event.getRawSlot() );
            if ( trainer != null )
            {
                event.getWhoClicked().sendMessage( ChatColor.LIGHT_PURPLE + "Teleporting to " + trainer.getName() + " at X: " + trainer.getLocation().getX() + ", Y: " + trainer.getLocation().getY() + ", Z: " + trainer.getLocation().getZ() );
                event.getWhoClicked().teleport( trainer.getLocation().setDirection( trainer.getLocation().getDirection().multiply( -1 ) ) );

            }
        }
        // Previous Page
        else if ( event.getRawSlot() == 45 )
        {
            if ( page != 0 )
            {
                page--;
                initItems();
            }
        }
        // Next Page
        else if ( event.getRawSlot() == 53 )
        {
            if ( page < numPages - 1 )
            {
                page++;
                initItems();
            }
        }
    }

    private void initItems()
    {

        for ( int index = 0; index < trainers.get( 0 ).length; index++ )
        {
            DwarfTrainer trainer = getTrainerAtSlot( index );
            DwarfSkill skill = dwarfPlayer.getSkill( trainer.getSkillTrained() );

            ArrayList<String> lore = new ArrayList<>();
            lore.add( ChatColor.GOLD + "Skill: " + ChatColor.RED + skill.getDisplayName() );
            lore.add( ChatColor.GOLD + "Min Level: " + ChatColor.WHITE + trainer.getMinSkill() );
            lore.add( ChatColor.GOLD + "Max Level: " + ChatColor.WHITE + trainer.getMaxSkill() );
            lore.add( ChatColor.GOLD + "Loc: " + ChatColor.WHITE + trainer.getLocation().getX() + ", " + trainer.getLocation().getY() + ", " + trainer.getLocation().getZ() );
            lore.add( "" );
            lore.add( ChatColor.LIGHT_PURPLE + "Click to teleport to Trainer..." );

            // Apparently causes too many requests of GameProfile lookups and Mojang times out
            /**
             * ItemStack skull = new ItemStack( Material.SKULL_ITEM, 1, ( short ) 3 ); SkullMeta meta = ( SkullMeta ) skull.getItemMeta(); meta.setOwner( trainer.getName() ); skull.setItemMeta( meta
             * );
             */

            addItem( trainer.getName(), lore, index, new ItemStack( Material.SKULL_ITEM, 1, ( short ) 3 ) );
        }

        addItem( "Previous Page", null, 45, new ItemStack( Material.STAINED_GLASS_PANE, 1, ( short ) 14 ) );
        addItem( "Next Page", null, 53, new ItemStack( Material.STAINED_GLASS_PANE, 1, ( short ) 5 ) );
    }

    private void initTrainers()
    {
        DwarfTrainer[] tempArray = new DwarfTrainer[getArraySize( 0 )];
        int num = 0;
        int index = 0;
        for ( DwarfTrainer trainer : plugin.getDataManager().trainerList.values() )
        {
            if ( index == 45 )
            {
                trainers.add( tempArray );
                tempArray = new DwarfTrainer[getArraySize( num )];
                index = 0;
                numPages++;
            }

            tempArray[index] = trainer;
            num++;
            index++;
        }

        trainers.add( tempArray );
        numPages++;
    }

    private int getArraySize( int num )
    {
        int size = plugin.getDataManager().trainerList.size() - num;
        if ( plugin.getDataManager().trainerList.size() - num >= 45 )
        {
            size = 45;
        }
        return size;
    }
}
