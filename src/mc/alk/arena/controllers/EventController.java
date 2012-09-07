package mc.alk.arena.controllers;

import java.util.HashMap;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.teams.Team;


public class EventController {
	static HashMap<String, Event> registeredEvents = new HashMap<String,Event>();

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
	}

	public void cancelAll() {
		for (Event evt : registeredEvents.values()){
			if (evt.isClosed())
				continue;
			evt.cancelEvent();
		}
	}
}
