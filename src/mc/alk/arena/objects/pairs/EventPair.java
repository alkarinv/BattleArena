package mc.alk.arena.objects.pairs;

import mc.alk.arena.objects.EventParams;


public class EventPair{
	EventParams eventParams;
	String[] args;
	public EventPair(EventParams event, String[] args) {
		this.eventParams = event;
		this.args = args;
	}

	public EventParams getEventParams() {
		return eventParams;
	}
	public void setEventParams(EventParams eventParams) {
		this.eventParams = eventParams;
	}
	public String[] getArgs() {
		return args;
	}
	public void setArgs(String[] args) {
		this.args = args;
	}
}
