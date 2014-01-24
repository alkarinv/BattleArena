package mc.alk.arena.executors;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MCCommand {
	/// the cmd and all its aliases, can be blank if you want to do something when they just type
	/// the command only
    String[] cmds() default {};

	/// subCommands
    String[] subCmds() default {};

    /// Verify the number of parameters, inGuild and notInGuild imply min if they have an index > number of args
    int min() default 0;
    int max() default Integer.MAX_VALUE;
	int exact() default -1;

    int order() default -1;
    float helpOrder() default Integer.MAX_VALUE;
	boolean admin() default false; /// admin
    boolean op() default false; /// op

    boolean inGame() default false;
    String usage() default "";
    String usageNode() default "";
	String perm() default ""; /// permission node
	int[] alphanum() default {}; /// only alpha numeric

	boolean selection() default false;	/// Selected arena

	int[] arenas() default {};
}