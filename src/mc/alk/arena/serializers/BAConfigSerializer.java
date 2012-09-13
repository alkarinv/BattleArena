package mc.alk.arena.serializers;

import java.util.List;
import java.util.Set;

import mc.alk.arena.Defaults;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.messaging.AnnouncementOptions;
import mc.alk.arena.objects.messaging.AnnouncementOptions.AnnouncementOption;
import mc.alk.arena.util.KeyValue;
import mc.alk.arena.util.Log;

import org.bukkit.configuration.ConfigurationSection;

public class BAConfigSerializer extends ConfigSerializer{
	
	public void loadAll(){
		try {config.load(f);} catch (Exception e){e.printStackTrace();}

		parseDefaultOptions(config.getConfigurationSection("defaultOptions"));
		Defaults.MONEY_STR = config.getString("moneyName");
		String[] defaultArenaTypes = {"arena","skirmish","colliseum","freeForAll","deathMatch","tourney","battleground"};

		/// Now initialize the specific settings
		for (String defaultType: defaultArenaTypes){
			try {
				setTypeConfig(defaultType,config.getConfigurationSection(defaultType));
			} catch (Exception e) {
				Log.err("Couldnt configure arenaType " + defaultType+". " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	protected static void parseDefaultOptions(ConfigurationSection cs) {
		Defaults.SECONDS_TILL_MATCH = cs.getInt("secondsTillMatch", 20);
		Defaults.SECONDS_TO_LOOT = cs.getInt("secondsToLoot", 20);
		Defaults.MATCH_TIME = cs.getInt("matchTime", 120/*matchEndTime*/);
		Defaults.AUTO_EVENT_COUNTDOWN_TIME = cs.getInt("eventCountdownTime",180);
		Defaults.ANNOUNCE_EVENT_INTERVAL = cs.getInt("eventCountdownInterval", 60);
		Defaults.MATCH_UPDATE_INTERVAL = cs.getInt("matchUpdateInterval", 30);
		AnnouncementOptions an = new AnnouncementOptions();
		parseAnnouncementOptions(an,true,cs.getConfigurationSection("announcements"), true);
		parseAnnouncementOptions(an,false,cs.getConfigurationSection("eventAnnouncements"),true);
		AnnouncementOptions.setDefaultOptions(an);
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
