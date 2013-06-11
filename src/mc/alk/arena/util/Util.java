package mc.alk.arena.util;

import mc.alk.arena.Defaults;

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

}
