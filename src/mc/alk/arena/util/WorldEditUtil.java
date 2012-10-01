package mc.alk.arena.util;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

/**
 * Stub class for future expansion
 * @author alkarin
 *
 */
public class WorldEditUtil {	
	public static WorldEditPlugin wep;
	public static boolean hasWorldEdit = false;
	
	public static class WorldGuardException extends Exception{
		private static final long serialVersionUID = 1L;
		public WorldGuardException(String msg) {
			super(msg);
		}
	}
	
	public static boolean hasWorldEdit() {
		return hasWorldEdit;
	}

	public static boolean setWorldEdit(Plugin plugin) {
		wep = (WorldEditPlugin) plugin;
		hasWorldEdit = true;
		return false;
	}
	
	public static Selection getSelection(Player player) {
		return wep.getSelection(player);
	}

	public static WorldEditPlugin getWorldEditPlugin() {
		return wep;
	}



}
