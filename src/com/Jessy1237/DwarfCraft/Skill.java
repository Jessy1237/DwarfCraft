package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.util.List;

import org.bukkit.Material;

public class Skill implements Cloneable
{
	
	private final int			mID;
	private final String		mName;
	private int					mLevel;
	private final List<Effect>	mEffects;
	private final Material		mHeldItem;
	public final TrainingItem	Item1;
	public final TrainingItem	Item2;
	public final TrainingItem	Item3;
	private int					deposit1;
	private int					deposit2;
	private int					deposit3;
	
	public Skill(int id, String displayName, int level, List<Effect> effects,
			TrainingItem item1, TrainingItem item2, TrainingItem item3,
			Material trainerHeldMaterial)
	{
		mID = id;
		mName = displayName;
		
		Item1 = item1;
		Item2 = item2;
		Item3 = item3;
		
		mLevel = level;
		mEffects = effects;
		mHeldItem = trainerHeldMaterial;
	}
	
	/**
	 * My attempt at making a cloneable class.
	 * 
	 * Known issue: it does not clone the effects table or itemStack table.
	 * This is not a problem because effects are 100% final, and ItemStack is
	 * never modified.
	 */
	@Override
	public Skill clone()
	{
		
		Skill newSkill = new Skill(mID, mName, mLevel, mEffects, Item1,
				Item2, Item3, mHeldItem);
		return newSkill;
	}
	
	public String getDisplayName()
	{
		return mName;
	}
	
	public List<Effect> getEffects()
	{
		return mEffects;
	}
	
	public int getId()
	{
		return mID;
	}
	
	public int getLevel()
	{
		return mLevel;
	}
	
	protected Material getTrainerHeldMaterial()
	{
		return mHeldItem;
	}
	
	public void setLevel(int newLevel)
	{
		mLevel = newLevel;
	}
	
	@Override
	public String toString()
	{
		return mName.toUpperCase().replaceAll(" ", "_");
	}
	
	public void setDeposit1(int deposit1)
	{
		this.deposit1 = deposit1;
	}
	
	public int getDeposit1()
	{
		return deposit1;
	}
	
	public void setDeposit2(int deposit2)
	{
		this.deposit2 = deposit2;
	}
	
	public int getDeposit2()
	{
		return deposit2;
	}
	
	public void setDeposit3(int deposit3)
	{
		this.deposit3 = deposit3;
	}
	
	public int getDeposit3()
	{
		return deposit3;
	}
}
