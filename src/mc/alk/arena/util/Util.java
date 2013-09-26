package mc.alk.arena.util;

import mc.alk.arena.Defaults;
import mc.alk.plugin.updater.v1r2.Version;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Util {

	static public void printStackTrace(){
		/// I've left in this accidentally too many times,
		/// make sure DEBUGGING is now on before printing
		if (Defaults.DEBUG_MSGS)
			for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
				System.out.println(ste);}
	}

	static public String getLocString(Location l){
		return l.getWorld().getName() +"," + (int)l.getX() + "," + (int)l.getY() + "," + (int)l.getZ();
	}

	/**
	 * Returns the version of craftbukkit
	 * @return version or 0 if the craftbukkit version is pre 1.4.5
	 */
	public static Version getCraftBukkitVersion(){
		final String pkg = Bukkit.getServer().getClass().getPackage().getName();
		String version = pkg.substring(pkg.lastIndexOf('.') + 1);
		if (version.equalsIgnoreCase("craftbukkit")){
			return new Version("0");
		}
		return new Version(version);
	}
}
