package mc.alk.arena.controllers;

import mc.alk.arena.util.Log;
import mc.alk.arena.util.Util;
import mc.alk.plugin.updater.v1r6.Version;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.lang.reflect.Field;

public class CommandController {

	public static CommandMap getCommandMap(){
		Version version = Util.getCraftBukkitVersion();
		final Class<?> clazz;
		try {
			if (version.compareTo("0") == 0 || version.getVersion().equalsIgnoreCase("craftbukkit")){
				clazz = Class.forName("org.bukkit.craftbukkit.CraftServer");
			} else{
				clazz = Class.forName("org.bukkit.craftbukkit." + version.getVersion() + ".CraftServer");
			}
		} catch (ClassNotFoundException e) {
			Log.printStackTrace(e);
			return null;
		}
		return getCommandMapFromServer(clazz);
	}

	private static CommandMap getCommandMapFromServer(Class<?> serverClass){
		try {
			if (serverClass.isAssignableFrom(Bukkit.getServer().getClass())) {
				final Field f = serverClass.getDeclaredField("commandMap");
				f.setAccessible(true);
				return (CommandMap) f.get(Bukkit.getServer());
			}
		} catch (final SecurityException e) {
			Log.err("You will need to disable the security manager to use dynamic commands");
		} catch (final Exception e) {
			Log.printStackTrace(e);
		}
		return null;
	}

	public static void registerCommand(final Command command) {
		CommandMap commandMap = getCommandMap();
		if (commandMap != null){
			commandMap.register("/", command);
		}
	}
}
