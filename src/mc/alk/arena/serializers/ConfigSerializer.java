package mc.alk.arena.serializers;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.ArenaClassController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.Rating;
import mc.alk.arena.objects.Exceptions.ConfigException;
import mc.alk.arena.objects.Exceptions.InvalidArgumentException;
import mc.alk.arena.objects.Exceptions.InvalidOptionException;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.messaging.AnnouncementOptions;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.options.TransitionOptions.TransitionOption;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.util.BTInterface;
import mc.alk.arena.util.EffectUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.SerializerUtil;
import mc.alk.arena.util.Util;
import mc.alk.arena.util.Util.MinMax;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

/**
 *
 * @author alkarin
 *
 */
public class ConfigSerializer extends BaseSerializer{
	static HashMap<ArenaType, ConfigSerializer> configs = new HashMap<ArenaType, ConfigSerializer>();

	public void setConfig(ArenaType at, String f){
		setConfig(at, new File(f));
	}

	public void setConfig(ArenaType at, File f){
		super.setConfig(f);
		if (at != null){ /// Other plugins using BattleArena, the name is the matchType or eventType name
			configs.put(at, this);}
	}

	public static ConfigSerializer getConfig(ArenaType arenaType){
		return configs.get(arenaType);
	}

	public static ConfigurationSection getOtherOptions(ArenaType arenaType){
		ConfigSerializer cs = getConfig(arenaType);
		return cs != null ? cs.getConfigurationSection(arenaType.getName()+".otherOptions") : null;
	}

	public static void reloadConfig(ArenaType arenaType) {
		final String name = arenaType.getName();
		ConfigSerializer cs = configs.get(arenaType);
		if (cs == null){
			Log.err("Couldnt find the serializer for " + name);
			return;
		}
		try {
			cs.reloadFile();
			ConfigSerializer.setTypeConfig(name,cs.getConfigurationSection(name),
					!(ParamController.getMatchParams(arenaType.getName()) instanceof EventParams));
		} catch (ConfigException e) {
			e.printStackTrace();
			Log.err("Error reloading " + name);
		} catch (InvalidOptionException e) {
			e.printStackTrace();
			Log.err("Error reloading " + name);
		}
	}

	public static MatchParams setTypeConfig(final String name, ConfigurationSection cs, boolean match) throws ConfigException, InvalidOptionException {
		if (cs == null){
			Log.err("[BattleArena] configSerializer can't load " + name +" with a config section of " + cs);
			return null;
		}
		//		System.out.println(" Setting up " + cs.getCurrentPath() +"   name=" +name);
		/// Set up match options.. specifying defaults where not specified
		/// Set Arena Type
		ArenaType at;
		if (cs.contains("arenaType")){
			at = ArenaType.fromString(cs.getString("arenaType"));
		} else if (cs.contains("type")){ /// old config option for setting arenaType
			at = ArenaType.fromString(cs.getString("type"));
		} else {
			at = ArenaType.fromString(cs.getName()); /// Get it from the configuration section name
			if (at == null)
				at = ArenaType.VERSUS; /// Default arena Type
		}
		if (at == null)
			throw new ConfigException("Could not parse arena type: valid types. " + ArenaType.getValidList());

		/// What is the default rating for this match type
		Rating rating = cs.contains("rated") ? Rating.fromBoolean(cs.getBoolean("rated")) : Rating.RATED;
		if (rating == null || rating == Rating.UNKNOWN)
			throw new ConfigException("Could not parse rating: valid types. " + Rating.getValidList());

		/// How does one win this matchType
		VictoryType vt;
		if (cs.contains("victoryCondition")){
			vt = VictoryType.fromString(cs.getString("victoryCondition"));
		} else {
			vt = VictoryType.DEFAULT;
		}

		// TODO make unknown types with a valid plugin name be deferred until after the other plugin is loaded
		if (vt == null){
			throw new ConfigException("Could not add the victoryCondition " +cs.getString("victoryCondition") +"\n"
					+"valid types are " + VictoryType.getValidList());}

		/// Number of teams and team sizes
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
		MatchParams pi = match ? new MatchParams(at, rating,vt) : new EventParams(at,rating, vt);

		/// Convert first letter of name to upper case
		StringBuilder sb = new StringBuilder();
		sb.append(name.substring(0,1).toUpperCase());
		sb.append(name.substring(1,name.length()));
		pi.setName(sb.toString());

		pi.setCommand( cs.contains("command") ? cs.getString("command") : name);
		if (cs.contains("cmd")) /// turns out I used cmd in a lot of old configs.. so use both :(
			pi.setCommand(cs.getString("cmd"));
		pi.setPrefix( cs.contains("prefix") ? cs.getString("prefix") : "&6["+name+"]");
		pi.setMinTeams(minTeams);
		pi.setMaxTeams(maxTeams);
		pi.setMinTeamSize(minTeamSize);
		pi.setMaxTeamSize(maxTeamSize);
		pi.setPreferredMinTeamSize(pminTeamSize);
		pi.setPreferredMaxTeamSize(pmaxTeamSize);

		pi.setTimeBetweenRounds( cs.contains("timeBetweenRounds") ? cs.getInt("timeBetweenRounds") : Defaults.TIME_BETWEEN_ROUNDS);
		pi.setSecondsToLoot( cs.contains("secondsToLoot") ? cs.getInt("secondsToLoot") : Defaults.SECONDS_TO_LOOT);
		pi.setSecondsTillMatch( cs.contains("secondsTillMatch") ? cs.getInt("secondsTillMatch") : Defaults.SECONDS_TILL_MATCH);

		pi.setMatchTime(cs.contains("matchTime") ? cs.getInt("matchTime") : Defaults.MATCH_TIME);
		//		pi.setEv(cs.contains("eventCountdownTime") ? cs.getInt("eventCountdownTime") : Defaults.AUTO_EVENT_COUNTDOWN_TIME);
		//		pi.setIntervalTime(cs.contains("eventCountdownInterval") ? cs.getInt("eventCountdownInterval") : Defaults.ANNOUNCE_EVENT_INTERVAL);
		pi.setIntervalTime(cs.contains("matchUpdateInterval") ? cs.getInt("matchUpdateInterval") : Defaults.MATCH_UPDATE_INTERVAL);

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
			TransitionOptions tops = null;
			try{
				tops = getParameters(cs.getConfigurationSection(transition.toString()));
				switch (transition){
				case ONCANCEL: /// OnCancel gets taken from onComplete and modified
					continue;
				case ONENTER: /// By Default on enter gets to store
				case ONENTERWAITROOM: /// as does enter wait room, these wont overwrite each other
					if (tops == null) tops = new TransitionOptions();
					tops.addOption(TransitionOption.STOREEXPERIENCE);
					tops.addOption(TransitionOption.STOREGAMEMODE);
					tops.addOption(TransitionOption.STOREHEROCLASS);
					if (allTops.needsClearInventory()){
						tops.addOption(TransitionOption.CLEARINVENTORYONFIRSTENTER);
						tops.addOption(TransitionOption.STOREITEMS);
					}
					break;
				case ONLEAVE: /// By Default on leave gets to restore items and exp
					if (tops == null) tops = new TransitionOptions();
					tops.addOption(TransitionOption.RESTOREEXPERIENCE);
					tops.addOption(TransitionOption.RESTOREGAMEMODE);
					tops.addOption(TransitionOption.RESTOREHEROCLASS);
					if (allTops.needsClearInventory())
						tops.addOption(TransitionOption.RESTOREITEMS);
					break;
				default:
					break;
				}
			} catch (Exception e){
				Log.err("Invalid Option was not added!!! transition= " + transition);
				e.printStackTrace();
				continue;
			}
			if (tops == null){
				allTops.removeOptions(transition);
				continue;}
			if (Defaults.DEBUG_TRACE) System.out.println("[ARENA] transition= " + transition +" "+tops);
			//			TOC.setOptions(transition, pi,tops);
			if (transition == MatchState.ONCOMPLETE){
				TransitionOptions cancelOps = new TransitionOptions(tops);
				allTops.addTransition(MatchState.ONCANCEL, cancelOps);
				if (Defaults.DEBUG_TRACE) System.out.println("[ARENA] transition= " + MatchState.ONCANCEL +" "+cancelOps);
			}
			allTops.addTransition(transition,tops);
		}
		pi.setAllTransitionOptions(allTops);
		ParamController.removeMatchType(pi);
		ParamController.addMatchType(pi);

		Log.info(BattleArena.getPName()+" registering match =" + pi +" BattleTrackerInterface=" + (dbName != null ? dbName : "none"));
		return pi;
	}

	private static TransitionOptions getParameters(ConfigurationSection cs) throws InvalidOptionException, InvalidArgumentException {
		if (cs == null)
			return null;
		Set<Object> optionsstr = new HashSet<Object>(cs.getList("options"));
		if (optionsstr.isEmpty())
			return null;
		Map<TransitionOption,Object> options = new EnumMap<TransitionOption,Object>(TransitionOption.class);
		TransitionOptions tops = new TransitionOptions();
		for (Object obj : optionsstr){
			String s = obj.toString();
			String[] split = s.split("=");
			split[0] = split[0].trim().toUpperCase();
			TransitionOption to = null;
			try{
				to = TransitionOption.valueOf(split[0]);
				if (to != null && to.hasValue() && split.length==1){
					Log.err("Transition Option " + to +" needs a value! " + split[0]+"=<value>");
					continue;
				}
			} catch (Exception e){
				Log.err("Transition Option " + split[0] +" doesn't exist!");
				continue;
			}

			options.put(to,null);
			if (split.length == 1){
				continue;}

			split[1] = split[1].trim();
//			System.out.println("split[0]= " + split[0] +"   " + split[1]);
			try{
				switch(to){
				case MONEY:tops.setMoney(Double.valueOf(split[1])); break;
				case EXPERIENCE: tops.setGiveExperience(Integer.valueOf(split[1])); break;
				case HEALTH: tops.setHealth(Integer.valueOf(split[1])); break;
				case HUNGER: tops.setHunger(Integer.valueOf(split[1])); break;
				case DISGUISEALLAS: tops.setDisguiseAllAs(split[1]); break;
				case WITHINDISTANCE: tops.setWithinDistance(Integer.valueOf(split[1])); break;
//				case TELEPORTTO:
//				case TELEPORTWINNER:
//				case TELEPORTLOSER:
//					Location l = SerializerUtil.getLocation(split[1]);
//					if (l == null)
//						throw new InvalidArgumentException("Location was not correct. needs to be in the form: world,x,y,z or world,x,y,z,pitch,yaw");
//					tops.addOption(to, l);
//					break;
				default:
					break;
				}
			} catch (Exception e){
				Log.err("Error setting the value of option " + to);
				e.printStackTrace();
			}
		}
		tops.setMatchOptions(options);

		if (cs.contains("teleportTo")){
			tops.addOption(TransitionOption.TELEPORTTO, SerializerUtil.getLocation(cs.getString("teleportTo")));}
		if (cs.contains("teleportWinner")){
			tops.addOption(TransitionOption.TELEPORTWINNER, SerializerUtil.getLocation(cs.getString("teleportWinner")));}
		if (cs.contains("teleportLoser")){
			tops.addOption(TransitionOption.TELEPORTLOSER, SerializerUtil.getLocation(cs.getString("teleportLoser")));}
		if (cs.contains("teleportOnArenaExit")){
			tops.addOption(TransitionOption.TELEPORTONARENAEXIT, SerializerUtil.getLocation(cs.getString("teleportOnArenaExit")));}
		if (cs.contains("giveClass")){
			tops.addOption(TransitionOption.GIVECLASS, getArenaClasses(cs.getConfigurationSection("giveClass")));}
		if (options.containsKey(TransitionOption.NEEDITEMS)){
			tops.addOption(TransitionOption.NEEDITEMS,getItemList(cs, "items"));}
		if (options.containsKey(TransitionOption.GIVEITEMS)){
			tops.addOption(TransitionOption.GIVEITEMS,getItemList(cs, "items"));}
		setPermissionSection(cs,"addPerms",tops);
		if (options.containsKey(TransitionOption.ENCHANTS)){ tops.setEffects(getEffectList(cs, "enchants"));}
		return tops;
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
					Log.err("Couldnt find which team this string belongs to '" + whichTeam+"'");
					continue;
				}
			}
			if (team ==-1){
				Log.err("Couldnt find which team this string belongs to '" + whichTeam+"'");
				continue;
			}
			classes.put(team, ac);
		}
		return classes;
	}
	public static List<PotionEffect> getEffectList(ConfigurationSection cs, String nodeString) {
		final int strengthDefault = 1;
		final int timeDefault = 60;
		ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();
		try {
			String str = null;
			for (Object o : cs.getList(nodeString)){
				str = o.toString();
				PotionEffect ewa = EffectUtil.parseArg(str,strengthDefault,timeDefault);
				if (ewa != null) {
					effects.add(ewa);
				} else {
					Log.warn(cs.getCurrentPath() +"."+nodeString + " could not be parsed in config.yml");
				}
			}
		} catch (Exception e){
			Log.warn(cs.getCurrentPath() +"."+nodeString + " could not be parsed in config.yml");
		}
		return effects;
	}

	public static ArrayList<ItemStack> getItemList(ConfigurationSection cs, String nodeString) {
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
						Log.warn(cs.getCurrentPath() +"."+nodeString + " couldnt parse item " + str);
					}
				} catch (Exception e){
					Log.warn(cs.getCurrentPath() +"."+nodeString + " couldnt parse item " + str);
				}
			}
		} catch (Exception e){
			Log.warn(cs.getCurrentPath() +"."+nodeString + " could not be parsed in config.yml");
		}
		return items;
	}


}
