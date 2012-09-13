package mc.alk.arena.objects.events;

import java.lang.reflect.Method;

import mc.alk.arena.events.BAEvent;

import org.bukkit.event.Event;

public class TransitionMethod implements Comparable<TransitionMethod>{
	final Method callMethod;
	final Class<? extends BAEvent> transitionEvent;
	final EventPriority priority;

	public TransitionMethod(Method callMethod, Class<? extends BAEvent> event, EventPriority priority) {
		this.callMethod = callMethod;
		this.transitionEvent = event;
		this.priority = priority;
	}
	public Method getMethod(){
		return callMethod;
	}

	public Class<? extends Event> getBukkitEvent(){
		return transitionEvent;
	}
	
	@Override
	public int compareTo(TransitionMethod arg0) {
		return this.priority.compareTo(arg0.priority);
	}
	
	public String toString(){
		return "[MTM "+callMethod.getName()+", " + transitionEvent+ "]";
	}

}
