/*
 * ToolEffect
 * 3/27/2022
 *
 * Copyright (c) 2016-2021
 * Licensed under LGPL 2.1
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt
 *
 * Authors: Jessy1237 and Drekryan
 * Original Authors: smartaleq, LexManos, and RCarretta
 */

package com.Jessy1237.DwarfCraft.models.effects;

import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Messages;
import com.Jessy1237.DwarfCraft.events.DwarfEffectEvent;
import com.Jessy1237.DwarfCraft.models.DwarfPlayer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ToolEffect extends DwarfEffect {
    protected boolean mRequireTool = false;
    protected Material[] mTools;
    protected DwarfCraft plugin;
    
    public ToolEffect(JsonElement element, String skill_id, DwarfCraft plugin) {
        super(element, skill_id, plugin);
        
        JsonObject json = element.getAsJsonObject();
        if ( !json.has("tools") || json.get("tools").getAsJsonArray().size() <= 0 )
            mTools = new Material[0];
        else
        {
            mRequireTool = true;
            JsonArray toolsArray = json.get("tools").getAsJsonArray();
            mTools = new Material[toolsArray.size()];
            if (toolsArray.size() > 0) {
                for (int x = 0; x < toolsArray.size(); x++) {
                    String material = toolsArray.get(x).toString().trim();
                    plugin.getUtil().checkMaterial( material, skill_id );
                    Material mat = Material.matchMaterial(material);
                    if (mat != null)
                        mTools[x] = mat;
                }
            }
        }
    }
    
    public boolean getToolRequired()
    {
        return mRequireTool;
    }
    
    public Material[] getTools()
    {
        return mTools;
    }
    
    public boolean checkTool( ItemStack tool )
    {
        if ( !mRequireTool )
            return true;
        
        if ( tool == null )
            return false;
        
        for ( Material mat : mTools )
            if ( mat == tool.getType() )
                return true;
        
        return false;
    }
    
    public String toolType()
    {
        for ( Material mat : mTools )
        {
            if ( mat == Material.IRON_SWORD )
                return "sword";
            if ( mat == Material.IRON_HOE )
                return "hoe";
            if ( mat == Material.IRON_AXE )
                return "axe";
            if ( mat == Material.WOODEN_PICKAXE )
                return "pickaxe";
            if ( mat == Material.IRON_PICKAXE )
                return "most pickaxes";
            if ( mat == Material.DIAMOND_PICKAXE )
                return "diamond pickaxe";
            if ( mat == Material.IRON_SHOVEL )
                return "shovel";
            if ( mat == Material.FISHING_ROD )
                return "fishing rod";
            if ( mat == Material.FLINT_AND_STEEL )
                return "flint and steel";
        }
        return "any tool";
    }
    
    public void damageTool(DwarfPlayer player, int base, ItemStack tool )
    {
        damageTool( player, base, tool, true );
    }
    
    @SuppressWarnings( "deprecation" )
    public
    void damageTool( DwarfPlayer player, int base, ItemStack tool, boolean negate )
    {
        short wear = ( short ) ( plugin.getUtil().randomAmount( getEffectAmount( player ) ) * base );
        Damageable dmg = ( Damageable ) tool.getItemMeta();
        
        if ( DwarfCraft.debugMessagesThreshold < 2 ) plugin.getUtil().consoleLog( Level.FINE, String.format( "DC2: Affected durability of a \"%s\" - Old: %d Base: %d Wear: %d", plugin.getUtil().getCleanName( tool ), tool.getDurability(), base, wear ) );
        
        // Some code taken from net.minecraft.server.ItemStack line 165.
        // Checks to see if damage should be skipped.
        if ( tool.containsEnchantment( Enchantment.DURABILITY ) )
        {
            int level = tool.getEnchantmentLevel( Enchantment.DURABILITY );
            Random r = new Random();
            if ( level > 0 && r.nextInt( level + 1 ) > 0 )
            {
                return;
            }
        }
        
        base = ( negate ? base : 0 );
        
        if ( wear == base ) return; // This is normal wear, skip everything and let MC handle
        // it
        // internally.
        
        DwarfEffectEvent e = new DwarfEffectEvent( player, this, null, null, null, null, ( double ) base, ( double ) wear, null, null, tool );
        plugin.getServer().getPluginManager().callEvent( e );
        
        if ( e.isCancelled() ) return;
        
        tool.setDurability( ( short ) ( tool.getDurability() + e.getAlteredDamage() - base ) );
        // This may have the side effect of causing items to flicker when they
        // are about to break
        // If this becomes a issue, we need to cast to a CraftItemStack, then
        // make CraftItemStack.item public,
        // And call CraftItemStack.item.damage(-base, player.getPlayer());
        
        if ( tool.getDurability() >= tool.getType().getMaxDurability() )
        {
            if ( tool.getType() == Material.IRON_SWORD && tool.getDurability() < 250 ) return;
            
            if ( tool.getAmount() > 1 )
            {
                tool.setAmount( tool.getAmount() - 1 );
                tool.setDurability( ( short ) -1 );
            }
            else
            {
                if ( player.getPlayer().getEquipment().getItemInMainHand().getType() == tool.getType() )
                {
                    player.getPlayer().getEquipment().setItemInMainHand( null );
                }
                else if ( player.getPlayer().getEquipment().getItemInOffHand().getType() == tool.getType() )
                {
                    player.getPlayer().getEquipment().setItemInOffHand( null );
                }
            }
        }
    }
    
    public String description( DwarfPlayer dCPlayer )
    {
        if ( dCPlayer == null ) return "An unknown error has occurred";
        String out;
        
        switch ( mType )
        {
            case SWORDDURABILITY:
                out = Messages.describeLevelSwordDurability;
                break;
            case PVPDAMAGE:
                out = Messages.describeLevelPVPDamage;
                break;
            case PVEDAMAGE:
                out = Messages.describeLevelPVEDamage;
                break;
            case PLOWDURABILITY:
                out = Messages.describeLevelPlowDurability;
                break;
            case TOOLDURABILITY:
                out = Messages.describeLevelToolDurability;
                break;
            case RODDURABILITY:
                out = Messages.describeLevelRodDurability;
                break;
            default: out = "&6This Effect description is not yet implemented: " + this.getEffectType().toString();
        }
        
        return out;
    }
}
