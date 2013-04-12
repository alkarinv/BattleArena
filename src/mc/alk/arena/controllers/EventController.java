package mc.alk.arena.controllers;

import java.util.HashMap;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.executors.EventExecutor;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.teams.ArenaTeam;


public class EventController {
	static HashMap<String, Event> registeredEvents = new HashMap<String,Event>();
	static HashMap<String, EventExecutor> registeredExecutors = new HashMap<String,EventExecutor>();

	public EventController(){}

	public static Event insideEvent(ArenaPlayer p) {
		for (Event evt : registeredEvents.values()){
			ArenaTeam t = evt.getTeam(p);
			if (t != null)
				return evt;
		}
		return null;
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

	public static void addEventExecutor(EventParams eventParams, EventExecutor executor) {
		registeredExecutors.put(eventParams.getName().toLowerCase(), executor);
		registeredExecutors.put(eventParams.getCommand().toLowerCase(),executor);
	}

	public static EventExecutor getEventExecutor(Event event){
		return registeredExecutors.get(event.getName().toLowerCase());
	}

	public static EventExecutor getEventExecutor(String eventType){
		return registeredExecutors.get(eventType.toLowerCase());
	}

	public static boolean isEventType(String name) {
		return ParamController.getEventParamCopy(name) != null;
	}
}
