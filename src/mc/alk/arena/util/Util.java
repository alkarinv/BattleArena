package mc.alk.arena.util;

import java.io.File;
import java.util.Collection;

import mc.alk.arena.Defaults;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.ArenaPlayer;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
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
	
	public static String playersToCommaDelimitedString(Collection<ArenaPlayer> players){
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (ArenaPlayer p : players){
			if (!first) sb.append(", ");
			else first = false;
			sb.append(p.getName());
		}
		return sb.toString();		
	}

	public static String toCommaDelimitedString(Collection<String> players){
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String p: players){
			if (!first) sb.append(", ");
			else first = false;
			sb.append(p);
		}
		return sb.toString();		
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
	public static OfflinePlayer findOfflinePlayer(String name) {
		OfflinePlayer p = findPlayer(name);
		if (p != null){
			return p;
		} else{
			/// Iterate over the worlds to see if a player.dat file exists
			for (World w : Bukkit.getWorlds()){
				File f = new File(w.getName()+"/players/"+name+".dat");
				if (f.exists()){
					return Bukkit.getOfflinePlayer(name);
				}
			}
			return null;
		}
	}

	public static Player[] getOnlinePlayers() {
		
		if (Defaults.DEBUG_VIRTUAL){
			Player[] online = VirtualPlayers.getOnlinePlayers();
			Player[] realonline = Bukkit.getOnlinePlayers();
			return ArrayUtils.addAll(online,realonline);
		} else {
			return Bukkit.getOnlinePlayers();
		}
	}

}
