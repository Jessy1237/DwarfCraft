package com.Jessy1237.DwarfCraft.commands;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.Jessy1237.DwarfCraft.CommandInformation;
import com.Jessy1237.DwarfCraft.CommandParser;
import com.Jessy1237.DwarfCraft.DCCommandException;
import com.Jessy1237.DwarfCraft.DCPlayer;
import com.Jessy1237.DwarfCraft.DwarfCraft;
import com.Jessy1237.DwarfCraft.Race;
import com.Jessy1237.DwarfCraft.DCCommandException.Type;

public class CommandRace extends Command {
	private final DwarfCraft plugin;

	public CommandRace(final DwarfCraft plugin) {
		super("Race");
		this.plugin = plugin;
	}
	
	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args){
		if (DwarfCraft.debugMessagesThreshold < 1)
			System.out.println("DC1: started command 'race'");

		if (args.length == 0 && sender instanceof Player){
			plugin.getOut().race(sender, (Player)sender);
		} else if (args.length==0) {
			plugin.getOut().sendMessage(sender, CommandInformation.Usage.RACE.getUsage());
		} else if (args[0].equalsIgnoreCase("?")) {
			plugin.getOut().sendMessage(sender, CommandInformation.Desc.RACE.getDesc());
		}else{
			try{
				CommandParser parser = new CommandParser(plugin, sender, args);
				List<Object> desiredArguments = new ArrayList<Object>();
				List<Object> outputList = null;
				
				
				DCPlayer dCPlayer = new DCPlayer(plugin, null);
				Race newRace = null;
				Boolean confirmed = false;
				desiredArguments.add(dCPlayer);
				desiredArguments.add(newRace);
				desiredArguments.add(confirmed);
				try {
					outputList = parser.parse(desiredArguments, false);
					dCPlayer   = (DCPlayer)outputList.get(0);
					newRace    = (Race)outputList.get(1);
					confirmed  = (Boolean)outputList.get(2);
					if (sender.isOp())
						race(newRace, confirmed, dCPlayer, sender);
					
				} catch (DCCommandException e) {
					if (e.getType() == Type.TOOFEWARGS) {
						desiredArguments.remove(0);
						desiredArguments.add(dCPlayer);
						outputList = parser.parse(desiredArguments, true);
						dCPlayer   = (DCPlayer)outputList.get(2);
						newRace    = (Race)outputList.get(0);
						confirmed  = (Boolean)outputList.get(1);
					}
					else throw e;
				} catch (IndexOutOfBoundsException f) {
					plugin.getOut().race(sender, (Player) sender);
					return true;
				}
				race(newRace, confirmed, dCPlayer, sender);
			} catch (DCCommandException e) {
				e.describe(sender);
				sender.sendMessage(CommandInformation.Usage.RACE.getUsage());
				return false;		
			}
		}
		return true;		
	}
	
	private void race(Race newRace, boolean confirm, DCPlayer dCPlayer, CommandSender sender) {
		if (dCPlayer.getRace() == newRace) {
			if (confirm)
				plugin.getOut().resetRace(sender, dCPlayer, newRace);
			else
				plugin.getOut().alreadyRace(sender, dCPlayer, newRace);
		} else {
			if (confirm) {
				plugin.getOut().changedRace(sender, dCPlayer, newRace);
				dCPlayer.changeRace(newRace);
			} 
			else {
				plugin.getOut().confirmRace(sender, dCPlayer, newRace);
			}
		}
	}
}
