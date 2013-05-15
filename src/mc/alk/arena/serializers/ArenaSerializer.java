package mc.alk.arena.serializers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.WorldGuardController;
import mc.alk.arena.controllers.WorldGuardController.WorldGuardFlag;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaControllerInterface;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.arenas.Persistable;
import mc.alk.arena.objects.exceptions.RegionNotFound;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.spawns.EntitySpawn;
import mc.alk.arena.objects.spawns.ItemSpawn;
import mc.alk.arena.objects.spawns.SpawnGroup;
import mc.alk.arena.objects.spawns.SpawnInstance;
import mc.alk.arena.objects.spawns.SpawnTime;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MinMax;
import mc.alk.arena.util.SerializerUtil;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class ArenaSerializer extends BaseConfig{
	static BattleArenaController arenaController;
	static HashMap<Plugin, Set<ArenaSerializer>> configs = new HashMap<Plugin, Set<ArenaSerializer>>();

	/// Which plugin does this ArenaSerializer belong to
	Plugin plugin;

	public static void setBAC(BattleArenaController bac){
		arenaController = bac;
	}

	public ArenaSerializer(Plugin plugin, File file){
		setConfig(file);
		this.plugin = plugin;

		config = new YamlConfiguration();
		Set<ArenaSerializer> paths = configs.get(plugin);
		if (paths == null){
			paths = new HashSet<ArenaSerializer>();
			configs.put(plugin, paths);
		} else { /// check to see if we have this path already
			for (ArenaSerializer as : paths){
				if (as.file.getPath().equals(this.file.getPath())){
					return;}
			}
		}
		paths.add(this);
	}

	public static void loadAllArenas(){
		for (Plugin plugin: configs.keySet()){
			loadAllArenas(plugin);
		}
	}

	public static void loadAllArenas(Plugin plugin){
		for (ArenaSerializer serializer: configs.get(plugin)){
			serializer.loadArenas(plugin);
		}
	}

	public static void loadAllArenas(Plugin plugin, ArenaType arenaType){
		Set<ArenaSerializer> serializers = configs.get(plugin);
		if (serializers == null || serializers.isEmpty()){
			Log.err(plugin.getName() +" has no arenas to load");
			return;
		}

		for (ArenaSerializer serializer: serializers){
			serializer.loadArenas(plugin,arenaType);
		}
	}

	public void loadArenas(Plugin plugin){
		try {config.load(file);} catch (Exception e){e.printStackTrace();}
//		Log.info("["+plugin.getName()+ "] Loading arenas from " + file.getPath()+" using config "+ config.getName());
		loadArenas(plugin, BattleArena.getBAController(), config,null);
	}

	public void loadArenas(Plugin plugin, ArenaType arenaType){
		try {config.load(file);} catch (Exception e){e.printStackTrace();}
//		Log.info("["+plugin.getName()+ "] Loading arenas from " + file.getPath() +" using config "+ config.getName() +" at=" + arenaType);
		loadArenas(plugin, BattleArena.getBAController(), config, arenaType);
	}

	protected void loadArenas(Plugin plugin, BattleArenaController bac, //ArenaSerializer arenaSerializer,
			ConfigurationSection cs, ArenaType arenaType){
		final String pname = "["+plugin.getName()+"] ";
		if (cs == null){
			Log.info(pname+" has no arenas, cs is null");
			return;
		}

		ConfigurationSection as = cs.getConfigurationSection("arenas");
		ConfigurationSection bks = cs.getConfigurationSection("brokenArenas");
		if (as == null && bks == null){
			Log.info(pname+"has no arenas, cs section =" + cs.getCurrentPath());
			return;
		}
		List<String> keys = new ArrayList<String>(as.getKeys(false));
		int oldGoodSize = keys.size();
		Set<String> brokenKeys = bks == null ? new HashSet<String>() : bks.getKeys(false);
		int oldBrokenSize = brokenKeys.size();
		keys.addAll(brokenKeys);

		Set<String> brokenArenas = new HashSet<String>();
		Set<String> loadedArenas = new HashSet<String>();
		for (String name : keys){

			if (loadedArenas.contains(name) || brokenArenas.contains(name)) /// We already tried to load this arena
				continue;
			boolean broken = brokenKeys.contains(name);
			String section = broken ? "brokenArenas" : "arenas";
			if (arenaType != null){ /// Are we looking for 1 particular arena type to load
				String path = section+"."+name;
				String atype = cs.getString(path+".type",null);
				if (atype == null || !ArenaType.isSame(atype,arenaType)){
					/// Its not the same type.. so don't let it affect the sizes of the arena counts
					if (brokenArenas.remove(name)){
						oldBrokenSize--;
					} else{
						oldGoodSize--;
					}
					continue;
				}
			}
			boolean success = false;
			try{
				success = loadArena(bac,cs.getConfigurationSection(section+"."+name));
				if (success){
					loadedArenas.add(name);
					if (broken){
						transfer(cs,"brokenArenas."+name, "arenas."+name);}
				}
			} catch(IllegalArgumentException e){
				Log.err(e.getMessage());
			} catch(Exception e){
				e.printStackTrace();
			}
			if (!success){
				brokenArenas.add(name);
				if (!broken){
					transfer(cs,"arenas."+name, "brokenArenas."+name);}
			}
		}
//		Util.printStackTrace();
		if (!loadedArenas.isEmpty()) {
			Log.info(pname+"Loaded "+arenaType+" arenas: " + StringUtils.join(loadedArenas,", "));
		} else {
			Log.info(pname+"No arenas found for " + cs.getCurrentPath() +"  arenatype="+arenaType +"  cs="+cs.getName());
		}
		if (!brokenArenas.isEmpty()){
			Log.info(pname+"Failed loading arenas: " + StringUtils.join(brokenArenas, ", ") + " arenatype="+arenaType +" cs="+cs.getName());
		}
		if (oldGoodSize != loadedArenas.size() || oldBrokenSize != brokenArenas.size()){
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void transfer(ConfigurationSection cs, String string, String string2) {
		try{
			Map<String,Object> map = new HashMap<String,Object>(cs.getConfigurationSection(string).getValues(false));
			cs.createSection(string2, map);
			cs.set(string,null);
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	public static boolean loadArena(BattleArenaController bac, ConfigurationSection cs) {
		String name = cs.getName().toLowerCase();
		Integer minTeams = cs.contains("minTeams") ? cs.getInt("minTeams") : 2;
		Integer maxTeams = cs.contains("maxTeams") ? cs.getInt("maxTeams") : ArenaParams.MAX;
		Integer minTeamSize = cs.contains("minTeamSize") ? cs.getInt("minTeamSize") : 1;
		Integer maxTeamSize = cs.contains("maxTeamSize") ? cs.getInt("maxTeamSize") : ArenaParams.MAX;
		if (cs.contains("teamSize")) {
			MinMax mm = MinMax.valueOf(cs.getString("teamSize"));
			minTeamSize = mm.min;
			maxTeamSize = mm.max;
		}
		if (cs.contains("nTeams")) {
			MinMax mm = MinMax.valueOf(cs.getString("nTeams"));
			minTeams = mm.min;
			maxTeams = mm.max;
		}

		if (minTeams == 0 || maxTeams == 0){
			Log.err( name + " Invalid range of team sizes " + minTeams + " " + maxTeams);
			return false;
		}

		ArenaType atype = ArenaType.fromString(cs.getString("type"));
		if (atype==null){
			Log.err(" Arena type not found for " + name);
			return false;
		}
		ArenaParams q = new ArenaParams(atype);
		q.setMinTeams(minTeams);
		q.setMaxTeams(maxTeams);
		q.setMinTeamSize(minTeamSize);
		q.setMaxTeamSize(maxTeamSize);

		if (!q.valid()){
			Log.err( name + " This arena is not valid arenaq=" + q.toString());
			return false;
		}

		Arena arena = ArenaType.createArena(name, q,false);
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
				TimedSpawn s;
				try {
					s = parseSpawnable(scs);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					continue;
				}
				if (s == null)
					continue;
				arena.addTimedSpawn(Long.parseLong(spawnStr), s);
			}
		}
		cs = cs.getConfigurationSection("persistable");
		Persistable.yamlToObjects(arena, cs,Arena.class);
		arena.setParameters(q);
		updateRegions(arena);
		ArenaControllerInterface aci = new ArenaControllerInterface(arena);
		aci.init();
		bac.addArena(arena);
		return true;
	}

	private static void updateRegions(Arena arena) {
		if (!WorldGuardController.hasWorldGuard())
			return;
		if (!arena.hasRegion())
			return;
		if (!WorldGuardController.hasRegion(arena.getWorldGuardRegion())){
			Log.err("Arena " + arena.getName() +" has a world guard region defined but it no longer exists inside of WorldGuard."+
					"You will have to remake the region.  /arena alter <arena name> addregion");}
		MatchParams mp = ParamController.getMatchParamCopy(arena.getArenaType().getName());
		if (mp == null)
			return;
		MatchTransitions trans = mp.getTransitionOptions();
		if (trans == null)
			return;
		WorldGuardController.setFlag(arena.getWorldGuardRegion(), WorldGuardFlag.ENTRY, !trans.hasAnyOption(TransitionOption.WGNOENTER));
		try {
			WorldGuardController.trackRegion(arena.getWorldGuardRegion());
		} catch (RegionNotFound e) {
			e.printStackTrace();
		}
	}

	private void saveArenas(boolean log) {
		ArenaSerializer.saveArenas(BattleArena.getBAController().getArenas().values(), file, config, plugin,log);
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void saveArenas(Collection<Arena> arenas, File f, ConfigurationSection config, Plugin plugin, boolean log){
		HashMap<String, Object> map = new HashMap<String, Object>();
		for (Arena arena : arenas){
			String arenaname = null;
			try{
				arenaname = arena.getName();
				ArenaType at = arena.getArenaType();
				if (!at.getPlugin().getName().equals(plugin.getName()))
					continue;

				HashMap<String, Object> amap = new HashMap<String, Object>();
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
				if (timedSpawns != null && !timedSpawns.isEmpty()){
					HashMap<String,Object> spawnmap = new HashMap<String,Object>();
					for (Long key: timedSpawns.keySet() ){
						TimedSpawn ts = timedSpawns.get(key);
						HashMap<String,Object> itemSpawnMap = saveSpawnable(key, ts);
						spawnmap.put(key.toString(), itemSpawnMap);
					}
					amap.put("spawns", spawnmap);
				}

				Location vloc = arena.getVisitorLoc();
				if (vloc != null)
					amap.put("vloc",SerializerUtil.getLocString(vloc));

				Map<String,Object> persisted = Persistable.objectsToYamlMap(arena, Arena.class);
				if (persisted != null && !persisted.isEmpty()){
					amap.put("persistable", persisted);
				}

				map.put(arenaname, amap);
				config.set("brokenArenas."+arenaname, null); /// take out any duplicate names in broken arenas
			} catch (Exception e){
				e.printStackTrace();
				if (arenaname != null){
					transfer(config, "arenas."+arenaname, "brokenArenas."+arenaname);
				}
			}
		}
		if (log)
			Log.info(plugin.getName() + " Saving arenas " + StringUtils.join(map.keySet(),",") +" to " +
					f.getPath() + " configSection="+config.getCurrentPath()+"." + config.getName());
		ConfigurationSection maincs = config.createSection("arenas");
		SerializerUtil.expandMapIntoConfig(maincs, map);
	}

	protected void saveArenas() {
		saveArenas(false);
	}
	@Override
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

	public static void saveArenas(Plugin plugin){
		if (!configs.containsKey(plugin))
			return;
		for (ArenaSerializer serializer: configs.get(plugin)){
			serializer.saveArenas(false);
		}
	}

	private static TimedSpawn parseSpawnable(ConfigurationSection cs) throws IllegalArgumentException {
		if (!cs.contains("spawn") || !cs.contains("time") || !cs.contains("loc")){
			Log.err("configuration section cs = " + cs +"  is missing either spawn,time,or loc");
			return null;
		}
		SpawnTime st = parseSpawnTime(cs.getString("time"));
		Location loc = SerializerUtil.getLocation(cs.getString("loc"));
		List<String> strings = SpawnSerializer.convertToStringList(cs.getString("spawn"));
		if (strings == null || strings.isEmpty())
			return null;
		List<SpawnInstance> spawns = SpawnSerializer.parseSpawnable(strings);
		if (spawns == null || spawns.isEmpty())
			return null;

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

	private static HashMap<String, Object> saveSpawnable(Long i, TimedSpawn ts) {
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
			key = in.getEntityString() + " " + in.getNumber();
			//			value = in.getNumber() +"";
		} else {

		}
		if (value == null)
			spawnmap.put("spawn", key);
		else
			spawnmap.put("spawn", key+":" + value);
		spawnmap.put("loc", SerializerUtil.getLocString(si.getLocation()));
		spawnmap.put("time", ts.getFirstSpawnTime()+" " + ts.getRespawnTime()+" " + ts.getTimeToDespawn());
		return spawnmap;
	}


}
