package mc.alk.arena.controllers;

import mc.alk.arena.objects.regions.PylamoRegion;
import mc.alk.arena.util.PylamoUtil;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class PylamoController {
	static boolean enabled = false;
	public static boolean enabled() {
		return enabled;
	}

	public static void setPylamo(Plugin plugin) {
		PylamoUtil.setPylamo(plugin);
		enabled = true;
	}

	public static void createRegion(String id, Location minimumPoint, Location maximumPoint) {
		PylamoUtil.createRegion(id,minimumPoint,maximumPoint);
	}

	public static void resetRegion(PylamoRegion pylamoRegion) {
		PylamoUtil.resetRegion(pylamoRegion);
	}

}
