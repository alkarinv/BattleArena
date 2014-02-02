package mc.alk.arena.objects.events;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchState;
import org.bukkit.event.Event;

import java.lang.reflect.Method;

public class ArenaEventMethod {
	final Method callMethod;
	final Class<? extends Event> bukkitEvent;
	final Method getPlayerMethod;
	final MatchState beginState, endState;
	final EventPriority priority;
	final org.bukkit.event.EventPriority bukkitPriority;
	final boolean specificArenaPlayer;
    final boolean isBAEvent; /// Whether this is a BAevent or a normal bukkit event

	public ArenaEventMethod(Method callMethod, Class<? extends Event> event,
			MatchState begin, MatchState end, MatchState cancel, EventPriority priority,
			org.bukkit.event.EventPriority bukkitPriority, boolean isBAEvent) {
		this(callMethod,event,null,begin,end,cancel,priority, bukkitPriority,isBAEvent);
	}

	public ArenaEventMethod(Method callMethod, Class<? extends Event> event,Method getPlayerMethod,
			MatchState begin, MatchState end, MatchState cancel, EventPriority priority,
			org.bukkit.event.EventPriority bukkitPriority, boolean isBAEvent) {
		this.callMethod = callMethod;
		this.bukkitEvent = event;
		this.getPlayerMethod = getPlayerMethod;
		this.beginState = begin;
		this.endState = end;
		this.priority = priority;
		this.bukkitPriority = bukkitPriority;
		this.specificArenaPlayer = 	getPlayerMethod != null &&
				ArenaPlayer.class.isAssignableFrom(getPlayerMethod().getReturnType());
        this.isBAEvent = isBAEvent;
    }

	public boolean isSpecificPlayerMethod() {
		return getPlayerMethod != null;
	}

	public boolean isSpecificArenaPlayerMethod() {
		return this.specificArenaPlayer;
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

	public Class<? extends Event> getBAEvent(){
		return bukkitEvent;
	}

	public MatchState getBeginState() {
		return beginState;
	}

	public MatchState getEndState() {
		return endState;
	}

    public boolean isBAEvent() {
        return isBAEvent;
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
