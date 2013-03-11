package mc.alk.arena.serializers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.EventScheduler;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.pairs.EventPair;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.SerializerUtil;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;


public class EventScheduleSerializer extends BaseConfig {
	EventScheduler es;

	public void loadAll(){
		try {config.load(file);} catch (Exception e){e.printStackTrace();}
		loadScheduledEvents(config.getConfigurationSection("events"));

	}

	public void loadScheduledEvents(ConfigurationSection cs) {
		if (cs == null){
			Log.info(BattleArena.getPluginName() +" has no scheduled events");
			return;}
		List<String> keys = new ArrayList<String>(cs.getKeys(false));
		Collections.sort(keys);
		for (String key : keys){
			String se = cs.getString(key);
			if (se == null)
				continue;
			String[] fullargs = se.split(" ");
			EventParams eventParams = ParamController.getEventParamCopy(fullargs[0]);
			if (eventParams == null){
				Log.err(BattleArena.getPluginName()+" couldn't reparse the scheduled event " + fullargs[0]);
				continue;
			}
			String[] args = Arrays.copyOfRange(fullargs, 1, fullargs.length);
			es.scheduleEvent(eventParams, args);
		}
	}

	public void saveScheduledEvents(){
		if (es == null)
			return;
		List<EventPair> events = es.getEvents();
		if (events == null)
			return;
		int i = 0;
		Map<String,Object> map = new HashMap<String,Object>();
		for (EventPair ep: events){
			map.put(i++ +"", ep.getEventParams().getName() +" " + StringUtils.join(ep.getArgs()," "));
		}
		ConfigurationSection cs = config.createSection("events");
		SerializerUtil.expandMapIntoConfig(cs, map);
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addScheduler(EventScheduler es) {
		this.es = es;
	}
}
