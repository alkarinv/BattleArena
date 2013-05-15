package mc.alk.arena.objects.events;

import java.lang.reflect.Method;

import mc.alk.arena.events.BAEvent;

import org.bukkit.event.Event;

public class TransitionMethod {
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
	public String toString(){
		return "[MTM "+callMethod.getName()+", " + transitionEvent+ "]";
	}

}
