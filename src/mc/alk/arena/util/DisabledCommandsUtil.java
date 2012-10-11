package mc.alk.arena.util;

import java.util.Collection;
import java.util.HashSet;

public class DisabledCommandsUtil {
	static HashSet<String> disabled = new HashSet<String>();

	public static boolean contains(String cmd) {
		return disabled.contains(cmd);
	}
	
	public static void addDisabledCommand(String cmd){
		disabled.add(cmd);
	}

	public static void addAll(Collection<String> disabledCommands) {
		if (disabled == null)
			return;
		for (String s: disabledCommands){
			disabled.add("/" + s);
		}
	}
}
