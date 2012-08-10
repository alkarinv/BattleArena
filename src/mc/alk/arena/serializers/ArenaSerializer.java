package mc.alk.arena.serializers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.MessageController;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.ArenaType;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.spawns.EntitySpawn;
import mc.alk.arena.objects.spawns.ItemSpawn;
import mc.alk.arena.objects.spawns.SpawnGroup;
import mc.alk.arena.objects.spawns.SpawnInstance;
import mc.alk.arena.objects.spawns.SpawnTime;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.SerializerUtil;
import mc.alk.arena.util.Util;
import mc.alk.arena.util.Util.MinMax;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ArenaSerializer {
	static BattleArenaController arenaController;
	static HashMap<JavaPlugin, List<ArenaSerializer>> configs = new HashMap<JavaPlugin, List<ArenaSerializer>>();
	YamlConfiguration config;
	File f = null;
	JavaPlugin plugin;

	public static void setBAC(BattleArenaController bac){
		arenaController = bac;
	}
	public ArenaSerializer(JavaPlugin plugin, String path){
		this.plugin = plugin;
		this.f = new File(path);
		if (!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		config = new YamlConfiguration();
		List<ArenaSerializer> paths = configs.get(plugin);
		if (paths == null){
			paths = new ArrayList<ArenaSerializer>();
			configs.put(plugin, paths);
		}
		paths.add(this);
		loadArenas();
	}

	public static void loadAllArenas(){
		for (Plugin plugin: configs.keySet()){
			for (ArenaSerializer serializer: configs.get(plugin)){
				serializer.loadArenas();
			}
		}
	}

	public void loadArenas(){
		try {config.load(f);} catch (Exception e){e.printStackTrace();}
		//		Log.info("Loading arenas from " + f.getAbsolutePath() +" using config "+ config.getName());
		loadArenas(BattleArena.getBAC(), config);
	}

	public static void loadArenas(BattleArenaController bac, ConfigurationSection cs){
		if (cs == null){
			Log.info("Configuration section is null");
			return;
		}
		ConfigurationSection as = cs.getConfigurationSection("arenas");
		if (as == null){
			Log.info("Arena section is empty in config cs=" + cs.getCurrentPath());
			return;
		}
		Set<String> keys = as.getKeys(false);
		StringBuilder loadedArenas = new StringBuilder(BattleArena.getPName()+"Successfully loaded arenas");
		StringBuilder failedArenas = new StringBuilder(BattleArena.getPName()+"Failed loading arenas");
		boolean hasFailed = false, hasAny = false;
		for (String name : keys){
			//			System.out.println("Loading arena " + name);
			if (loadArena(bac,cs.getConfigurationSection("arenas."+name))){
				hasAny = true;
				loadedArenas.append(","+name);
			} else{
				hasFailed = true;
				failedArenas.append(","+name);
			}
		}
		if (hasAny)
			Log.info(loadedArenas.toString());
		else 
			Log.info(BattleArena.getPName() + "No arenas found");
		if (hasFailed)
			Log.info(failedArenas.toString());
	}

	public static boolean loadArena(BattleArenaController bac, ConfigurationSection cs) {
		String name = cs.getName().toLowerCase();
		Integer minTeams = cs.contains("minTeams") ? cs.getInt("minTeams") : 2;
		Integer maxTeams = cs.contains("maxTeams") ? cs.getInt("maxTeams") : ArenaParams.MAX;
		Integer minTeamSize = cs.contains("minTeamSize") ? cs.getInt("minTeamSize") : 1;
		Integer maxTeamSize = cs.contains("maxTeamSize") ? cs.getInt("maxTeamSize") : ArenaParams.MAX;
		Integer pminTeamSize = cs.contains("preferredMinTeamSize") ? cs.getInt("preferredMinTeamSize") : minTeamSize;
		Integer pmaxTeamSize = cs.contains("preferredMaxTeamSize") ? cs.getInt("preferredMaxTeamSize") : maxTeamSize;
		if (cs.contains("teamSize")) {
			MinMax mm = Util.getMinMax(cs.getString("teamSize"));
			minTeamSize = mm.min;
			maxTeamSize = mm.max;
		}
		if (cs.contains("nTeams")) {
			MinMax mm = Util.getMinMax(cs.getString("nTeams"));
			minTeams = mm.min;
			maxTeams = mm.max;
		}
		if (cs.contains("preferredTeamSize")) {
			MinMax mm = Util.getMinMax(cs.getString("preferredTeamSize"));
			pminTeamSize = mm.min;
			pmaxTeamSize = mm.max;
		}

		if (minTeams == 0 || maxTeams == 0){
			Log.err( name + " Invalid range of team sizes " + minTeams + " " + maxTeams);
			return false;
		}

		ArenaType atype = ArenaType.fromString(cs.getString("type"));
		if (atype==null){
			Log.err( name + " Arena type not found");
			return false;
//			atype = ArenaType.DEFAULT;
		}

		ArenaParams q = new ArenaParams(minTeamSize,maxTeamSize,atype);
		q.setMinTeams(minTeams);
		q.setMaxTeams(maxTeams);
		q.setMinTeamSize(minTeamSize);
		q.setMaxTeamSize(maxTeamSize);
		q.setPreferredMinTeamSize(pminTeamSize);
		q.setPreferredMaxTeamSize(pmaxTeamSize);

		if (!q.valid()){
			Log.err( name + " This arena is not valid arenaq=" + q.toString());
			return false;
		}

		Arena arena = ArenaType.createArena(name, q);
		if (arena == null){
			Log.err("Couldnt load the Arena " + name);
			return false;
		}
		/// Spawns
		ConfigurationSection loccs = cs.getConfigurationSection("locations");
		Map<Integer,Location> locs = SerializerUtil.parseLocations(loccs);
		if (locs != null){
			for (Integer i: locs.keySet()){
				arena.setSpawnLoc(i, locs.get(i));}
		}
		
		/// Wait room spawns
		loccs = cs.getConfigurationSection("waitRoomLocations");
		locs = SerializerUtil.parseLocations(loccs);
		if (locs != null){
			for (Integer i: locs.keySet()){
				arena.setWaitRoomSpawnLoc(i, locs.get(i));}
		}
		
		/// Item/mob/group spawns
		ConfigurationSection spawncs = cs.getConfigurationSection("spawns");
		if (spawncs != null){
			for (String spawnStr : spawncs.getKeys(false)){
				ConfigurationSection scs = spawncs.getConfigurationSection(spawnStr);
				TimedSpawn s = parseSpawnable(scs);
				if (s == null)
					continue;
				arena.addTimedSpawn(s);
			}				
		}
		arena.setParameters(q);
		bac.addArena(arena);
		return true;
	}

	private void saveArenas(boolean log) {
		ArenaSerializer.saveArenas(BattleArena.getBAC().getArenas().values(), f, config, plugin,log);
		try {
			config.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void saveArenas(Collection<Arena> arenas, File f, ConfigurationSection config, Plugin plugin, boolean log){
		ConfigurationSection maincs = config.createSection("arenas");
		HashMap<String, Object> map = new HashMap<String, Object>();
		for (Arena arena : arenas){
			ArenaType at = arena.getArenaType();
			//			System.out.println("plugin = "+ plugin +"  atplugin=" + at.getPlugin() +"    at=" + at);
			if (at.getPlugin().getName() != plugin.getName())
				continue;
			String arenaname = arena.getName();

			HashMap<String, Object> amap = new HashMap<String, Object>();
			if (!arena.valid()){
				Log.err(MessageController.decolorChat("Unfinished arena not being saved  name=" + arena.getName() + " details=" + arena));
				continue;
			}

			//			amap.put("name", arena.getName().toLowerCase());
			amap.put("type", arena.getArenaType().getName());
			amap.put("teamSize", arena.getParameters().getTeamSizeRange());
			amap.put("nTeams", arena.getParameters().getNTeamRange());

			/// Spawn locations
			Map<Integer, Location> mlocs = arena.getSpawnLocs();
			if (mlocs != null){
				HashMap<String,String> locations = SerializerUtil.createSaveableLocations(mlocs);
				amap.put("locations", locations);		
			}

			/// Wait room spawns
			mlocs = arena.getWaitRoomSpawnLocs();
			if (mlocs!= null){
				HashMap<String,String> locations = SerializerUtil.createSaveableLocations(mlocs);
				amap.put("waitRoomLocations", locations);
			}

			/// Timed spawns
			Map<Long, TimedSpawn> timedSpawns = arena.getTimedSpawns();
			Integer i = 0;
			if (timedSpawns != null && !timedSpawns.isEmpty()){
				HashMap<String,Object> spawnmap = new HashMap<String,Object>();				
				for (TimedSpawn ts: timedSpawns.values() ){
					//					System.out.println("Saving ts =" + ts);
					HashMap<String,Object> itemSpawnMap = saveSpawnable(i++, ts);
					spawnmap.put(i.toString(), itemSpawnMap);
				}
				amap.put("spawns", spawnmap);		
			}

			Location vloc = arena.getVisitorLoc();
			if (vloc != null)
				amap.put("vloc",SerializerUtil.getLocString(vloc));

			map.put(arenaname, amap);
			if (log)
				Log.info(plugin.getName() + " Saving arena " + arenaname +" type=" + at.getName() +" to " + 
						f.getPath() + " configSection="+config.getCurrentPath()+"." + config.getName());
		}
		SerializerUtil.expandMapIntoConfig(maincs, map);
	}
	
	protected void saveArenas() {
		saveArenas(false);
	}
	public void save() {
		this.saveArenas(true);
	}

	public static void saveAllArenas(boolean log){
		for (Plugin plugin: configs.keySet()){
			for (ArenaSerializer serializer: configs.get(plugin)){
				serializer.saveArenas(log);
			}
		}
	}

	private static TimedSpawn parseSpawnable(ConfigurationSection cs) {
		if (!cs.contains("spawn") || !cs.contains("time") || !cs.contains("loc")){
			Log.err("configuration section cs = " + cs +"  is missing either spawn,time,or loc");
			return null;
		}
		SpawnTime st = parseSpawnTime(cs.getString("time"));
		Location loc = SerializerUtil.getLocation(cs.getString("loc"));
		List<SpawnInstance> spawns = SpawnSerializer.parseSpawnable(SpawnSerializer.convertToStringList(cs.getString("spawn")));
		//		System.out.println("Parsing spawn " + st + " loc=" + loc +"  spawn = " + spawns.get(0));
		spawns.get(0).setLocation(loc);
		TimedSpawn ts = new TimedSpawn(st.i1,st.i2, st.i3,spawns.get(0));
		return ts;
	}

	public static SpawnTime parseSpawnTime(String str){
		String strs[] = str.split(" ");
		Integer is[] = new Integer[strs.length];
		for (int i=0;i<strs.length;i++){
			is[i] = Integer.valueOf(strs[i]);
		}
		SpawnTime st = new SpawnTime(is[0],is[1],is[2]);
		return st;
	}
	private static HashMap<String, Object> saveSpawnable(Integer i, TimedSpawn ts) {
		HashMap<String, Object> spawnmap = new HashMap<String,Object>();
		SpawnInstance si = ts.getSpawn();
		String key = null;
		String value =null;
		if (si instanceof SpawnGroup){
			SpawnGroup in = (SpawnGroup) si;
			key = in.getName();
			value =  "1";
		} else if (si instanceof ItemSpawn){
			ItemSpawn in = (ItemSpawn) si;
			key = InventoryUtil.getItemString(in.getItemStack());

		} else if (si instanceof EntitySpawn){
			EntitySpawn in = (EntitySpawn) si;
			key = in.getEntityString();
			value = in.getNumber() +"";
		} else {

		}
		if (value == null)
			spawnmap.put("spawn", key);
		else 
			spawnmap.put("spawn", key+":" + value);
		//		for (String k: spawnmap.keySet()){
		//			System.out.println("k="+k +"  vlaue=" + key+":"+value);
		//		}
		spawnmap.put("loc", SerializerUtil.getLocString(si.getLocation()));
		spawnmap.put("time", ts.getTimeToStart()+" " + ts.getRespawnInterval()+" " + ts.getTimeToDespawn());
		return spawnmap;
	}


}
