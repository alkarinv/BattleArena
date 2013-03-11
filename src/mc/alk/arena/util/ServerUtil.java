package mc.alk.arena.util;

import java.io.File;
import java.util.Set;

import mc.alk.arena.Defaults;
import mc.alk.virtualPlayer.VirtualPlayers;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;


public class ServerUtil {

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
		Player foundPlayer = Bukkit.getPlayer(name);
		if (foundPlayer != null)
			return foundPlayer;

		Player[] online = Bukkit.getOnlinePlayers();
		if (Defaults.DEBUG_VIRTUAL){online = VirtualPlayers.getOnlinePlayers();}

		for (Player player : online) {
			String playerName = player.getName();

			if (playerName.equalsIgnoreCase(name)) {
				foundPlayer = player;
				break;
			}

			if (playerName.toLowerCase().indexOf(name.toLowerCase()) != -1) {
				if (foundPlayer != null) {
					return null;}

				foundPlayer = player;
			}
		}

		return foundPlayer;
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
			return (Player[]) ArrayUtils.addAll(online,realonline);
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
