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
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.ArenaClassController;
import mc.alk.arena.controllers.ModuleController;
import mc.alk.arena.controllers.OptionSetController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.StatController;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.ArenaSize;
import mc.alk.arena.objects.CommandLineString;
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
import mc.alk.arena.objects.messaging.AnnouncementOptions.AnnouncementOption;
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

		MatchParams mp = loadMatchParams(plugin, at, name, cs);
		return mp;
	}

	public static MatchParams loadMatchParams(Plugin plugin, ArenaType at, String name, ConfigurationSection cs)
			throws ConfigException, InvalidOptionException {
		return loadMatchParams(plugin,at,name,cs,false);
	}

	public static MatchParams loadMatchParams(Plugin plugin, ArenaType at, String name,
			ConfigurationSection cs, boolean isArena) throws ConfigException, InvalidOptionException {

		MatchParams mp = new MatchParams(at);
		if (!isArena || cs.contains("victoryCondition"))
			mp.setVictoryType(loadVictoryType(cs)); /// How does one win this game

		/// Set our name and command
		if (!isArena){
			mp.setName(name);
			mp.setCommand(cs.getString("command",name));
			if (cs.contains("cmd")){ /// turns out I used cmd in a lot of old configs.. so use both :(
				mp.setCommand(cs.getString("cmd"));}
		}
		loadGameSize(cs, mp, isArena); /// Set the game size

		if (!isArena)
			ArenaType.addAliasForType(name, mp.getCommand());
		if (!isArena || cs.contains("prefix"))
			mp.setPrefix( cs.getString("prefix","&6["+name+"]"));

		loadTimes(cs, mp); /// Set the game times
		if (!isArena || cs.contains("nLives"))
			mp.setNLives(parseSize(cs.getString("nLives"),1)); /// Number of lives
		loadBTOptions(cs, mp, isArena); /// Load battle tracker options

		/// number of concurrently running matches of this type
		if (cs.contains("nConcurrentCompetitions"))
			mp.setNConcurrentCompetitions(ArenaSize.toInt(cs.getString("nConcurrentCompetitions","infinite")));
		if (cs.contains("waitroomClosedWhileRunning"))
			mp.setWaitroomClosedWhileRunning(cs.getBoolean("waitroomClosedWhileRunning",true));
		if (cs.contains("cancelIfNotEnoughPlayers"))
			mp.setCancelIfNotEnoughPlayers(cs.getBoolean("cancelIfNotEnoughPlayers",false));
		if (cs.contains("arenaCooldown"))
			mp.setArenaCooldown(cs.getInt("arenaCooldown"));

		loadAnnouncementsOptions(cs, mp); /// Load announcement options

		List<String> modules = loadModules(cs, mp); /// load modules

		MatchTransitions tops = loadTransitionOptions(cs, mp, isArena); /// load transition options
		mp.setTransitionOptions(tops);

		mp.setParent(ParamController.getDefaultConfig());
		if (!isArena){
			ParamController.removeMatchType(mp);
			ParamController.addMatchParams(mp);
		}

		try{
			/// Load our PlayerContainers
			PlayerContainerSerializer pcs = new PlayerContainerSerializer();
			pcs.setConfig(BattleArena.getSelf().getDataFolder()+"/saves/containers.yml");
			pcs.load(mp);
		} catch (Exception e){}

		String mods = modules.isEmpty() ? "" : " mods=" + StringUtils.join(modules,", ");
		if (!isArena)
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


	private static MatchTransitions loadTransitionOptions(ConfigurationSection cs, MatchParams mp, boolean isArena)
			throws InvalidOptionException {
		MatchTransitions allTops = new MatchTransitions();

		/// Set all Transition Options
		for (MatchState transition : MatchState.values()){
			/// OnCancel gets taken from onComplete and modified
			if (transition == MatchState.ONCANCEL)
				continue;
			TransitionOptions tops = null;
			try{
				tops = getTransitionOptions(cs.getConfigurationSection(transition.toString()));
				/// check for the most common alternate spelling of onPrestart
				/// also check for the old version of winners (winner)
				if (tops == null && transition == MatchState.ONPRESTART){
					tops = getTransitionOptions(cs.getConfigurationSection("onPrestart"));}
				else if (tops == null && transition == MatchState.WINNERS){
					tops = getTransitionOptions(cs.getConfigurationSection("winner"));}
			} catch (Exception e){
				Log.err("Invalid Option was not added!!! transition= " + transition);
				Log.printStackTrace(e);
				continue;
			}
			if (tops == null){
				allTops.removeTransitionOptions(transition);
				continue;}
			if (Defaults.DEBUG_TRACE) Log.info("[ARENA] transition= " + transition +" "+tops);
			switch (transition){
			case ONCOMPLETE:
				if (allTops.hasOptionAt(MatchState.ONLEAVE, TransitionOption.CLEARINVENTORY)){
					tops.addOption(TransitionOption.CLEARINVENTORY);
				}
				TransitionOptions cancelOps = new TransitionOptions(tops);
				allTops.addTransitionOptions(MatchState.ONCANCEL, cancelOps);
				if (Defaults.DEBUG_TRACE) Log.info("[ARENA] transition= " + MatchState.ONCANCEL +" "+cancelOps);
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
		if (!isArena)
			ParamController.setTransitionOptions(mp, allTops);
		else {
			mp.setTransitionOptions(allTops);
		}
		/// By Default if they respawn in the arena.. people must want infinite lives
		if (mp.hasOptionAt(MatchState.ONSPAWN, TransitionOption.RESPAWN) && !cs.contains("nLives")){
			mp.setNLives(Integer.MAX_VALUE);
		}
		/// start auto setting this option, as really thats what they want
		if (mp.getNLives() != null && mp.getNLives() > 1){
			allTops.addTransitionOption(MatchState.ONDEATH, TransitionOption.RESPAWN);}
		return allTops;
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


	private static void loadBTOptions(ConfigurationSection cs, MatchParams mp, boolean isArena) throws ConfigException {
		if (cs.contains("tracking")){
			cs = cs.getConfigurationSection("tracking");}

		/// TeamJoinResult in tracking for this match type
		String dbName = (cs.contains("database")) ? cs.getString("database",null) : cs.getString("db",null);
		if (dbName != null){
			mp.setDBName(dbName);
			if (StatController.enabled()){
				try{
					if (!BTInterface.addBTI(mp)){
						Log.err("Couldn't add tracker interface");}
				} catch (Exception e){
					Log.err("Couldn't add tracker interface");
				}
			}
		}
		if (cs.contains("overrideBattleTracker")){
			mp.setUseTrackerPvP(cs.getBoolean("overrideBattleTracker", true));
		} else {
			mp.setUseTrackerPvP(cs.getBoolean("useTrackerPvP", false));
		}
		if (!isArena || cs.contains("useTrackerMessages"))
			mp.setUseTrackerMessages(cs.getBoolean("useTrackerMessages", false));
		if (cs.contains("teamRating")){
			mp.setTeamRating(cs.getBoolean("teamRating",false));}
		//		mp.set
		//		mp.setOverrideBTMessages(cs.getBoolean(path))
		/// What is the default rating for this match type
		Rating rating = cs.contains("rated") ? Rating.fromBoolean(cs.getBoolean("rated")) : Rating.ANY;
		if (rating == null || rating == Rating.UNKNOWN)
			throw new ConfigException("Could not parse rating: valid types. " + Rating.getValidList());
		mp.setRating(rating);
	}


	private static void loadGameSize(ConfigurationSection cs, MatchParams mp, boolean isArena) {
		if (cs.contains("gameSize") || isArena){
			cs = cs.getConfigurationSection("gameSize");}

		/// Number of teams and team sizes
		if (!isArena || (cs != null && cs.contains("teamSize"))) {
			mp.setTeamSizes(MinMax.valueOf(cs.getString("teamSize", "1+")));
		}
		if (!isArena || (cs != null && cs.contains("nTeams"))) {
			mp.setNTeams(MinMax.valueOf(cs.getString("nTeams", "2+")));
		}
	}


	private static void loadTimes(ConfigurationSection cs, MatchParams mp) {
		if (cs.contains("times")){
			cs = cs.getConfigurationSection("times");}
		if (cs == null)
			return;
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
		if (cs == null || !cs.contains("options"))
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
				if (to == TransitionOption.ENCHANTS) /// we deal with these later
					continue;
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
			Log.printStackTrace(e);
		}
		try{
			if (cs.contains("giveClass")){
				tops.addOption(TransitionOption.GIVECLASS, getArenaClasses(cs.getConfigurationSection("giveClass")));}
		} catch (Exception e){
			Log.err("Error setting the value of giveClass ");
			Log.printStackTrace(e);
		}
		try{
			if (cs.contains("giveDisguise")){
				tops.addOption(TransitionOption.GIVEDISGUISE, getArenaDisguises(cs.getConfigurationSection("giveDisguise")));}
		} catch (Exception e){
			Log.err("Error setting the value of giveDisguise ");
			Log.printStackTrace(e);
		}
		try{
			if (cs.contains("doCommands")){
				tops.addOption(TransitionOption.DOCOMMANDS, getDoCommands(cs.getStringList("doCommands")));}
		} catch (Exception e){
			Log.err("Error setting the value of doCommands ");
			Log.printStackTrace(e);
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
			Log.printStackTrace(e);
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
			Log.printStackTrace(e);
		}

		try{
			List<PotionEffect> effects = getEffectList(cs, "enchants");
			if (effects!=null && !effects.isEmpty())
				tops.addOption(TransitionOption.ENCHANTS, effects);
			else
				options.remove(TransitionOption.ENCHANTS);
		} catch (Exception e){
			Log.err("Error setting the value of enchants ");
			Log.printStackTrace(e);
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
				} catch (IllegalArgumentException e) {
					Log.err(cs.getCurrentPath() +"."+nodeString + " couldnt parse item " + str);
				} catch (Exception e){
					Log.err(cs.getCurrentPath() +"."+nodeString + " couldnt parse item " + str);
				}
			}
		} catch (Exception e){
			Log.err(cs.getCurrentPath() +"."+nodeString + " could not be parsed in config.yml");
			Log.printStackTrace(e);
		}
		return items;
	}

	public static JoinType getJoinType(ConfigurationSection cs) {
		if (cs == null || cs.getName() == null)
			return null;
		if (cs.getName().equalsIgnoreCase("Tourney"))
			return JoinType.JOINPHASE;
		boolean isMatch = !cs.getBoolean("isEvent",false);
		isMatch = cs.getBoolean("queue",isMatch);
		if (cs.contains("joinType")){
			String type = cs.getString("joinType");
			try{
				return JoinType.fromString(type);
			} catch (Exception e){
				Log.printStackTrace(e);
			}
		}
		return isMatch ? JoinType.QUEUE : JoinType.JOINPHASE;
	}

	public void save(MatchParams params){
		ConfigurationSection main = config.createSection(params.getName());
		saveParams(params,main, false);
		super.save();
	}

	/**
	 * Not sure how best to not save particular values if its an arena or not
	 * so for now, its a variable
	 * @param params
	 * @param main
	 * @param isArena
	 */
	public static void saveParams(MatchParams params, ConfigurationSection main, boolean isArena){
		ArenaParams parent = params.getParent();
		params.setParent(null); /// set the parent to null so we aren't grabbing options from the parent
		if (!isArena){
			main.set("name", params.getName());
			main.set("command", params.getCommand());
		}

		main.set("prefix", params.getPrefix());

		ConfigurationSection cs = main.createSection("gameSize");
		if (params.getSize() != null) cs.set("nTeams", params.getNTeamRange());
		if (params.getSize() != null) cs.set("teamSize", params.getTeamSizeRange());

		if (params.getNLives() != null) main.set("nLives", ArenaSize.toString(params.getNLives()));
		if (params.getVictoryType()!= null) main.set("victoryCondition", params.getVictoryType().getName());

		cs = main.createSection("times");
		if (params.getSecondsTillMatch() != null) cs.set("secondsTillMatch", params.getSecondsTillMatch());
		if (params.getMatchTime() != null) cs.set("matchTime", params.getMatchTime());
		if (params.getSecondsToLoot() != null) cs.set("secondsToLoot", params.getSecondsToLoot());

		if (params.getTimeBetweenRounds() != null) cs.set("timeBetweenRounds", params.getTimeBetweenRounds());
		if (params.getIntervalTime() != null) cs.set("matchUpdateInterval", params.getIntervalTime());

		cs = main.createSection("tracking");
		if (params.getDBName() != null) cs.set("database", params.getDBName());
		if (params.getRated() != Rating.ANY) cs.set("rated", params.isRated());
		if (params.getUseTrackerMessages() != null) cs.set("useTrackerMessages", params.getUseTrackerMessages());

		if (!isArena){
			main.set("arenaType", params.getType().getName());
			try{
				main.set("arenaClass", ArenaType.getArenaClass(params.getType()).getSimpleName());
			} catch(Exception e){
				main.set("arenaClass", params.getType().getClass().getSimpleName());
			}
		}
		if (params.getNConcurrentCompetitions() != null)  main.set("nConcurrentCompetitions", ArenaSize.toString(params.getNConcurrentCompetitions()));

		if (params.isWaitroomClosedWhenRunning() != null)  main.set("waitroomClosedWhileRunning", params.isWaitroomClosedWhenRunning());

		if (params.isCancelIfNotEnoughPlayers() != null)  main.set("cancelIfNotEnoughPlayers", params.isCancelIfNotEnoughPlayers());

		if (params.getArenaCooldown() != null)  main.set("arenaCooldown", params.getArenaCooldown());


		Collection<ArenaModule> modules = params.getModules();
		if (modules != null && !modules.isEmpty()){ main.set("modules", getModuleList(modules));}

		/// Announcements
		AnnouncementOptions ao = params.getAnnouncementOptions();
		if (ao != null){
			Map<MatchState, Map<AnnouncementOption, Object>> map = ao.getMatchOptions();
			if (map != null){
				cs = main.createSection("announcements");
				for (Entry<MatchState, Map<AnnouncementOption, Object>> entry : map.entrySet()){
					List<String> ops = new ArrayList<String>();
					for (Entry<AnnouncementOption,Object> entry2 : entry.getValue().entrySet()){
						Object o = entry2.getValue();
						ops.add(entry2.getKey() +(o != null ? o.toString() :""));
					}
					cs.set(entry.getKey().name(), ops);
				}
			}

			map = ao.getEventOptions();
			if (map != null){
				cs = main.createSection("eventAnnouncements");
				for (Entry<MatchState, Map<AnnouncementOption, Object>> entry : map.entrySet()){
					List<String> ops = new ArrayList<String>();
					for (Entry<AnnouncementOption,Object> entry2 : entry.getValue().entrySet()){
						Object o = entry2.getValue();
						ops.add(entry2.getKey() +(o != null ? o.toString() :""));
					}
					cs.set(entry.getKey().name(), ops);
				}
			}
		}

		MatchTransitions alltops = params.getTransitionOptions();
		if (alltops != null){
			Map<MatchState,TransitionOptions> transitions =
					new TreeMap<MatchState,TransitionOptions>(alltops.getAllOptions());
			for (MatchState ms: transitions.keySet()){
				try{
					if (ms == MatchState.ONCANCEL)
						continue;
					TransitionOptions tops = transitions.get(ms);
					if (tops == null)
						continue;
					if (tops.getOptions() == null)
						continue;
					tops = new TransitionOptions(tops); // make a copy so we can modify while saving
					Map<TransitionOption,Object> ops = tops.getOptions();
					List<String> list = new ArrayList<String>();

					for (Entry<String,TransitionOptions> entry : OptionSetController.getOptionSets().entrySet()){
						if (tops.containsAll(entry.getValue())){
							list.add(entry.getKey());
							for (TransitionOption op : entry.getValue().getOptions().keySet()){
								ops.remove(op);
							}
						}
					}
					/// transition map
					Map<String,Object> tmap = new LinkedHashMap<String,Object>();
					HashSet<TransitionOption> possibleOptionSet = new HashSet<TransitionOption>();
					ops = new TreeMap<TransitionOption,Object>(ops); /// try to maintain some ordering
					for (TransitionOption to: ops.keySet()){
						try{
							String s;
							possibleOptionSet.add(to);
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
							Log.printStackTrace(e);
						}
					}
//					list = getOptionSets(possibleOptionSet);
					tmap.put("options", list);
					//			main.put(ms.toString(), tmap);
					main.set(ms.toString(), tmap);
				} catch(Exception e){
					Log.printStackTrace(e);
				}
			}
		}

		//		main.set("options", map);
		params.setParent(parent); ///reset the parent
	}

	private static List<String> getModuleList(Collection<ArenaModule> modules) {
		List<String> list = new ArrayList<String>();
		if (modules != null){
			for (ArenaModule m: modules){
				list.add(m.getName());}
		}
		return list;
	}

	private static List<String> getEnchants(List<PotionEffect> effects) {
		List<String> list = new ArrayList<String>();
		if (effects != null){
			for (PotionEffect is: effects){
				list.add(EffectUtil.getEnchantString(is));}
		}
		return list;
	}

	private static List<String> getItems(List<ItemStack> items) {
		List<String> list = new ArrayList<String>();
		if (items != null){
			for (ItemStack is: items){
				list.add(InventoryUtil.getItemString(is));}
		}
		return list;
	}

	private static Map<String,Object> getArenaClasses(Map<Integer, ArenaClass> classes) {
		HashMap<String,Object> map = new HashMap<String, Object>();
		for (Integer teamNumber: classes.keySet()){
			String teamName = teamNumber == ArenaClass.DEFAULT.intValue() ? "default" : "team" + teamNumber;
			map.put(teamName, classes.get(teamNumber).getName());
		}
		return map;
	}
	private static List<String> getDoCommandsStringList(List<CommandLineString> doCommands) {
		List<String> list = new ArrayList<String>();
		if (doCommands != null){
			for (CommandLineString s: doCommands){
				list.add(s.getRawCommand());}
		}
		return list;
	}

}
