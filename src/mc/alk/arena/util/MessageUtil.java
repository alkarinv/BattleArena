package mc.alk.arena.util;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.serializers.BaseSerializer;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtil extends BaseSerializer {

	public static String colorChat(String msg) {return msg.replaceAll("&", Character.toString((char) 167));}
	public static String decolorChat(String msg) { return ChatColor.stripColor(msg);}


	public static boolean sendMessage(CommandSender p, String message){
		if (message ==null || message.isEmpty()) return true;
		String[] msgs = message.split("\n");
		for (String msg: msgs){
			if (p instanceof Player){
				if (((Player) p).isOnline())
					p.sendMessage(colorChat(msg));			
			} else {
				p.sendMessage(colorChat(msg));
			}	
		}
		return true;
	}

	public static boolean sendMessage(CommandSender p, Collection<String> msgs){
		if (msgs ==null || msgs.isEmpty()) return true;
		for (String msg: msgs){
			if (p instanceof Player){
				if (((Player) p).isOnline())
					p.sendMessage(colorChat(msg));			
			} else {
				p.sendMessage(colorChat(msg));
			}	
		}
		return true;
	}

	public static void sendPlayerMessage(Set<Player> players, String message) {
		final String msg = colorChat(message);
		for (Player p: players){
			p.sendMessage(msg);
		}
	}

	public static String minuteOrMinutes(int minutes) {return minutes == 1 ? "minute" : "minutes";}
	public static String getTeamsOrPlayers(int teamSize) {return teamSize==1 ? "players" : "teams";}
	public static String teamsOrPlayers(int nPlayersPerTeam){return nPlayersPerTeam > 1? "teams" : "players";}
	public static String playerOrPlayers(int n) {return n> 1? "players" : "player";}
	public static String hasOrHave(int size) {return size==1 ? "has" : "have";}


	public static boolean sendMessage(final ArenaPlayer player, final String message) {
		return sendMessage(player.getPlayer(),message);
	}

	public static void sendMessage(Set<ArenaPlayer> players, String message) {
		final String msg = colorChat(message);
		for (ArenaPlayer p: players){
			p.getPlayer().sendMessage(msg);
		}
	}
	
	public static String convertToString(List<String> strs){
		if (strs == null)
			return null;
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String s : strs){
			if (!first) sb.append("\n");
			sb.append(s+"\n");
			first = false;
		}
		return sb.toString();
	}
}
