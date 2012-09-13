package mc.alk.arena.objects;

import mc.alk.arena.competition.events.Event;

public class EventPair{
	Event event;
	String[] args;
	public EventPair(Event event, String[] args) {
		this.event = event;
		this.args = args;
	}

	public Event getEvent() {
		return event;
	}
	public void setEvent(Event event) {
		this.event = event;
	}
	public String[] getArgs() {
		return args;
	}
	public void setArgs(String[] args) {
		this.args = args;
	}
}
