package mc.alk.arena.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mc.alk.arena.objects.ArenaPlayer;

import org.bukkit.entity.Player;

public class PlayerController {
	private static HashMap<String,ArenaPlayer> players = new HashMap<String,ArenaPlayer>();

	public static ArenaPlayer toArenaPlayer(Player player){
		ArenaPlayer ap = players.get(player.getName());
		if (ap == null){
			ap = new ArenaPlayer(player);
			players.put(player.getName(), ap);
		} else {
			ap.setPlayer(player);
		}
		return ap;
	}

	public static List<ArenaPlayer> toArenaPlayerList(Collection<Player> players){
		List<ArenaPlayer> aplayers = new ArrayList<ArenaPlayer>(players.size());
		for (Player p: players)
			aplayers.add(toArenaPlayer(p));
		return aplayers;
	}
	
	public static Set<ArenaPlayer> toArenaPlayerSet(Collection<Player> players){
		Set<ArenaPlayer> aplayers = new HashSet<ArenaPlayer>(players.size());
		for (Player p: players)
			aplayers.add(toArenaPlayer(p));
		return aplayers;
	}

	public static Set<Player> toPlayerSet(Collection<ArenaPlayer> arenaPlayers) {
		Set<Player> players = new HashSet<Player>(arenaPlayers.size());
		for (ArenaPlayer ap: arenaPlayers)
			players.add(ap.getPlayer());
		return players;
	}

	public static List<Player> toPlayerList(Collection<ArenaPlayer> arenaPlayers) {
		List<Player> players = new ArrayList<Player>(arenaPlayers.size());
		for (ArenaPlayer ap: arenaPlayers)
			players.add(ap.getPlayer());
		return players;
	}

//	public static Collection<Player> getAllBukkitPlayers() {
//		List<Player> ps = new ArrayList<Player>(players.size());
//		for (ArenaPlayer ap: players.values())
//			ps.add(ap.getPlayer());
//		return null;
//	}

}
