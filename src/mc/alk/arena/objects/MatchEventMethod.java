package mc.alk.arena.objects;

import java.lang.reflect.Method;

import org.bukkit.event.Event;

public class MatchEventMethod implements Comparable<MatchEventMethod>{
	final Method callMethod;
	final Class<? extends Event> bukkitEvent;
	final Method getPlayerMethod;
	final MatchState beginState, endState, cancelState;
	final MatchEventPriority priority;
	public MatchEventMethod(Method callMethod, Class<? extends Event> event, 
			MatchState begin, MatchState end, MatchState cancel, MatchEventPriority priority) {
		this.callMethod = callMethod;
		this.bukkitEvent = event;
		this.getPlayerMethod = null;
		this.beginState = begin;
		this.endState = end;
		this.cancelState = cancel;
		this.priority = priority;
	}
	public MatchEventMethod(Method callMethod, Class<? extends Event> event, 
			Method getPlayerMethod, MatchState begin, MatchState end, MatchState cancel, MatchEventPriority priority) {
		this.callMethod = callMethod;
		this.bukkitEvent = event;
		this.getPlayerMethod = getPlayerMethod;
		this.beginState = begin;
		this.endState = end;
		this.cancelState = cancel;
		this.priority = priority;
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
	public String toString(){
		return "[MEM "+callMethod.getName()+", " + bukkitEvent+ " "  + beginState+":"+endState+"   playerMethod=" + getPlayerMethod+"]";
	}
	
	@Override
	public int compareTo(MatchEventMethod arg0) {
		return this.priority.compareTo(arg0.priority);
	}
}
