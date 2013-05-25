package mc.alk.arena.serializers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.ArenaClassController;
import mc.alk.arena.controllers.ModuleController;
import mc.alk.arena.controllers.OptionSetController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.ArenaSize;
import mc.alk.arena.objects.CommandLineString;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.JoinType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.Rating;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.exceptions.ConfigException;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.messaging.AnnouncementOptions;
import mc.alk.arena.objects.modules.ArenaModule;
import mc.alk.arena.objects.modules.BrokenArenaModule;
import mc.alk.arena.objects.options.TransitionOption;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.victoryconditions.OneTeamLeft;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.util.BTInterface;
import mc.alk.arena.util.DisguiseInterface;
import mc.alk.arena.util.EffectUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MinMax;
import mc.alk.arena.util.SerializerUtil;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

/**
 *
 * @author alkarin
 *
 */
public class ConfigSerializer extends BaseConfig{
	final Plugin plugin;
	final String name;

	public ConfigSerializer(Plugin plugin, File configFile, String name) {
		this.setConfig(configFile);
		this.name = name;
		this.plugin = plugin;
	}

	public MatchParams loadMatchParams() throws ConfigException, InvalidOptionException {
		return ConfigSerializer.loadMatchParams(plugin, this, name);
	}

	public static MatchParams loadMatchParams(Plugin plugin, BaseConfig config, String name)
			throws ConfigException, InvalidOptionException {
		ConfigurationSection cs = config.getConfig();
		if (config.getConfigurationSection(name) != null){
			cs = config.getConfigurationSection(name);}

		/// Set up match options.. specifying defaults where not specified
		/// Set Arena Type
		ArenaType at = getArenaType(plugin, cs);
		if (at == null && !name.equalsIgnoreCase("tourney"))
			throw new ConfigException("Could not parse arena type. Valid types. " + ArenaType.getValidList());


		JoinType jt = getJoinType(cs); /// how is this game joined
		MatchParams mp = jt == JoinType.QUEUE ? new MatchParams(at) : new EventParams(at);

		mp.setVictoryType(loadVictoryType(cs)); /// How does one win this game

		/// Set our name and command
		mp.setName(name);
		mp.setCommand(cs.getString("command",name));
		if (cs.contains("cmd")){ /// turns out I used cmd in a lot of old configs.. so use both :(
			mp.setCommand(cs.getString("cmd"));}

		loadGameSize(cs, mp); /// Set the game size

		ArenaType.addAliasForType(name, mp.getCommand());
		mp.setPrefix( cs.getString("prefix","&6["+name+"]"));

		loadTimes(cs, mp); /// Set the game times
		mp.setNLives(parseSize(cs.getString("nLives"),1)); /// Number of lives
		loadBTOptions(cs, mp); /// Load battle tracker options

		/// number of concurrently running matches of this type
		mp.setNConcurrentCompetitions(ArenaSize.toInt(cs.getString("nConcurrentCompetitions","infinite")));

		loadAnnouncementsOptions(cs, mp); /// Load announcement options

		List<String> modules = loadModules(cs, mp); /// load modules

		loadTransitionOptions(cs, mp); /// load transition options

		mp.setParent(ParamController.getDefaultConfig());
		ParamController.removeMatchType(mp);
		ParamController.addMatchType(mp);

		/// Load our PlayerContainers
		PlayerContainerSerializer pcs = new PlayerContainerSerializer();
		pcs.setConfig(BattleArena.getSelf().getDataFolder()+"/saves/containers.yml");
		pcs.load(mp);

		String mods = modules.isEmpty() ? "" : " mods=" + StringUtils.join(modules,", ");
		Log.info("["+plugin.getName()+"] Loaded "+mp.getName()+" params" +mods);
		return mp;
	}


	public static VictoryType loadVictoryType(ConfigurationSection cs) throws ConfigException {
		VictoryType vt;
		if (cs.contains("victoryCondition")){
			vt = VictoryType.fromString(cs.getString("victoryCondition"));
		} else {
			vt = VictoryType.getType(OneTeamLeft.class);
		}

		// TODO make unknown types with a valid plugin name be deferred until after the other plugin is loaded
		if (vt == null){
			throw new ConfigException("Could not add the victoryCondition " +cs.getString("victoryCondition") +"\n"
					+"valid types are " + VictoryType.getValidList());}
		return vt;
	}


	private static void loadTransitionOptions(ConfigurationSection cs, MatchParams mp) throws InvalidOptionException {
		MatchTransitions allTops = new MatchTransitions();

		/// Set all Transition Options
		for (MatchState transition : MatchState.values()){
			/// OnCancel gets taken from onComplete and modified
			if (transition == MatchState.ONCANCEL)
				continue;
			TransitionOptions tops = null;
			try{
				tops = getTransitionOptions(cs.getConfigurationSection(transition.toString()));
			} catch (Exception e){
				Log.err("Invalid Option was not added!!! transition= " + transition);
				e.printStackTrace();
				continue;
			}
			if (tops == null){
				allTops.removeTransitionOptions(transition);
				continue;}
			if (Defaults.DEBUG_TRACE) System.out.println("[ARENA] transition= " + transition +" "+tops);
			switch (transition){
			case ONCOMPLETE:
				TransitionOptions cancelOps = new TransitionOptions(tops);
				allTops.addTransitionOptions(MatchState.ONCANCEL, cancelOps);
				if (Defaults.DEBUG_TRACE) System.out.println("[ARENA] transition= " + MatchState.ONCANCEL +" "+cancelOps);
				break;
			case ONLEAVE:
				if (tops.hasOption(TransitionOption.TELEPORTOUT)){
					tops.removeOption(TransitionOption.TELEPORTOUT);
					Log.warn("You should never use the option teleportOut inside of onLeave!");
				}
				break;
			case DEFAULTS:
				if (cs.getBoolean("duelOnly", false)){ /// for backwards compatibility
					tops.addOption(TransitionOption.DUELONLY);}
			default:
				break;
			}
			allTops.addTransitionOptions(transition,tops);
		}
		if (allTops.hasOptionAt(MatchState.DEFAULTS, TransitionOption.ALWAYSOPEN))
			allTops.addTransitionOption(MatchState.ONJOIN, TransitionOption.ALWAYSJOIN);
		ParamController.setTransitionOptions(mp, allTops);
		/// By Default if they respawn in the arena.. people must want infinite lives
		if (mp.getTransitionOptions().hasOptionAt(MatchState.ONSPAWN, TransitionOption.RESPAWN) && !cs.contains("nLives")){
			mp.setNLives(Integer.MAX_VALUE);
		}
		/// start auto setting this option, as really thats what they want
		if (mp.getNLives() > 1){
			allTops.addTransitionOption(MatchState.ONDEATH, TransitionOption.RESPAWN);}
	}


	private static void loadAnnouncementsOptions(ConfigurationSection cs, MatchParams mp) {
		if (cs.contains("announcements")){
			AnnouncementOptions an = new AnnouncementOptions();
			BAConfigSerializer.parseAnnouncementOptions(an,true,cs.getConfigurationSection("announcements"), false);
			BAConfigSerializer.parseAnnouncementOptions(an,false,cs.getConfigurationSection("eventAnnouncements"),false);
			mp.setAnnouncementOptions(an);
		}
	}


	private static List<String> loadModules(ConfigurationSection cs, MatchParams mp) {
		List<String> modules = new ArrayList<String>();

		if (cs.contains("modules")){
			List<?> keys = cs.getList("modules");
			if (keys != null){
				for (Object key: keys){
					ArenaModule am = ModuleController.getModule(key.toString());
					if (am == null){
						Log.err("Module " + key +" not found!");
						mp.addModule(new BrokenArenaModule(key.toString()));
					} else {
						mp.addModule(am);
						modules.add(am.getName());
					}
				}
			}
		}
		return modules;
	}


	private static void loadBTOptions(ConfigurationSection cs, MatchParams mp) throws ConfigException {
		if (cs.contains("tracking")){
			cs = cs.getConfigurationSection("tracking");}

		/// TeamJoinResult in tracking for this match type
		String dbName = (cs.contains("database")) ? cs.getString("database",null) : cs.getString("db",null);
		if (dbName != null){
			mp.setDBName(dbName);
			if (!BTInterface.addBTI(mp)){
				mp.setDBName(null);}
		}
		if (cs.contains("overrideBattleTracker")){
			mp.setUseTrackerPvP(cs.getBoolean("overrideBattleTracker", true));
		} else {
			mp.setUseTrackerPvP(cs.getBoolean("useTrackerPvP", false));
		}
		mp.setUseTrackerMessages(cs.getBoolean("useTrackerMessages", false));
//		mp.set
//		mp.setOverrideBTMessages(cs.getBoolean(path))
		/// What is the default rating for this match type
		Rating rating = cs.contains("rated") ? Rating.fromBoolean(cs.getBoolean("rated")) : Rating.ANY;
		if (rating == null || rating == Rating.UNKNOWN)
			throw new ConfigException("Could not parse rating: valid types. " + Rating.getValidList());
		mp.setRating(rating);
	}


	private static void loadGameSize(ConfigurationSection cs, MatchParams mp) {
		if (cs.contains("gameSize")){
			cs = cs.getConfigurationSection("gameSize");}

		/// Number of teams and team sizes
		Integer minTeams = cs.getInt("minTeams",2);
		Integer maxTeams = cs.getInt("maxTeams",ArenaParams.MAX);
		Integer minTeamSize = cs.getInt("minTeamSize",1);
		Integer maxTeamSize = cs.getInt("maxTeamSize", ArenaParams.MAX);
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
		mp.setMinTeams(minTeams);
		mp.setMaxTeams(maxTeams);
		mp.setMinTeamSize(minTeamSize);
		mp.setMaxTeamSize(maxTeamSize);
	}


	private static void loadTimes(ConfigurationSection cs, MatchParams mp) {
		if (cs.contains("times")){
			cs = cs.getConfigurationSection("times");}
		if (cs.contains("timeBetweenRounds"))
			mp.setTimeBetweenRounds(cs.getInt("timeBetweenRounds",Defaults.TIME_BETWEEN_ROUNDS));
		if (cs.contains("secondsToLoot"))
			mp.setSecondsToLoot( cs.getInt("secondsToLoot", Defaults.SECONDS_TO_LOOT));
		if (cs.contains("secondsTillMatch"))
			mp.setSecondsTillMatch( cs.getInt("secondsTillMatch",Defaults.SECONDS_TILL_MATCH));

		if (cs.contains("matchTime"))
			mp.setMatchTime(parseSize(cs.getString("matchTime"),Defaults.MATCH_TIME));
		if (cs.contains("matchUpdateInterval"))
			mp.setIntervalTime(cs.getInt("matchUpdateInterval",Defaults.MATCH_UPDATE_INTERVAL));
	}

	public static int parseSize(String value, int defValue) {
		if (value == null)
			return defValue;
		if (value.equalsIgnoreCase("infinite")){
			return Integer.MAX_VALUE;
		} else {
			int lives = Integer.valueOf(value);
			return lives <= 0 ? Integer.MAX_VALUE : lives;
		}
	}

	/**
	 * Get and create the ArenaType for this plugin given the Configuration section
	 * @param plugin
	 * @param cs
	 * @return
	 * @throws ConfigException
	 */
	public static ArenaType getArenaType(Plugin plugin, ConfigurationSection cs) throws ConfigException {
		ArenaType at;
		if (cs.contains("arenaType") || cs.contains("type")){
			String type = cs.contains("type") ? cs.getString("type") : cs.getString("arenaType");
			at = ArenaType.fromString(type);
			if (at == null && type != null && !type.isEmpty()){ /// User is trying to make a custom type... let them
				Class<? extends Arena> arenaClass = ArenaType.getArenaClass(cs.getString("arenaClass","Arena"));
				at = ArenaType.register(type, arenaClass, plugin);
			}
			if (at == null)
				throw new ConfigException("Could not parse arena type. Valid types. " + ArenaType.getValidList());
		} else {
			at = ArenaType.fromString(cs.getName()); /// Get it from the configuration section name
		}
		if (at == null){
			at = ArenaType.register(cs.getName(), Arena.class, plugin);
		}
		return at;
	}

	public static ArenaType getArenaGameType(Plugin plugin, ConfigurationSection cs) throws ConfigException {
		ArenaType at = null;
		if (cs.contains("gameType")){
			String s = cs.getString("gameType");
			at = ArenaType.fromString(s);
			if (at == null){
				at = getArenaType(plugin,cs);}
		}
		return at;
	}

	public static TransitionOptions getTransitionOptions(ConfigurationSection cs) throws InvalidOptionException, IllegalArgumentException {
		if (cs == null)
			return null;
		Set<Object> optionsstr = new HashSet<Object>(cs.getList("options"));
		Map<TransitionOption,Object> options = new EnumMap<TransitionOption,Object>(TransitionOption.class);
		TransitionOptions tops = new TransitionOptions();
		for (Object obj : optionsstr){
			String[] split = obj.toString().split("=");
			/// Our key for this option
			final String key = split[0].trim().toUpperCase();
			final String value = split.length > 1 ? split[1].trim() : null;
			Object ovalue = null;

			/// Check first to see if this option is actually a set of options
			TransitionOptions optionSet = OptionSetController.getOptionSet(key);
			if (optionSet != null){
				tops.addOptions(optionSet);
				continue;
			}

			TransitionOption to = null;
			try{
				to = TransitionOption.fromString(key);
				if (to.hasValue() && value == null){
					Log.err("Transition Option " + to +" needs a value! " + key+"=<value>");
					continue;
				}
				ovalue = to.parseValue(value);
			} catch (Exception e){
				Log.err("Couldn't parse Option " + key +" value="+value);
				continue;
			}

			options.put(to,ovalue);

		}
		tops.addOptions(options);

		try{
			if (cs.contains("teleportTo")){
				tops.addOption(TransitionOption.TELEPORTTO, SerializerUtil.getLocation(cs.getString("teleportTo")));}
		} catch (Exception e){
			Log.err("Error setting the value of teleportTo ");
			e.printStackTrace();
		}
		try{
			if (cs.contains("giveClass")){
				tops.addOption(TransitionOption.GIVECLASS, getArenaClasses(cs.getConfigurationSection("giveClass")));}
		} catch (Exception e){
			Log.err("Error setting the value of giveClass ");
			e.printStackTrace();
		}
		try{
			if (cs.contains("giveDisguise")){
				tops.addOption(TransitionOption.GIVEDISGUISE, getArenaDisguises(cs.getConfigurationSection("giveDisguise")));}
		} catch (Exception e){
			Log.err("Error setting the value of giveDisguise ");
			e.printStackTrace();
		}
		try{
			if (cs.contains("doCommands")){
				tops.addOption(TransitionOption.DOCOMMANDS, getDoCommands(cs.getStringList("doCommands")));}
		} catch (Exception e){
			Log.err("Error setting the value of doCommands ");
			e.printStackTrace();
		}
		try{
			/// Convert from old to new style aka ("needItems" and items: list, to needItems:)
			List<ItemStack> items = getItemList(cs,"needItems");
			if (items == null && options.containsKey(TransitionOption.NEEDITEMS)){
				items = getItemList(cs, "items");
				if (items!=null && !items.isEmpty())
					tops.addOption(TransitionOption.NEEDITEMS,items);
				else
					options.remove(TransitionOption.NEEDITEMS);
			} else if (items != null && !items.isEmpty()){
				tops.addOption(TransitionOption.NEEDITEMS,items);
			} else {
				options.remove(TransitionOption.NEEDITEMS);
			}
		} catch (Exception e){
			Log.err("Error setting the value of needItems ");
			e.printStackTrace();
		}
		try{
			List<ItemStack> items = getItemList(cs,"giveItems");
			if (items == null)
				items = getItemList(cs,"items");

			if (items!=null && !items.isEmpty()) {
				tops.addOption(TransitionOption.GIVEITEMS,items);
			} else {
				options.remove(TransitionOption.GIVEITEMS);}
		} catch (Exception e){
			Log.err("Error setting the value of giveItems ");
			e.printStackTrace();
		}

		try{
			List<PotionEffect> effects = getEffectList(cs, "enchants");
			if (effects!=null && !effects.isEmpty())
				tops.addOption(TransitionOption.ENCHANTS, effects);
			else
				options.remove(TransitionOption.ENCHANTS);
		} catch (Exception e){
			Log.err("Error setting the value of enchants ");
			e.printStackTrace();
		}

		setPermissionSection(cs,"addPerms",tops);

		return tops;
	}

	public static List<CommandLineString> getDoCommands(List<String> list) throws InvalidOptionException {
		List<CommandLineString> commands = new ArrayList<CommandLineString>();
		for (String line: list){
			CommandLineString cls = CommandLineString.parse(line);
			commands.add(cls);
		}
		return commands;
	}

	private static void setPermissionSection(ConfigurationSection cs, String nodeString, TransitionOptions tops) throws InvalidOptionException {
		if (cs == null || !cs.contains(nodeString))
			return ;
		List<?> olist = cs.getList(nodeString);
		List<String> permlist = new ArrayList<String>();

		for (Object perm: olist){
			permlist.add(perm.toString());}
		if (permlist.isEmpty())
			return;
		tops.addOption(TransitionOption.ADDPERMS, permlist);
	}

	public static HashMap<Integer,ArenaClass> getArenaClasses(ConfigurationSection cs){
		HashMap<Integer,ArenaClass> classes = new HashMap<Integer,ArenaClass>();
		Set<String> keys = cs.getKeys(false);
		for (String whichTeam: keys){
			int team = -1;
			final String className = cs.getString(whichTeam);
			ArenaClass ac = ArenaClassController.getClass(className);
			if (whichTeam.equalsIgnoreCase("default")){
				team = ArenaClass.DEFAULT;
			} else {
				try {
					team = Integer.valueOf(whichTeam.replaceAll("team", "")) - 1;
				} catch(Exception e){
					Log.err("Couldnt find which team this class belongs to '" + whichTeam+"'");
					continue;
				}
			}
			if (team ==-1){
				Log.err("Couldnt find which team this class belongs to '" + whichTeam+"'");
				continue;
			}
			if (ac == null){
				Log.err("Couldnt find arenaClass " + className);
				ac = new ArenaClass(className);
			}
			classes.put(team, ac);
		}
		return classes;
	}

	public static HashMap<Integer,String> getArenaDisguises(ConfigurationSection cs){
		HashMap<Integer,String> disguises = new HashMap<Integer,String>();
		Set<String> keys = cs.getKeys(false);
		for (String whichTeam: keys){
			int team = -1;
			final String disguiseName = cs.getString(whichTeam);
			if (whichTeam.equalsIgnoreCase("default")){
				team = DisguiseInterface.DEFAULT;
			} else {
				try {
					team = Integer.valueOf(whichTeam.replaceAll("team", "")) - 1;
				} catch(Exception e){
					Log.err("Couldnt find which team this disguise belongs to '" + whichTeam+"'");
					continue;
				}
			}
			if (team ==-1){
				Log.err("Couldnt find which team this disguise belongs to '" + whichTeam+"'");
				continue;
			}
			disguises.put(team, disguiseName);
		}
		return disguises;
	}

	public static List<PotionEffect> getEffectList(ConfigurationSection cs, String nodeString) {
		if (cs == null || cs.getList(nodeString) == null)
			return null;
		final int strengthDefault = 1;
		final int timeDefault = 60;
		ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();
		try {
			String str = null;
			for (Object o : cs.getList(nodeString)){
				str = o.toString();
				try{
					PotionEffect ewa = EffectUtil.parseArg(str,strengthDefault,timeDefault);
					effects.add(ewa);
				} catch (Exception e){
					Log.err("Effect "+cs.getCurrentPath() +"."+nodeString +"."+str+ " could not be parsed in classes.yml. " + e.getMessage());

				}
			}
		} catch (Exception e){
			Log.err("Effect "+cs.getCurrentPath() +"."+nodeString + " could not be parsed in classes.yml");
		}
		return effects;
	}

	public static ArrayList<ItemStack> getItemList(ConfigurationSection cs, String nodeString) {
		if (cs == null || cs.getList(nodeString) == null)
			return null;
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		try {
			String str = null;
			for (Object o : cs.getList(nodeString)){
				try {
					str = o.toString();
					ItemStack is = InventoryUtil.parseItem(str);
					if (is != null){
						items.add(is);
					} else {
						Log.err(cs.getCurrentPath() +"."+nodeString + " couldnt parse item " + str);
					}
				} catch (Exception e){
					Log.err(cs.getCurrentPath() +"."+nodeString + " couldnt parse item " + str);
					e.printStackTrace();
				}
			}
		} catch (Exception e){
			Log.err(cs.getCurrentPath() +"."+nodeString + " could not be parsed in config.yml");
			e.printStackTrace();
		}
		return items;
	}

	public static JoinType getJoinType(ConfigurationSection cs) {
		if (cs.getName().equalsIgnoreCase("Tourney"))
			return JoinType.JOINPHASE;
		boolean isMatch = !cs.getBoolean("isEvent",false);
		isMatch = cs.getBoolean("queue",isMatch);
		if (cs.contains("joinType")){
			String type = cs.getString("joinType");
			try{
				return JoinType.fromString(type);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		return isMatch ? JoinType.QUEUE : JoinType.JOINPHASE;
	}

	public void save(MatchParams params){
		ConfigurationSection main = config.createSection(params.getName());
		ArenaParams parent = params.getParent();
		params.setParent(null); /// set the parent to null so we aren't grabbing options from the parent

		main.set("enabled", true);
		//		ConfigurationSection main = config;
		//		main.set("enabled", BAExecutor.);
		main.set("name", params.getName());
		main.set("command", params.getCommand());
		main.set("prefix", params.getPrefix());

		ConfigurationSection cs = main.createSection("gameSize");
		cs.set("nTeams", params.getNTeamRange());
		cs.set("teamSize", params.getTeamSizeRange());

		main.set("nLives", ArenaSize.toString(params.getNLives()));
		main.set("joinType", params.getJoinType().toString());
		main.set("victoryCondition", params.getVictoryType().getName());

		cs = main.createSection("times");
		cs.set("secondsTillMatch", params.getSecondsTillMatch());
		cs.set("matchTime", params.getMatchTime());
		cs.set("secondsToLoot", params.getSecondsToLoot());

		cs.set("timeBetweenRounds", params.getTimeBetweenRounds());
		cs.set("matchUpdateInterval", params.getIntervalTime());

		cs = main.createSection("tracking");
		cs.set("db", params.getDBName());
		cs.set("rated", params.isRated());
		cs.set("overrideBTMessages", params.getUseTrackerPvP());

		main.set("arenaType", params.getType().getName());
		try{
			main.set("arenaClass", ArenaType.getArenaClass(params.getType()).getSimpleName());
		} catch(Exception e){
			main.set("arenaClass", params.getType().getClass().getSimpleName());
		}

		main.set("nConcurrentCompetitions", ArenaSize.toString(params.getNConcurrentCompetitions()));

		Collection<ArenaModule> modules = params.getModules();
		if (modules != null && !modules.isEmpty()){
			main.set("modules", getModuleList(modules));
		}
		/// TODO Come back and add custom AnnouncementOption support, for now leave it strictly in config.yml
		//		AnnouncementOptions ao = params.getAnnouncementOptions();
		//		if (ao != null){
		//			ao.
		//		}
		//		Map<String,Object> map = new LinkedHashMap<String,Object>();
		MatchTransitions alltops = params.getTransitionOptions();
		Map<MatchState,TransitionOptions> transitions =
				new TreeMap<MatchState,TransitionOptions>(alltops.getAllOptions());
		for (MatchState ms: transitions.keySet()){
			try{
				if (ms == MatchState.ONCANCEL)
					continue;
				TransitionOptions tops = transitions.get(ms);
				if (tops == null)
					continue;
				Map<TransitionOption,Object> ops = tops.getOptions();
				if (ops == null || ops.isEmpty())
					continue;
				/// transition map
				Map<String,Object> tmap = new LinkedHashMap<String,Object>();
				List<String> list = new ArrayList<String>();
				ops = new TreeMap<TransitionOption,Object>(ops); /// try to maintain some ordering
				for (TransitionOption to: ops.keySet()){
					try{
						String s;
						switch(to){
						case NEEDITEMS:
							tmap.put(to.toString(), getItems(tops.getNeedItems()));
							continue;
						case GIVEITEMS:
							tmap.put(to.toString(), getItems(tops.getGiveItems()));
							continue;
						case GIVECLASS:
							tmap.put(to.toString(), getArenaClasses(tops.getClasses()));
							continue;
						case ENCHANTS:
							tmap.put(to.toString(), getEnchants(tops.getEffects()));
							continue;
						case DOCOMMANDS:
							tmap.put(to.toString(), getDoCommandsStringList(tops.getDoCommands()));
							continue;
						case TELEPORTTO:
							tmap.put(to.toString(), SerializerUtil.getLocString(tops.getTeleportToLoc()));
							continue;
						default:
							break;
						}
						Object value = ops.get(to);
						if (value == null){
							s = to.toString();
						} else {
							s = to.toString() + "="+value.toString();
						}
						list.add(s);
					} catch (Exception e){
						Log.err("[BA Error] couldn't save " + to);
						e.printStackTrace();
					}
				}
				list = getOptionSets(list);
				tmap.put("options", list);
				//			main.put(ms.toString(), tmap);
				main.set(ms.toString(), tmap);
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		//		main.set("options", map);
		super.save();
		params.setParent(parent); ///reset the parent
	}

	private List<String> getOptionSets(List<String> list) {
		HashSet<String> set = new HashSet<String>(list);

		//		for (Map<String,TransitionOptions> ops = OptionSetController.getOptionSet(key)){
		//
		//		}
		return list;
	}

	private List<String> getModuleList(Collection<ArenaModule> modules) {
		List<String> list = new ArrayList<String>();
		if (modules != null){
			for (ArenaModule m: modules){
				list.add(m.getName());}
		}
		return list;
	}

	private List<String> getEnchants(List<PotionEffect> effects) {
		List<String> list = new ArrayList<String>();
		if (effects != null){
			for (PotionEffect is: effects){
				list.add(EffectUtil.getEnchantString(is));}
		}
		return list;
	}

	private List<String> getItems(List<ItemStack> items) {
		List<String> list = new ArrayList<String>();
		if (items != null){
			for (ItemStack is: items){
				list.add(InventoryUtil.getItemString(is));}
		}
		return list;
	}

	private Map<String,Object> getArenaClasses(Map<Integer, ArenaClass> classes) {
		HashMap<String,Object> map = new HashMap<String, Object>();
		for (Integer teamNumber: classes.keySet()){
			String teamName = teamNumber == ArenaClass.DEFAULT.intValue() ? "default" : "team" + teamNumber;
			map.put(teamName, classes.get(teamNumber).getName());
		}
		return map;
	}
	private List<String> getDoCommandsStringList(List<CommandLineString> doCommands) {
		List<String> list = new ArrayList<String>();
		if (doCommands != null){
			for (CommandLineString s: doCommands){
				list.add(s.getRawCommand());}
		}
		return list;
	}

}
