package mc.alk.arena.controllers;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.CraftServer;

import com.alk.util.Log;

public class CommandController {

	public static CommandMap getCommandMap(){
		try {
			if (Bukkit.getServer() instanceof CraftServer) {
				final Field f = CraftServer.class.getDeclaredField("commandMap");
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
