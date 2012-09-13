package mc.alk.arena.serializers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.events.Event;
import mc.alk.arena.controllers.EventController;
import mc.alk.arena.controllers.EventScheduler;
import mc.alk.arena.objects.EventPair;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.SerializerUtil;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;


public class EventScheduleSerializer extends BaseSerializer {
	EventScheduler es;
	public void loadAll(){
		try {config.load(f);} catch (Exception e){e.printStackTrace();}	
		loadScheduledEvents(config.getConfigurationSection("events"));
	}	

	public void loadScheduledEvents(ConfigurationSection cs) {
		if (cs == null){
			Log.info(BattleArena.getPName() +" has no scheduled events");
			return;}
		List<String> keys = new ArrayList<String>(cs.getKeys(false));
		Collections.sort(keys);
		for (String key : keys){
			String se = cs.getString(key);
			if (se == null)
				continue;
			String[] fullargs = se.split(" ");
			Event event = EventController.getEvent(fullargs[0]);
			if (event == null){
				Log.err(BattleArena.getPName()+" couldn't reparse the scheduled event " + fullargs[0]);
				continue;
			}
			String[] args = Arrays.copyOfRange(fullargs, 1, fullargs.length);
			es.scheduleEvent(event, args);
		}
	}
	
	public void saveScheduledEvents(){
		List<EventPair> events = es.getEvents();
		int i = 0;
		Map<String,Object> map = new HashMap<String,Object>();
		for (EventPair ep: events){
			map.put(i++ +"", ep.getEvent().getName() +" " + StringUtils.join(ep.getArgs()," "));
		}
		ConfigurationSection cs = config.createSection("events");
		SerializerUtil.expandMapIntoConfig(cs, map);
		try {
			config.save(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addScheduler(EventScheduler es) {
		this.es = es;
	}
}
