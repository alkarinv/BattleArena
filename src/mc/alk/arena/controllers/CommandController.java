package mc.alk.arena.controllers;

import java.lang.reflect.Field;

import mc.alk.arena.util.Log;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

public class CommandController {

	public static CommandMap getCommandMap(){
		final String pkg = Bukkit.getServer().getClass().getPackage().getName();
		String version = pkg.substring(pkg.lastIndexOf('.') + 1);
		final Class<?> clazz;
		try {
			if (version.equalsIgnoreCase("craftbukkit")){
				clazz = Class.forName("org.bukkit.craftbukkit.CraftServer");
			} else{
				clazz = Class.forName("org.bukkit.craftbukkit." + version + ".CraftServer");
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
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
			e.printStackTrace();
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
