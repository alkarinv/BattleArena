package mc.alk.arena.serializers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.ArenaCommand;
import mc.alk.arena.controllers.CommandController;
import mc.alk.arena.executors.BAExecutor;
import mc.alk.arena.executors.ReservedArenaEventExecutor;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.messaging.AnnouncementOptions;
import mc.alk.arena.objects.messaging.AnnouncementOptions.AnnouncementOption;
import mc.alk.arena.util.DisabledCommandsUtil;
import mc.alk.arena.util.KeyValue;
import mc.alk.arena.util.Log;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class BAConfigSerializer extends ConfigSerializer{

	public void loadAll(){
		/// Do this after 0 ticks so all Custom Arena/Victory or other types can be registered by other plugins first
//		Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new Runnable(){
//			@Override
//			public void run() {
				synchronizedLoadAll();
//			}
//		});
	}
	private void synchronizedLoadAll(){
		try {config.load(file);} catch (Exception e){e.printStackTrace();}

		parseDefaultOptions(config.getConfigurationSection("defaultOptions"));
		if (!Defaults.MONEY_SET)
			Defaults.MONEY_STR = config.getString("moneyName",Defaults.MONEY_STR);
		Defaults.AUTO_UPDATE = config.getBoolean("autoUpdate", Defaults.AUTO_UPDATE);
		Defaults.TELEPORT_Y_OFFSET = config.getDouble("teleportYOffset", Defaults.TELEPORT_Y_OFFSET);
		Defaults.NUM_INV_SAVES = config.getInt("numberSavedInventories", Defaults.NUM_INV_SAVES);
		Defaults.IGNORE_STACKSIZE = config.getBoolean("ignoreMaxStackSize", Defaults.IGNORE_STACKSIZE);
		Defaults.USE_ARENAS_ONLY_IN_ORDER = config.getBoolean("useArenasOnlyInOrder", Defaults.USE_ARENAS_ONLY_IN_ORDER);
		DisabledCommandsUtil.addAll(config.getStringList("disabledCommands"));
		Set<String> defaultMatchTypes = new HashSet<String>(Arrays.asList(new String[] {"arena","skirmish","colliseum","battleground"}));
		Set<String> defaultEventTypes = new HashSet<String>(Arrays.asList(new String[] {"freeForAll","deathMatch","tourney"}));
		JavaPlugin plugin = BattleArena.getSelf();

		/// Now initialize the specific match settings
		for (String defaultType: defaultMatchTypes){
			try {
				setTypeConfig(plugin,defaultType,config.getConfigurationSection(defaultType), true);
			} catch (Exception e) {
				Log.err("Couldnt configure arenaType " + defaultType+". " + e.getMessage());
				e.printStackTrace();
			}
		}
		/// Now initialize the specific event settings
		for (String defaultType: defaultEventTypes){
			try {
				setTypeConfig(plugin,defaultType,config.getConfigurationSection(defaultType), false);
			} catch (Exception e) {
				Log.err("Couldnt configure arenaType " + defaultType+". " + e.getMessage());
				e.printStackTrace();
			}
		}
		/// Initialize custom matches or events
		Set<String> keys = config.getKeys(false);
		for (String key: keys){
			ConfigurationSection cs = config.getConfigurationSection(key);
			if (cs == null || defaultMatchTypes.contains(key) || defaultEventTypes.contains(key) || key.equals("defaultOptions"))
				continue;
			try {
				/// A new match/event needs the params, an executor, and the command to use
				boolean isMatch = !config.getBoolean(key+".isEvent",false);
				MatchParams mp = setTypeConfig(plugin,key,cs, isMatch);
				BAExecutor executor = isMatch ? BattleArena.getBAExecutor() : new ReservedArenaEventExecutor();
				ArenaCommand arenaCommand = new ArenaCommand(mp.getCommand(),"","", new ArrayList<String>(), BattleArena.getSelf());
				arenaCommand.setExecutor(executor);
				CommandController.registerCommand(arenaCommand);
			} catch (Exception e) {
				Log.err("Couldnt configure arenaType " + key+". " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	protected static void parseDefaultOptions(ConfigurationSection cs) {
		Defaults.SECONDS_TILL_MATCH = cs.getInt("secondsTillMatch", Defaults.SECONDS_TILL_MATCH);
		Defaults.SECONDS_TO_LOOT = cs.getInt("secondsToLoot", Defaults.SECONDS_TO_LOOT);
		Defaults.MATCH_TIME = cs.getInt("matchTime", Defaults.MATCH_TIME);
		Defaults.AUTO_EVENT_COUNTDOWN_TIME = cs.getInt("eventCountdownTime",Defaults.AUTO_EVENT_COUNTDOWN_TIME);
		Defaults.ANNOUNCE_EVENT_INTERVAL = cs.getInt("eventCountdownInterval", Defaults.ANNOUNCE_EVENT_INTERVAL);
		Defaults.ALLOW_PLAYER_EVENT_CREATION = cs.getBoolean("allowPlayerCreation", Defaults.ALLOW_PLAYER_EVENT_CREATION);
		Defaults.MATCH_UPDATE_INTERVAL = cs.getInt("matchUpdateInterval", Defaults.MATCH_UPDATE_INTERVAL);
		Defaults.FORCESTART_ENABLED = cs.getBoolean("matchEnableForceStart", Defaults.FORCESTART_ENABLED);
		Defaults.FORCESTART_TIME = cs.getLong("matchForceStartTime", Defaults.FORCESTART_TIME);
		Defaults.DUEL_ALLOW_RATED = cs.getBoolean("allowRatedDuels", Defaults.DUEL_ALLOW_RATED);
		Defaults.DUEL_CHALLENGE_INTERVAL = cs.getInt("challengeInterval", Defaults.DUEL_CHALLENGE_INTERVAL);
		Defaults.TIME_BETWEEN_SCHEDULED_EVENTS = cs.getInt("timeBetweenScheduledEvents", Defaults.TIME_BETWEEN_SCHEDULED_EVENTS);
		Defaults.SCHEDULER_ANNOUNCE_TIMETILLNEXT = cs.getBoolean("announceTimeTillNextEvent", Defaults.SCHEDULER_ANNOUNCE_TIMETILLNEXT);

		parseOnServerStartOptions(cs);
		AnnouncementOptions an = new AnnouncementOptions();
		parseAnnouncementOptions(an,true,cs.getConfigurationSection("announcements"), true);
		parseAnnouncementOptions(an,false,cs.getConfigurationSection("eventAnnouncements"),true);
		AnnouncementOptions.setDefaultOptions(an);
	}

	private static void parseOnServerStartOptions( ConfigurationSection cs) {
		if (cs ==null || !cs.contains("onServerStart")){
			Log.warn(BattleArena.getPName() +" No onServerStart options found");
			return;
		}
		List<String> options = cs.getStringList("onServerStart");
		for (String op : options){
			if (op.equalsIgnoreCase("startContinuous")) Defaults.START_CONTINUOUS = true;
			else if (op.equalsIgnoreCase("startNext")) Defaults.START_NEXT = true;
		}
	}

	public static AnnouncementOptions parseAnnouncementOptions(AnnouncementOptions an , boolean match, ConfigurationSection cs, boolean warn) {
		if (cs == null){
			if (warn)
				Log.err((match? "match" : "event" ) + " announcements are null. cs= ");
			return null;
		}
		Set<String> keys = cs.getKeys(false);
		for (String key: keys){
			MatchState ms = MatchState.fromName(key);
			//			System.out.println("contains " +key + "  ms=" + ms);
			if (ms == null){
				Log.err("Couldnt recognize matchstate " + key +" in the announcement options");
				continue;
			}
			List<String> list = cs.getStringList(key);
			for (String s: list){
				KeyValue<String,String> kv = KeyValue.split(s,"=");
				AnnouncementOption bo = AnnouncementOption.fromName(kv.key);
				if (bo == null){
					Log.err("Couldnt recognize AnnouncementOption " + s);
					continue;
				}
				//				System.out.println("!!!!! Setting broadcast option " +ms +"  " + bo + "  " + kv.value);
				an.setBroadcastOption(match, ms, bo,kv.value);
			}
		}
		return an;
	}

}
