package mc.alk.arena.objects;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(RetentionPolicy.RUNTIME)
public @interface MatchEventHandler {
	MatchState begin() default MatchState.NONE;
	MatchState end() default MatchState.NONE;
	MatchEventPriority priority() default MatchEventPriority.NORMAL;
	boolean needsPlayer() default true;
}