package mc.alk.arena.util;

import mc.alk.arena.objects.regions.PylamoRegion;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import de.pylamo.pylamorestorationsystem.PylamoRestorationSystem;
import de.pylamo.pylamorestorationsystem.Commands.CreateRegionCommand;
import de.pylamo.pylamorestorationsystem.Commands.DeleteRegionCommand;
import de.pylamo.pylamorestorationsystem.Commands.RestoreCommand;

public class PylamoUtil {
	static PylamoRestorationSystem plugin;

	public static boolean setPylamo(Plugin plugin) {
		PylamoUtil.plugin = (PylamoRestorationSystem) plugin;
		return true;
	}

	public static void createRegion(String id, Location minimumPoint, Location maximumPoint) {
		DeleteRegionCommand.deleteRegionCommand(Bukkit.getConsoleSender(), new String[]{"delete", id});
		CreateRegionCommand.createRegion(minimumPoint, maximumPoint, id);
	}

	public static void resetRegion(PylamoRegion pylamoRegion) {
		RestoreCommand.restoreCommand(Bukkit.getConsoleSender(), new String[]{"restore", pylamoRegion.getID()});
	}

}
