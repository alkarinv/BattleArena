package mc.alk.arena.util;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.UserMap;

public class EssentialsUtil {
	static Essentials essentials;

	public static boolean enableEssentials(Plugin plugin) {
		try{
			essentials = (Essentials) plugin;
		} catch(Exception e){
			Log.printStackTrace(e);
			return false;
		}
		return true;
	}

	public static User getUser(String playerName){
		UserMap map = essentials.getUserMap();
		if (map == null)
			return null;
		return map.getUser(playerName);
	}

	public static void setGod(String playerName, boolean enable) {
		User user = getUser(playerName);
		if (user != null && user.isGodModeEnabled() != enable){
			user.setGodModeEnabled(enable);}
	}

	public static void setFlight(String playerName, boolean enable) {
		User user = getUser(playerName);
		if (user != null && user.isFlying() != enable){
			user.setFlying(enable);}
	}

	public static void setFlightSpeed(String playerName, Float flightSpeed) {
		User user = getUser(playerName);
		if (user != null){
			user.setFlySpeed(flightSpeed);}
	}

	public static boolean inJail(String playerName) {
		try{
			User user = getUser(playerName);
			return user.getJailTimeout() > System.currentTimeMillis();
		} catch(Exception e){
			Log.printStackTrace(e);
			return false;
		}
	}

	public static Boolean isGod(String playerName) {
		try{
			User user = getUser(playerName);
			return user.isGodModeEnabled();
		} catch(Exception e){
			Log.printStackTrace(e);
			return false;
		}
	}

	public static Boolean isFlying(String playerName) {
		try{
			User user = getUser(playerName);
			return user.isFlying();
		} catch(Exception e){
			Log.printStackTrace(e);
			return false;
		}
	}

	public static void setBackLocation(String playerName, Location loc){
		try{
			User user = getUser(playerName);
			user.setLastLocation(loc);
		} catch(Exception e){
			Log.printStackTrace(e);
		}
	}

	public static Location getBackLocation(String playerName){
		try{
			User user = getUser(playerName);
			return user.getLastLocation();
		} catch(Exception e){
			Log.printStackTrace(e);
			return null;
		}
	}
}
