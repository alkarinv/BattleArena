package mc.alk.arena.controllers;

import java.util.HashMap;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.executors.EventExecutor;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.Team;


public class EventController {
	static HashMap<String, Event> registeredEvents = new HashMap<String,Event>();
	static HashMap<String, EventExecutor> registeredExecutors = new HashMap<String,EventExecutor>();

	public EventController(){}
	
	public static Event insideEvent(ArenaPlayer p) {
		for (Event evt : registeredEvents.values()){
			Team t = evt.getTeam(p);
			if (t != null)
				return evt;
		}
		return null;
	}

	public static Event getEvent(String name) {
		return registeredEvents.get(name.toLowerCase());
	}
	
	public static void addEvent(Event event){
		registeredEvents.put(event.getName().toLowerCase(),event);
		registeredEvents.put(event.getCommand().toLowerCase(),event);
	}

	public void cancelAll() {
		for (Event evt : registeredEvents.values()){
			if (evt.isClosed())
				continue;
			evt.cancelEvent();
		}
	}

	public static void addEventExecutor(Event event, EventExecutor executor) {
		registeredExecutors.put(event.getName().toLowerCase(), executor);
		registeredExecutors.put(event.getCommand().toLowerCase(),executor);

	}
	
	public static EventExecutor getEventExecutor(Event event){
		return registeredExecutors.get(event.getName().toLowerCase());
	}
}
