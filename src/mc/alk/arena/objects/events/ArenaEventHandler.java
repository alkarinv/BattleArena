package mc.alk.arena.objects.events;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import mc.alk.arena.objects.MatchState;


@Retention(RetentionPolicy.RUNTIME)
public @interface ArenaEventHandler {
	MatchState begin() default MatchState.NONE;
	MatchState end() default MatchState.NONE;
	EventPriority priority() default EventPriority.NORMAL;
	boolean needsPlayer() default true;
	String entityMethod() default "";
	boolean suppressCastWarnings() default false;
	org.bukkit.event.EventPriority bukkitPriority() default org.bukkit.event.EventPriority.HIGHEST;
}