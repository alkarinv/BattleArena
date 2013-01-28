package mc.alk.arena.util;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NotifierUtil {
	public static Map<String,Set<String>> listeners = new ConcurrentHashMap<String,Set<String>>();

	public static void notify(String type, String msg) {
		Set<String> players = listeners.get(type);
		if (players == null)
			return;
		for (String name: players){
			Player p = Bukkit.getPlayerExact(name);
			if (p== null || !p.isOnline())
				continue;
			MessageUtil.sendMessage(p, msg);
		}
	}

	public static void addListener(Player player, String type) {
		Set<String> players = listeners.get(type);
		if (players == null){
			players = new CopyOnWriteArraySet<String>();
			listeners.put(type, players);
		}
		players.add(player.getName());
	}

	public static void removeListener(Player player, String type) {
		Set<String> players = listeners.get(type);
		if (players != null){
			players.remove(player.getName());
		}
		if (players.isEmpty())
			listeners.remove(type);
	}

}
