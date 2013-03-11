package mc.alk.arena.util;

import org.bukkit.Location;

public class Util {

	static public void printStackTrace(){
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			System.out.println(ste);}
	}

	static public String getLocString(Location l){
		return l.getWorld().getName() +"," + (int)l.getX() + "," + (int)l.getY() + "," + (int)l.getZ();
	}

}
