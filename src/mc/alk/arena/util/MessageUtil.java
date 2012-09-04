package mc.alk.arena.util;

import java.io.File;
import java.util.Formatter;
import java.util.List;
import java.util.Set;

import mc.alk.arena.Defaults;
import mc.alk.arena.objects.ArenaPlayer;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class MessageUtil {

	private static YamlConfiguration mc = new YamlConfiguration();
	static File f = new File(Defaults.MESSAGES_FILE);
	
	public static boolean setConfig(File f){
		MessageUtil.f = f;
		return load();
	}
	public static boolean load() {
		try {
			mc.load(f);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static String colorChat(String msg) {return msg.replaceAll("&", Character.toString((char) 167));}
	public static String decolorChat(String msg) { return msg.replaceAll("&", "§").replaceAll("\\§[0-9a-zA-Z]", "");}

	public static String getMessage(String prefix,String node, Object... varArgs) {
		try{
			ConfigurationSection n = mc.getConfigurationSection(prefix);

			StringBuilder buf = new StringBuilder(n.getString("prefix", "[Arena]"));
			String msg = n.getString(node, "No translation for " + node);
			Formatter form = new Formatter(buf);

			form.format(msg, varArgs);
			return colorChat(buf.toString());
		} catch(Exception e){
			System.err.println("Error getting message " + prefix + "." + node);
			for (Object o: varArgs){ System.err.println("argument=" + o);}
			e.printStackTrace();
			return "Error getting message " + prefix + "." + node;
		}
	}
	public static String getMessageNP(String prefix,String node, Object... varArgs) {
		ConfigurationSection n = mc.getConfigurationSection(prefix);
		StringBuilder buf = new StringBuilder();
		String msg = n.getString(node, "No translation for " + node);
		Formatter form = new Formatter(buf);
		try{
			form.format(msg, varArgs);
		} catch(Exception e){
			System.err.println("Error getting message " + prefix + "." + node);
			for (Object o: varArgs){ System.err.println("argument=" + o);}
			e.printStackTrace();
		}
		return colorChat(buf.toString());
	}

	public static String getMessageAddPrefix(String pprefix, String prefix,String node, Object... varArgs) {
		ConfigurationSection n = mc.getConfigurationSection(prefix);
		StringBuilder buf = new StringBuilder(pprefix);
		String msg = n.getString(node, "No translation for " + node);
		Formatter form = new Formatter(buf);
		try{
			form.format(msg, varArgs);
		} catch(Exception e){
			System.err.println("Error getting message " + prefix + "." + node);
			for (Object o: varArgs){ System.err.println("argument=" + o);}
			e.printStackTrace();
		}
		return colorChat(buf.toString());
	}


	public static boolean sendMessage(CommandSender p, String message){
		if (message ==null) return true;
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
	
	public static void sendPlayerMessage(Set<Player> players, String message) {
		final String msg = colorChat(message);
		for (Player p: players){
			p.sendMessage(msg);
		}
	}
	public static boolean hasMessage(String prefix, String node) {
		return mc.contains(prefix+"." + node);
	}    
	public static String minuteOrMinutes(int minutes) {
		return minutes == 1 ? "minute" : "minutes";
	}
	public static String getTeamsOrPlayers(int teamSize) {
		return teamSize==1 ? "players" : "teams";
	}
	public static String teamsOrPlayers(int nPlayersPerTeam){return nPlayersPerTeam > 1? "teams" : "players";}
	public static String playerOrPlayers(int n) {return n> 1? "players" : "player";}


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
