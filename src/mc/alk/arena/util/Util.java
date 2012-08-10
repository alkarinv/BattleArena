package mc.alk.arena.util;

import java.util.Collection;

import mc.alk.arena.Defaults;
import mc.alk.arena.objects.ArenaParams;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.alk.virtualPlayer.VirtualPlayers;

public class Util {
	public static class MinMax {
		public int min; public int max;
		public MinMax(int min, int max){
			this.min = min; this.max = max;
		}
		public String toString(){return "[MM "+min+":" + max+"]";}
	}
	
	static public void printStackTrace(){
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			System.out.println(ste);
		}
	}
	static public String getLocString(Location l){
		return l.getWorld().getName() +"," + (int)l.getX() + "," + (int)l.getY() + "," + (int)l.getZ();
	}

	
	public static String getStr(int min, int max){
		if (min==max)
			return min+"";
		if (max == ArenaParams.MAX){
			return min+"+";}
		return min+"-"+max;
	}

	public static MinMax getMinMax(String s){
		if (s == null) return null;
		if (s.contains("+")){
			s = s.replaceAll("\\+", "");
			try {
				Integer i = Integer.valueOf(s);
				return new MinMax(i,ArenaParams.MAX);
			} catch (Exception e){
				return null;	
			}
		}
		if (s.contains("-")){
			String[] vals = s.split("-");
			int i = Integer.valueOf(vals[0]);
			int j = Integer.valueOf(vals[1]);
			return new MinMax(i,j);
		}
		
		try {
			Integer i = null;
			if (s.contains("v")){
				i = Integer.valueOf(s.split("v")[0]);				
			} else {
				i = Integer.valueOf(s);
			}
			return new MinMax(i,i);			
		} catch (Exception e){
			return null;	
		}
	}
	
	public static Player findPlayer(String name) {
		if (name == null)
			return null;
		Server server =Bukkit.getServer();
		Player lastPlayer = server.getPlayer(name);
		if (lastPlayer != null) 
			return lastPlayer;

		Player[] online = server.getOnlinePlayers();
		if (Defaults.DEBUG_VIRTUAL){online = VirtualPlayers.getOnlinePlayers();}

		for (Player player : online) {
			String playerName = player.getName();

			if (playerName.equalsIgnoreCase(name)) {
				lastPlayer = player;
				break;
			}

			if (playerName.toLowerCase().indexOf(name.toLowerCase()) != -1) {
				if (lastPlayer != null) {
					return null;}

				lastPlayer = player;
			}
		}

		return lastPlayer;
	}
	
	public static String getColor(String str) {
		int index = str.indexOf("&");
		if (index == -1){
			return ChatColor.WHITE+"";
		}
		if (index < str.length()-1){
			return str.substring(index,index+2);
		}
		return ChatColor.WHITE+"";
	}
	
	public static String toCommaDelimitedString(Collection<Player> players){
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Player p : players){
			if (!first) sb.append(", ");
			else first = false;
			sb.append(p.getName());
		}
		return sb.toString();		
	}

}
