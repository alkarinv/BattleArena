package mc.alk.arena.controllers;

import mc.alk.arena.Defaults;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.util.PlayerUtil;
import mc.alk.arena.util.ServerUtil;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class PlayerController {
	private static HashMap<UUID,ArenaPlayer> players = new HashMap<UUID,ArenaPlayer>();

	/**
	 * wrap a player into an ArenaPlayer
	 * @param player Bukkit player
	 * @return ArenaPlayer
	 */
	public static ArenaPlayer toArenaPlayer(Player player){
		ArenaPlayer ap = players.get(PlayerUtil.getID(player));
		if (Defaults.DEBUG_VIRTUAL) {
			Player p2 = ServerUtil.findPlayerExact(player.getName());
			if (p2 != null)
				player = p2;
		}
		if (ap == null){
			ap = new ArenaPlayer(player);
			players.put(ap.getID(), ap);
		} else{
			ap.setPlayer(player);
		}
		return ap;
	}
    public static ArenaPlayer toArenaPlayer(UUID id){
        ArenaPlayer ap = players.get(id);
        Player player = ServerUtil.findPlayer(id);
        if (Defaults.DEBUG_VIRTUAL && player == null) {
            Player p2 = ServerUtil.findPlayer(id);
            if (p2 != null)
                player = p2;
        }
        if (ap == null){
            ap = player == null ? new ArenaPlayer(id) : new ArenaPlayer(player);
            players.put(ap.getID(), ap);
        } else if (player != null) {
            ap.setPlayer(player);
        }
        return ap;
    }
//
//    public static ArenaPlayer getArenaPlayer(String playerName) {
//        return players.get(playerName);
//    }

	/**
	 * Returns the ArenaPlayer for the given player
	 * @param player Bukkit player
	 * @return player if found, null otherwise
	 */
	public static ArenaPlayer getArenaPlayer(Player player){
		return players.get(PlayerUtil.getID(player));
	}

	public static boolean hasArenaPlayer(Player player){
		return players.containsKey(PlayerUtil.getID(player));
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

    public static List<Player> UUIDToPlayerList(Collection<UUID> uuids) {
        List<Player> players = new ArrayList<Player>(uuids.size());
        for (UUID id : uuids)
            players.add(ServerUtil.findPlayer(id));
        return players;
    }

    public static void clearArenaPlayers(){
		players.clear();
	}
}
