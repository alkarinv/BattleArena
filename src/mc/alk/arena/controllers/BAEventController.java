package mc.alk.arena.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.events.Event;
import mc.alk.arena.competition.events.ReservedArenaEvent;
import mc.alk.arena.events.events.EventFinishedEvent;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.EventState;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.exceptions.InvalidEventException;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class BAEventController implements Listener{
	/// A map of all of our events
	private Map<String, Map<EventState,List<Event>>> allEvents =
			Collections.synchronizedMap(new HashMap<String,Map<EventState,List<Event>>>());

	public BAEventController(){
		Bukkit.getPluginManager().registerEvents(this, BattleArena.getSelf());
	}
	public static class SizeEventPair{
		public Integer nEvents = 0;
		public Event event = null;
	}

	public SizeEventPair getUniqueEvent(EventParams eventParams) {
		final String key = getKey(eventParams);
		Map<EventState,List<Event>> events = allEvents.get(key);
		SizeEventPair result = new SizeEventPair();
		if (events == null || events.isEmpty())
			return result;
		result.nEvents = 0;
		Event event = null;
		for (List<Event> list: events.values()){
			result.nEvents += list.size();
			for (Event evt: list){
				if (evt != null){
					if (event != null){
						result.event = null;
						return result;
					}
					event = evt;
					result.event = evt;
				}
			}
		}
		return result;
	}

	public Event getEvent(ArenaPlayer p) {
		/// Really??? I need a triply nested loop??  maybe ArenaPlayers can have a sense of which event has them...
		for (Map<EventState,List<Event>> map : allEvents.values()){
			for (List<Event> list: map.values()){
				for (Event event: list){
					if (event.hasPlayer(p)){
						return event;}
				}
			}
		}
		return null;
	}

	public boolean hasOpenEvent() {
		for (Map<EventState, List<Event>> map : allEvents.values()){
			for (EventState es: map.keySet()){
				switch (es){
				case CLOSED:
				case FINISHED:
					continue;
				case OPEN:
				case RUNNING:
				default:
					if (!map.get(es).isEmpty())
						return true;
					break;
				}
			}
		}
		return false;
	}

	public boolean hasOpenEvent(EventParams eventParam) {
		final String key = getKey(eventParam);
		Map<EventState,List<Event>> events = allEvents.get(key);
		if (events == null)
			return false;
		return events.get(EventState.OPEN) != null;
	}

	private String getKey(final Event event){
		return getKey(event.getParams());
	}

	private String getKey(final EventParams eventParams){
		return eventParams.getCommand().toUpperCase();
	}

	public void addOpenEvent(Event event) throws InvalidEventException {
		final String key = getKey(event);
		Map<EventState, List<Event>> map = allEvents.get(key);
		if (map == null){
			map = Collections.synchronizedMap(new EnumMap<EventState,List<Event>>(EventState.class));
			allEvents.put(key, map);
		}
		List<Event> events = map.get(EventState.OPEN);
		if (events == null){
			events = Collections.synchronizedList(new ArrayList<Event>());
			map.put(EventState.OPEN, events);
		}
		if (!events.isEmpty()){
			throw new InvalidEventException("There is already an open event of this type!");}
		events.add(event);
	}

	public Event getOpenEvent(EventParams eventParams) {
		final String key = getKey(eventParams);
		Map<EventState,List<Event>> events = allEvents.get(key);
		if (events == null)
			return null;
		List<Event> es = events.get(EventState.OPEN);
		return (es != null && !es.isEmpty()) ? es.get(0) : null;
	}

	public void startEvent(Event event) throws Exception {
		if (event.getState() != EventState.OPEN)
			throw new Exception("Event was not open!");
		final String key = getKey(event);
		Event evt = getOpenEvent(event.getParams());
		if (evt != event){
			throw new Exception("Trying to start the wrong open event!");}
		Map<EventState, List<Event>> map = allEvents.get(key);
		if (map == null){
			map = Collections.synchronizedMap(new EnumMap<EventState,List<Event>>(EventState.class));
			allEvents.put(key, map);
		}
		/// Remove the open event
		List<Event> events = map.get(EventState.OPEN);
		events.remove(event);
		/// Add to running events and start
		events = map.get(EventState.RUNNING);
		if (events == null){
			events = Collections.synchronizedList(new ArrayList<Event>());
			map.put(EventState.RUNNING, events);
		}
		events.add(event);
		event.startEvent();
//		System.out.println("************* startEvent = " + event +"   " + events.size() +"  " + key);
	}

	public Map<EventState,List<Event>> getCurrentEvents(EventParams eventParams) {
		final String key = getKey(eventParams);
		Map<EventState,List<Event>> events = allEvents.get(key);
		return events != null ? new EnumMap<EventState,List<Event>>(events) : null;
	}

	public Event getEvent(Arena arena) {
		for (Map<EventState,List<Event>> map : allEvents.values()){
			for (List<Event> list: map.values()){
				for (Event event: list){
					if (event instanceof ReservedArenaEvent && ((ReservedArenaEvent)event).getArena().equals(arena)){
						return event;
					}
				}
			}
		}
		return null;
	}

	public boolean removeEvent(Event event){
		for (Map<EventState,List<Event>> map : allEvents.values()){
			for (List<Event> list: map.values()){
				Iterator<Event> iter = list.iterator();
				while(iter.hasNext()){
					Event evt = iter.next();
					if (evt.equals(event)){
						iter.remove();}
				}
			}
		}
		return false;
	}

	public boolean cancelEvent(Event event) {
		event.cancelEvent();
		return removeEvent(event);
	}

	@EventHandler
	public void onEventFinished(EventFinishedEvent event){
		removeEvent(event.getEvent());
	}


}
