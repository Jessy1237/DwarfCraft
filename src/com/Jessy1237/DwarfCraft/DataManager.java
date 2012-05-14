package com.Jessy1237.DwarfCraft;

/**
 * Original Authors: smartaleq, LexManos and RCarretta
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;

public class DataManager {

	private List<DCPlayer>                  dwarves = new ArrayList<DCPlayer>();
	private List<DwarfVehicle>              vehicleList = new ArrayList<DwarfVehicle>();
	public HashMap<String, DwarfTrainer>    trainerList = new HashMap<String, DwarfTrainer>();
	private HashMap<String, GreeterMessage> greeterMessageList = new HashMap<String, GreeterMessage>();
	private final ConfigManager             configManager;
	private final DwarfCraft plugin;
	private List<Player> trainerRemove = new ArrayList<Player>();
	private List<Player> trainerLookAt = new ArrayList<Player>();
	private Connection mDBCon;
	
	protected DataManager(DwarfCraft plugin, ConfigManager cm) {
		this.plugin = plugin;
		this.configManager = cm;
		dbInitialize();
		for (Iterator<World> i = plugin.getServer().getWorlds().iterator(); i.hasNext();) {
			populateTrainers(i.next());
		}
		
	}

	public void addVehicle(DwarfVehicle v) {
		vehicleList.add(v);
	}

	/**
	 * this is untested and quite a lot of new code, it will probably fail
	 * several times. no way to bugfix currently. Just praying it works
	 * 
	 * @param oldVersion
	 */
	private void buildDB(int oldVersion) {
		try {
			Statement statement = mDBCon.createStatement();
			ResultSet rs  = statement.executeQuery("select * from sqlite_master WHERE name = 'trainers';");
			if (!rs.next()){
				statement.executeUpdate(
						"create table trainers " +
						"  (" +
						"    world, uniqueId, name, skill, maxSkill, material, isGreeter, messageId, " +
						"    x, y, z, yaw, pitch" +
						"  );");
			}
			rs.close();

			rs = statement.executeQuery("select * from sqlite_master WHERE name = 'players';");
			if (!rs.next()){
				statement.executeUpdate("create table players ( id INTEGER PRIMARY KEY, name, race );");
			}
			rs.close();

			rs = statement.executeQuery("select * from sqlite_master WHERE name = 'skills';");
			if (!rs.next()){
				statement.executeUpdate(
						"CREATE TABLE 'skills' " +
						"  ( " +
						"    'player' INT, " +
						"    'id' int, " +
						"    'level' INT DEFAULT 0, " +
						"    'deposit1' INT DEFAULT 0, " + 
						"    'deposit2' INT DEFAULT 0, " +
						"    'deposit3' INT DEFAULT 0, " +
						"    PRIMARY KEY ('player','id') " +
						"  );"
				);
			}
			rs.close();
			
			rs = statement.executeQuery("select name from sqlite_master WHERE name LIKE 'dwarf%';");
			while(rs.next()){
				convertOld(rs.getString("name"));
				statement.execute("DROP TABLE " + rs.getString("name") + ";");
			}
			rs.close();
			
		} catch (SQLException e) {
			System.out.println("[SEVERE]DB not built successfully");
			e.printStackTrace();
			plugin.getServer().getPluginManager().disablePlugin(plugin);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void convertOld(String name) throws SQLException, ClassNotFoundException{
		ResultSet rs = mDBCon.createStatement().executeQuery("SELECT * FROM " + name + ";" );
		System.out.println("DC Init: Converting old table: " + name + " (may lag a little wait for complete message)");

		mDBCon.setAutoCommit(false);
		while(rs.next()){
			String playerName = rs.getString("playername");
			int id = getPlayerID(playerName);
			
			if(id == -1){
				PreparedStatement prep = mDBCon.prepareStatement("insert into players(name, race) values(?,?);" );
				prep.setString(1, playerName);
				prep.setString(2, (rs.getBoolean("iself") ? "Elf" : "Dwarf"));
				prep.execute();
				prep.close();
				id = getPlayerID(playerName);
			}	
			assert(id == -1);
			System.out.println(String.format("Converting %s id %d", playerName, id));

			PreparedStatement prep = mDBCon.prepareStatement("INSERT INTO skills(player, id, level, " +
																				"deposit1, deposit2, deposit3) " +
																				"values(?,?,?,?,?,?);");			
			HashMap<Integer,Skill> skills = plugin.getConfigManager().getAllSkills();
			for(Skill skill : skills.values()){
				
				prep.setInt(1, id);
				prep.setInt(2, skill.getId());
				try{
					prep.setInt(3, rs.getInt(skill.toString()));
					System.out.println(String.format("\t%s\t\t%d", skill.toString(), rs.getInt(skill.toString())));
				}catch(SQLException e){
					prep.setInt(3, 0);
					System.out.println(String.format("\t%s\t\t0 - Default", skill.toString()));
				}
				prep.setInt(4, 0);
				prep.setInt(5, 0);
				prep.setInt(6, 0);
				prep.addBatch();
			}
			prep.executeBatch();
			prep.close();			
		}
		mDBCon.setAutoCommit(true);
		System.out.println("DC Init: Converting of " + name + " complete");		
		rs.close();
	}

	public boolean checkTrainersInChunk(Chunk chunk) {
		for (Iterator<Map.Entry<String, DwarfTrainer>> i = trainerList.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, DwarfTrainer> pairs = i.next();
			DwarfTrainer d = (pairs.getValue());
			if (Math.abs(chunk.getX() - d.getLocation().getBlock().getChunk().getX()) > 1)
				continue;
			if (Math.abs(chunk.getZ() - d.getLocation().getBlock().getChunk().getZ()) > 1)
				continue;
			return true;
		}
		return false;
	}

	public DCPlayer createDwarf(Player player) {
		DCPlayer newDwarf = new DCPlayer(plugin, player);
		newDwarf.changeRace(plugin.getConfigManager().getDefaultRace());
		newDwarf.setSkills(plugin.getConfigManager().getAllSkills(newDwarf.getRace()));
		
		for (Skill skill : newDwarf.getSkills().values()) {
			skill.setLevel(0);
			skill.setDeposit1(0);
			skill.setDeposit2(0);
			skill.setDeposit3(0);
		}
		
		if (player != null)
			dwarves.add(newDwarf);
		return newDwarf;
	}

	public void createDwarfData(DCPlayer dCPlayer) {
		createDwarfData(dCPlayer.getPlayer().getName(), dCPlayer.isElf());
	}
	protected void createDwarfData(String name, boolean isElf) {
		try {
			PreparedStatement prep = mDBCon.prepareStatement("insert into players(name, race) values(?,?);" );
			prep.setString(1, name);
			prep.setString(2, isElf ? "Elf" : "Dwarf");
			prep.execute();
			prep.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void dbInitialize() {
		try {
			Class.forName("org.sqlite.JDBC");
			mDBCon = DriverManager.getConnection("jdbc:sqlite:" + configManager.getDbPath());
			Statement statement = mDBCon.createStatement();
			ResultSet rs = statement.executeQuery("select * from sqlite_master WHERE name = 'players';");
			if (!rs.next()){
				buildDB(0);
			}
			
			//check for update to skill deposits
			try {
			rs = statement.executeQuery("SELECT deposit1 FROM skills;");
			} catch (SQLException ex) {
				statement.executeUpdate("ALTER TABLE skills ADD COLUMN deposit1 NUMERIC DEFAULT 0;");
				statement.executeUpdate("ALTER TABLE skills ADD COLUMN deposit2 NUMERIC DEFAULT 0;");
				statement.executeUpdate("ALTER TABLE skills ADD COLUMN deposit3 NUMERIC DEFAULT 0;");
			}	
			
			rs.close();
			mDBCon.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private void dbFinalize(){
		try{
			mDBCon.commit();
			mDBCon.close();
			mDBCon = null;
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * Finds a dwarf from the server's static list based on player's name
	 * 
	 * @param player
	 * @return dwarf or null
	 */
	public DCPlayer find(Player player) {
		for (DCPlayer d : dwarves) {
			if (d != null){
				if (d.getPlayer() != null){
					if (d.getPlayer().getName().equalsIgnoreCase(player.getName())){
						d.setPlayer(player);
						return d;	
					}
				}
			}			
		}
		return null;
	}
	
	protected DCPlayer findOffline(String name) {
		DCPlayer dCPlayer = createDwarf(null);
		if (getDwarfData(dCPlayer, name))
			return dCPlayer;
		else {
			// No dwarf or data found
			return null;
		}
	}

	public boolean getDwarfData(DCPlayer player) {
		return getDwarfData(player, player.getPlayer().getName());
	}

	/**
	 * Used for creating and populating a dwarf with a null(off line) player
	 * 
	 * @param player
	 * @param name
	 */
	private boolean getDwarfData(DCPlayer player, String name) {
		try {
			PreparedStatement prep = mDBCon.prepareStatement("select * from players WHERE name = ?;");
			prep.setString(1, name);
			ResultSet rs = prep.executeQuery();
			
			if (!rs.next())
				return false;
			
			//System.out.println("DC: PlayerJoin success for " + player.getPlayer().getName() + " id " + rs.getInt("id"));
			
			player.changeRace(plugin.getConfigManager().findRace(rs.getString("race"), false));
			int id = rs.getInt("id");
			rs.close();
			
			prep.close();
			prep = mDBCon.prepareStatement("select id, level, deposit1, deposit2, deposit3 " +
											"from skills WHERE player = ?;");
			prep.setInt(1, id);
			rs = prep.executeQuery();
			
			while(rs.next()){
				int skillID = rs.getInt("id");
				int level = rs.getInt("level");
				Skill skill = player.getSkill(skillID);
				if (skill != null){
					skill.setLevel(level);
					skill.setDeposit1(rs.getInt("deposit1"));
					skill.setDeposit2(rs.getInt("deposit2"));
					skill.setDeposit3(rs.getInt("deposit3"));
				}
			}
			rs.close();
			prep.close();
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected GreeterMessage getGreeterMessage(String messageId) {
		System.out.println(messageId);
		return greeterMessageList.get(messageId);
	}

	public DwarfTrainer getTrainer(Entity entity) {
		for (Iterator<Map.Entry<String, DwarfTrainer>> i = trainerList.entrySet().iterator(); i.hasNext();) {
			Map.Entry<String, DwarfTrainer> pairs = i.next();
			DwarfTrainer trainer = (pairs.getValue());
			if (trainer.getEntity().getBukkitEntity().getEntityId() == entity.getEntityId())
				return trainer;
		}
		return null;
	}
	
	public boolean isTrainer(Entity entity){
		for(Iterator<Map.Entry<String, DwarfTrainer>> i = trainerList.entrySet().iterator(); i.hasNext();){
			Map.Entry<String, DwarfTrainer> pairs = i.next();
			DwarfTrainer trainer = (pairs.getValue());
			if(trainer.getEntity().getBukkitEntity().getEntityId() == entity.getEntityId())
				return true;
		}
		return false;
	}

	protected DwarfTrainer getTrainer(String str) {
		return (trainerList.get(str)); // can return null
	}

	public DwarfVehicle getVehicle(Vehicle v) {
		for (DwarfVehicle i : vehicleList) {
			if (i.equals(v)) {
				return i;
			}
		}
		return null;
	}

	protected void insertGreeterMessage(String messageId,
			GreeterMessage greeterMessage) {
		try {
			greeterMessageList.put(messageId, greeterMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void insertTrainer(DwarfTrainer trainer) {
		assert (trainer != null);
		trainerList.put(trainer.getUniqueId(), trainer);
		try {
			PreparedStatement prep = mDBCon.prepareStatement("insert into trainers values (?,?,?,?,?,?,?,?,?,?,?,?,?);");
			prep.setString(1, trainer.getWorld().getName());
			prep.setString(2, trainer.getUniqueId());
			prep.setString(3, trainer.getName());
			
			if (!trainer.isGreeter()) {
				prep.setInt(4, trainer.getSkillTrained());
				prep.setInt(5, trainer.getMaxSkill());
			} else {
				prep.setInt(4, 0);
				prep.setInt(5, 0);
			}
			
			prep.setInt    (6,  trainer.getMaterial());
			prep.setBoolean(7,  trainer.isGreeter());
			prep.setString (8,  trainer.getMessage());
			prep.setDouble (9,  trainer.getLocation().getX());
			prep.setDouble (10, trainer.getLocation().getY());
			prep.setDouble (11, trainer.getLocation().getZ());
			prep.setFloat  (12, trainer.getLocation().getYaw());
			prep.setFloat  (13, trainer.getLocation().getPitch());
			
			if (DwarfCraft.debugMessagesThreshold < 7)
				System.out.println(String.format("DC7: Added trainer %s in world: %s", trainer.getUniqueId(), trainer.getWorld().getName()));
			
			prep.execute();
			prep.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	public void updateTrainerLocation(DwarfTrainer trainer, float yaw, float pitch) {
		assert (trainer != null);
		trainerList.put(trainer.getUniqueId(), trainer);
		try {
			PreparedStatement prep = mDBCon.prepareStatement("UPDATE trainers SET yaw=? WHERE uniqueId=?;");
			prep.setFloat(1, yaw);
			prep.setString(2, trainer.getUniqueId());
			prep.execute();
			prep.close();
			PreparedStatement prep1 = mDBCon.prepareStatement("UPDATE trainers SET pitch=? WHERE uniqueId=?;");
			prep1.setFloat(1, pitch);
			prep1.setString(2, trainer.getUniqueId());
			prep1.execute();
			prep1.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	private HashMap<String, DwarfTrainer> populateTrainers(World world) {
		try {
			PreparedStatement prep = mDBCon.prepareStatement("SELECT * FROM trainers WHERE world = ?;");
			prep.setString(1, world.getName());
			ResultSet rs = prep.executeQuery();
			
			while (rs.next()) {
				if (world.getName().equals(rs.getString("world"))) {
					if (DwarfCraft.debugMessagesThreshold < 5)
						System.out.println("DC5: trainer: " + rs.getString("name") + " in world: " + world.getName());
					
					DwarfTrainer trainer = 
						new DwarfTrainer(plugin,
							new Location(world, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"), rs.getFloat("yaw"), rs.getFloat("pitch")),
							rs.getString("uniqueId"), 
							rs.getString("name"),
							rs.getInt("skill"), 
							rs.getInt("maxSkill"),
							rs.getString("messageId"),
							rs.getBoolean("isGreeter")
						);
					
					trainerList.put(rs.getString("uniqueId"), trainer);
				}
			}
			rs.close();
			prep.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return trainerList;
	}


	public void removeTrainer(String str) {
		DwarfTrainer trainer = trainerList.remove(str);

		plugin.getNPCManager().despawnById(trainer.getUniqueId());

		try {
			PreparedStatement prep = mDBCon.prepareStatement("DELETE FROM trainers WHERE uniqueId = ?;");
			prep.setString(1, trainer.getUniqueId());
			prep.execute();
			prep.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeVehicle(Vehicle v) {
		for (DwarfVehicle i : vehicleList) {
			if (i.equals(v)) {
				plugin.getDataManager().vehicleList.remove(i);
				if (DwarfCraft.debugMessagesThreshold < 5)
					System.out.println("DC5:Removed DwarfVehicle from vehicleList");
			}
		}
	}
	private int getPlayerID(String name) {
		try{		
			PreparedStatement prep = mDBCon.prepareStatement("select id from players WHERE name = ?;");
			prep.setString(1, name);
			ResultSet rs = prep.executeQuery();
		
			if (!rs.next())
				return -1;		
		
			int id = rs.getInt("id");
			rs.close();
			prep.close();
			return id;
		}catch(Exception e ){
			System.out.println("DC: Failed to get player ID: " + name);
		}
		return -1;
	}

	public boolean saveDwarfData(DCPlayer dCPlayer) {
		try {
			PreparedStatement prep = mDBCon.prepareStatement("UPDATE players SET race=? WHERE name=?;");
			prep.setString(1, dCPlayer.getRace().getName());
			prep.setString(2, dCPlayer.getPlayer().getName());
			prep.execute();
			prep.close();
			
			prep = mDBCon.prepareStatement("REPLACE INTO skills(player, id, level, " +
																"deposit1, deposit2, deposit3) " +
																"values(?,?,?,?,?,?);");
			
			int id = getPlayerID(dCPlayer.getPlayer().getName());
			for (Skill skill : dCPlayer.getSkills().values()){
				prep.setInt(1, id);
				prep.setInt(2, skill.getId());
				prep.setInt(3, skill.getLevel());
				prep.setInt(4, skill.getDeposit1());
				prep.setInt(5, skill.getDeposit2());
				prep.setInt(6, skill.getDeposit3());
				prep.addBatch();
			}
			prep.executeBatch();
			prep.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public List<Player> getTrainerRemove() {
		return trainerRemove;
	}
	
	public List<Player> getTrainerLookAt(){
		return trainerLookAt;
	}

}
