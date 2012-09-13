package mc.alk.arena.objects;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import mc.alk.arena.objects.events.EventPriority;


@Retention(RetentionPolicy.RUNTIME)
public @interface MatchTransitionHandler {
	EventPriority priority() default EventPriority.NORMAL;
}