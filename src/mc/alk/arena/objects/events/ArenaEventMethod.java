package mc.alk.arena.objects.events;

import java.lang.reflect.Method;

import mc.alk.arena.objects.MatchState;

import org.bukkit.event.Event;

public class ArenaEventMethod {
	final Method callMethod;
	final Class<? extends Event> bukkitEvent;
	final Method getPlayerMethod;
	final MatchState beginState, endState, cancelState;
	final EventPriority priority;
	final org.bukkit.event.EventPriority bukkitPriority;

	public ArenaEventMethod(Method callMethod, Class<? extends Event> event,
			MatchState begin, MatchState end, MatchState cancel, EventPriority priority,
			org.bukkit.event.EventPriority bukkitPriority) {
		this(callMethod,event,null,begin,end,cancel,priority, bukkitPriority);
	}

	public ArenaEventMethod(Method callMethod, Class<? extends Event> event,Method getPlayerMethod,
			MatchState begin, MatchState end, MatchState cancel, EventPriority priority,
			org.bukkit.event.EventPriority bukkitPriority) {
		this.callMethod = callMethod;
		this.bukkitEvent = event;
		this.getPlayerMethod = getPlayerMethod;
		this.beginState = begin;
		this.endState = end;
		this.cancelState = cancel;
		this.priority = priority;
		this.bukkitPriority = bukkitPriority;
	}


	public boolean isSpecificPlayerMethod() {
		return getPlayerMethod != null;
	}

	public EventPriority getPriority(){
		return priority;
	}

	public Method getMethod(){
		return callMethod;
	}

	public Method getPlayerMethod(){
		return getPlayerMethod;
	}

	public Class<? extends Event> getBukkitEvent(){
		return bukkitEvent;
	}

	public MatchState getBeginState() {
		return beginState;
	}

	public MatchState getEndState() {
		return endState;
	}

	public MatchState getCancelState() {
		return cancelState;
	}

	@Override
	public String toString(){
		return "[MEM "+callMethod.getName()+", " + (bukkitEvent != null ? bukkitEvent.getSimpleName():"null")+
				" p="+bukkitPriority+" "  + beginState+":"+endState+"   playerMethod=" + getPlayerMethod+"]";
	}

	public org.bukkit.event.EventPriority getBukkitPriority() {
		return bukkitPriority;
	}

}
