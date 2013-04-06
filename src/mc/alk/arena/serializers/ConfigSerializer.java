package mc.alk.arena.serializers;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.ArenaClassController;
import mc.alk.arena.controllers.OptionSetController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.ArenaParams;
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
import org.bukkit.GameMode;
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


	public MatchParams loadType() throws ConfigException, InvalidOptionException {
		return ConfigSerializer.loadType(plugin, this, name);
	}

	public static MatchParams loadType(Plugin plugin, BaseConfig config, String name)
			throws ConfigException, InvalidOptionException {
		ConfigurationSection cs = config.getConfigurationSection(name);

		if (cs == null){
			throw new ConfigException("configSerializer can't load " + name +" with a config section of " + cs);
		}
		/// Set up match options.. specifying defaults where not specified
		/// Set Arena Type
		ArenaType at;
		at = getArenaType(plugin, cs);
		if (at == null && !name.equalsIgnoreCase("tourney"))
			throw new ConfigException("Could not parse arena type. Valid types. " + ArenaType.getValidList());

		/// What is the default rating for this match type
		Rating rating = cs.contains("rated") ? Rating.fromBoolean(cs.getBoolean("rated")) : Rating.RATED;

		if (rating == null || rating == Rating.UNKNOWN)
			throw new ConfigException("Could not parse rating: valid types. " + Rating.getValidList());

		/// How does one win this matchType
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
		JoinType jt = getJoinType(cs);
		MatchParams pi = jt == JoinType.QUEUE ? new MatchParams(at, rating,vt) : new EventParams(at,rating, vt);

		pi.setName(StringUtils.capitalize(name));

		pi.setCommand(cs.getString("command",name));
		if (cs.contains("cmd")){ /// turns out I used cmd in a lot of old configs.. so use both :(
			pi.setCommand(cs.getString("cmd"));}
		ArenaType.addAliasForType(name, pi.getCommand());
		pi.setPrefix( cs.getString("prefix","&6["+name+"]"));
		pi.setMinTeams(minTeams);
		pi.setMaxTeams(maxTeams);
		pi.setMinTeamSize(minTeamSize);
		pi.setMaxTeamSize(maxTeamSize);

		pi.setTimeBetweenRounds(cs.getInt("timeBetweenRounds",Defaults.TIME_BETWEEN_ROUNDS));
		pi.setSecondsToLoot( cs.getInt("secondsToLoot", Defaults.SECONDS_TO_LOOT));
		pi.setSecondsTillMatch( cs.getInt("secondsTillMatch",Defaults.SECONDS_TILL_MATCH));

		pi.setMatchTime(cs.getInt("matchTime",Defaults.MATCH_TIME));
		pi.setIntervalTime(cs.getInt("matchUpdateInterval",Defaults.MATCH_UPDATE_INTERVAL));

		pi.setOverrideBattleTracker(cs.getBoolean("overrideBattleTracker", true));
		pi.setNLives(cs.getInt("nDeaths",1));
		String nLives = cs.getString("nLives", null);
		if (nLives != null){
			if (nLives.equalsIgnoreCase("infinite")){
				pi.setNLives(Integer.MAX_VALUE);
			} else {
				pi.setNLives(Integer.valueOf(nLives));
			}
		}
		pi.setDuelOnly(cs.getBoolean("duelOnly", false));
		pi.setNConcurrentCompetitions(cs.getInt("nConcurrentCompetitions",Integer.MAX_VALUE));

		if (cs.contains("announcements")){
			AnnouncementOptions an = new AnnouncementOptions();
			BAConfigSerializer.parseAnnouncementOptions(an,true,cs.getConfigurationSection("announcements"), false);
			BAConfigSerializer.parseAnnouncementOptions(an,false,cs.getConfigurationSection("eventAnnouncements"),false);
			pi.setAnnouncementOptions(an);
		}
		/// TeamJoinResult in tracking for this match type
		String dbName = cs.getString("database");
		if (dbName != null){
			pi.setDBName(dbName);
			if (!BTInterface.addBTI(pi))
				dbName = null;
		}

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
			default:
				break;
			}
			allTops.addTransitionOptions(transition,tops);
		}
		ParamController.setTransitionOptions(pi, allTops);
		/// By Default if they respawn in the arena.. people must want infinite lives
		if (pi.getTransitionOptions().hasOptionAt(MatchState.ONSPAWN, TransitionOption.RESPAWN) && !cs.contains("nLives")){
			pi.setNLives(Integer.MAX_VALUE);
		}
		ParamController.removeMatchType(pi);
		ParamController.addMatchType(pi);

		Log.info(plugin.getName()+" registering " + pi.getName() +",bti=" + (dbName != null ? dbName : "none")+",join="+pi.getJoinType());
		return pi;
	}

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

			/// Check first to see if this option is actually a set of options
			TransitionOptions optionSet = OptionSetController.getOptionSet(key);
			if (optionSet != null){
				tops.addOptions(optionSet);
				continue;
			}

			TransitionOption to = null;
			try{
				to = TransitionOption.fromString(key);
				if (to != null && to.hasValue() && split.length==1){
					Log.err("Transition Option " + to +" needs a value! " + key+"=<value>");
					continue;
				}
			} catch (Exception e){
				Log.err("Transition Option " + key +" doesn't exist!");
				continue;
			}

			options.put(to,null);
			if (split.length == 1){
				continue;}

			/// Handle values for this option
			final String value = split[1].trim();
			try{
				switch(to){
				case MONEY:
					options.put(to, Double.valueOf(value));
					break;
				case LEVELRANGE:
					options.put(to, MinMax.valueOf(value));
					break;
				case DISGUISEALLAS:
					options.put(to, value);
					break;
				case HEALTH: case HEALTHP:
				case MAGIC: case MAGICP:
				case HUNGER:
				case EXPERIENCE:
				case WITHINDISTANCE:
					options.put(to,Integer.valueOf(value));
					break;
				case INVULNERABLE:
					options.put(to,Integer.valueOf(value)*20); // multiply by number of ticks per second
					break;
				case FLIGHTSPEED:
					options.put(to,Float.valueOf(value));
					break;
				case GAMEMODE:
					GameMode gm = null;
					try {
						gm = GameMode.getByValue(Integer.valueOf(value));
					} catch (Exception e){
						gm = GameMode.valueOf(value.toUpperCase());
					}
					options.put(to,gm); // multiply by number of ticks per second
					break;
				default:
					break;
				}
			} catch (Exception e){
				Log.err("Error setting the value of option " + to);
				e.printStackTrace();
			}
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
			if (cs.contains("teleportWinner")){
				tops.addOption(TransitionOption.TELEPORTWINNER, SerializerUtil.getLocation(cs.getString("teleportWinner")));}
		} catch (Exception e){
			Log.err("Error setting the value of teleportWinner ");
			e.printStackTrace();
		}
		try{
			if (cs.contains("teleportLoser")){
				tops.addOption(TransitionOption.TELEPORTLOSER, SerializerUtil.getLocation(cs.getString("teleportLoser")));}
		} catch (Exception e){
			Log.err("Error setting the value of teleportLoser ");
			e.printStackTrace();
		}

		try{
			if (cs.contains("teleportOnArenaExit")){
				tops.addOption(TransitionOption.TELEPORTONARENAEXIT, SerializerUtil.getLocation(cs.getString("teleportOnArenaExit")));}
		} catch (Exception e){
			Log.err("Error setting the value of teleportOnArenaExit ");
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
			if (options.containsKey(TransitionOption.NEEDITEMS)){
				List<ItemStack> items = getItemList(cs, "items");
				if (items!=null && !items.isEmpty())
					tops.addOption(TransitionOption.NEEDITEMS,items);
				else
					options.remove(TransitionOption.NEEDITEMS);
			}
		} catch (Exception e){
			Log.err("Error setting the value of needItems ");
			e.printStackTrace();
		}
		try{
			if (options.containsKey(TransitionOption.GIVEITEMS)){
				List<ItemStack> items = getItemList(cs, "items");
				if (items!=null && !items.isEmpty())
					tops.addOption(TransitionOption.GIVEITEMS,items);
				else
					options.remove(TransitionOption.GIVEITEMS);
			}
		} catch (Exception e){
			Log.err("Error setting the value of giveItems ");
			e.printStackTrace();
		}

		try{
			if (options.containsKey(TransitionOption.ENCHANTS)){
				List<PotionEffect> effects = getEffectList(cs, "enchants");
				if (effects!=null && !effects.isEmpty())
					tops.addOption(TransitionOption.ENCHANTS, effects);
			}
		} catch (Exception e){
			Log.err("Error setting the value of enchants ");
			e.printStackTrace();
		}

		setPermissionSection(cs,"addPerms",tops);

		return tops;
	}

	private static List<CommandLineString> getDoCommands(List<String> list) throws InvalidOptionException {
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
			final ArenaClass ac = ArenaClassController.getClass(className);
			if (ac == null){
				Log.err("Couldnt find arenaClass " + className);
				continue;
			}
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
					Log.err("Effect "+cs.getCurrentPath() +"."+nodeString +"."+str+ " could not be parsed in classes.yml");
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
				return JoinType.valueOf(type.toUpperCase());
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		return isMatch ? JoinType.QUEUE : JoinType.JOINPHASE;
	}

}
