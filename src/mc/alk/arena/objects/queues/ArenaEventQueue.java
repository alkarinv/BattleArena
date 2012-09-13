package mc.alk.arena.objects.queues;

import java.util.LinkedList;
import java.util.List;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.listeners.TransitionListener;
import mc.alk.arena.objects.EventPair;


public class ArenaEventQueue implements TransitionListener {
	static final boolean DEBUG = false;

	List<EventPair> events = new LinkedList<EventPair>();

	LinkedList<Event> ready_events = new LinkedList<Event>();	
	boolean suspend = false;

	public synchronized Event nextEvent() {
		try{
			if (ready_events.isEmpty())
				wait(30000); /// Technically this could wait forever, but just in case.. check occasionally
			if (!ready_events.isEmpty() && !suspend){
				synchronized(ready_events){
					return ready_events.get(0);
				}
			}
		} catch(InterruptedException e) {
			System.err.println("InterruptedException caught");
		} 
		notify();
		return null;
	}
	
	public void add(EventPair eventPair) {
		synchronized (events){
			events.add(eventPair);			
		}
	}
	public synchronized void stop() {
		suspend = true;
		notifyAll();
	}

	public synchronized void resume() {
		suspend = false;
		notifyAll();
	}
}
