package mc.alk.arena.util;

import java.io.File;
import java.util.Set;

import mc.alk.arena.Defaults;
import mc.alk.arena.objects.ArenaParams;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.alk.virtualPlayer.VirtualPlayers;

public class Util {
	public static class MinMax {
		public int min; public int max;
		public MinMax(int min, int max){
			this.min = min; this.max = max;
		}
		@Override
		public String toString(){return getStr(min,max);}
		public boolean contains(int i) {
			return min <= i && max >= i;
		}

		public static MinMax valueOf(String s) throws NumberFormatException{
			if (s == null) throw new NumberFormatException("Number can not be null");
			if (s.contains("+")){
				s = s.replaceAll("\\+", "");
				Integer i = Integer.valueOf(s);
				return new MinMax(i,ArenaParams.MAX);
			}
			if (s.contains("-")){
				String[] vals = s.split("-");
				int i = Integer.valueOf(vals[0]);
				int j = Integer.valueOf(vals[1]);
				return new MinMax(i,j);
			}

			Integer i = null;
			if (s.contains("v")){
				i = Integer.valueOf(s.split("v")[0]);
			} else {
				i = Integer.valueOf(s);
			}
			return new MinMax(i,i);
		}
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

	public static Player findPlayerExact(String name) {
		if (name == null)
			return null;
		Player lastPlayer = Bukkit.getPlayerExact(name);
		if (lastPlayer != null)
			return lastPlayer;
		if (Defaults.DEBUG_VIRTUAL){return VirtualPlayers.getPlayer(name);}
		return null;
	}

	public static Player findPlayer(String name) {
		if (name == null)
			return null;
		Player lastPlayer = Bukkit.getPlayer(name);
		if (lastPlayer != null)
			return lastPlayer;

		Player[] online = Bukkit.getOnlinePlayers();
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

	public static void findOnlinePlayers(Set<String> names, Set<Player> foundplayers, Set<String> unfoundplayers) {
		Player[] online = getOnlinePlayers();
		for (String name : names){
			Player lastPlayer = null;
			for (Player player : online) {
				String playerName = player.getName();
				if (playerName.equalsIgnoreCase(name)) {
					lastPlayer = player;
					break;
				}

				if (playerName.toLowerCase().indexOf(name.toLowerCase()) != -1) { /// many names match the one given
					if (lastPlayer != null) {
						lastPlayer = null;
						break;
					}
					lastPlayer = player;
				}
			}
			if (lastPlayer != null){
				foundplayers.add(lastPlayer);
			} else{
				unfoundplayers.add(name);
			}
		}
	}


}
