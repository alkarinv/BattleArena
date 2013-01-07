package mc.alk.arena.util;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.bukkit.BukkitCommandSender;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

/**
 * Stub class for future expansion
 * @author alkarin
 *
 */
public class WorldEditUtil {
	public static WorldEditPlugin wep;

	public static boolean hasWorldEdit() {
		return wep != null;
	}

	public static boolean setWorldEdit(Plugin plugin) {
		wep = (WorldEditPlugin) plugin;
		return true;
	}

	public static Selection getSelection(Player player) {
		return wep.getSelection(player);
	}

	public static WorldEditPlugin getWorldEditPlugin() {
		return wep;
	}

	public class ConsolePlayer extends BukkitCommandSender {
		LocalWorld world;
		public ConsolePlayer(WorldEditPlugin plugin, ServerInterface server,CommandSender sender, World w) {
			super(plugin, server, sender);
			world = BukkitUtil.getLocalWorld(w);
		}

		@Override
		public boolean isPlayer() {
			return true;
		}
		@Override
		public LocalWorld getWorld() {
			return world;
		}
	}
}
