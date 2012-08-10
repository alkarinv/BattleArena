package mc.alk.arena.executors;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.ArenaDebugger;
import mc.alk.arena.controllers.MessageController;
import mc.alk.arena.controllers.MethodController;
import mc.alk.arena.listeners.ArenaListener;
import mc.alk.arena.listeners.BukkitEventListener;
import mc.alk.arena.match.Match;
import mc.alk.arena.match.PerformTransition;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.MapOfHash;
import mc.alk.arena.util.TeamUtil;
import mc.alk.arena.util.Util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.alk.controllers.MC;

public class BattleArenaDebugExecutor extends CustomCommandExecutor{
	public BattleArenaDebugExecutor(){}


	public void showHelp(CommandSender sender, Command command){
		help(sender,command,null,null);
	}

	@MCCommand( cmds = {"help","?"})
	public void help(CommandSender sender, Command command, String label, Object[] args){
		super.help(sender, command, args);
	}


	@MCCommand( cmds = {"enableDebugging"}, op=true,min=3, usage="enableDebugging <code section> <on off>")
	public void enableDebugging(CommandSender sender, Command command, String label, Object[] args){
		final String section = (String) args[1];
		final boolean on = ((String) args[2]).equals("on");
		if (section.equalsIgnoreCase("transitions")){
			PerformTransition.debug=on;
		} else if(section.equalsIgnoreCase("virtualplayer")){
			Defaults.DEBUG_VIRTUAL = on;
		} else {
			sendMessage(sender, "&cDebugging couldnt find code section &6"+ section);
			return;
		}
		sendMessage(sender, "&2Debugging for &6" + section +"&2 now &6" + on);
	}

	@MCCommand( cmds = {"giveTeam","gt"}, online={1}, ints={2}, op=true, usage="giveTeam <player> <team index>")
	public boolean giveTeamHelmOther(CommandSender sender, Command command, String label, Object[] args){
		Player p = (Player) args[1];
		Integer index = (Integer) args[2];
		TeamUtil.setTeamHead(index, p);
		return sendMessage(sender, p.getName() +" Given team " + index);
	}
	
	@MCCommand( cmds = {"giveTeam","gt"}, inGame=true, op=true, ints={1}, usage="giveTeam <team index>")
	public boolean giveTeamHelm(CommandSender sender, Command command, String label, Object[] args){
		Player p = (Player) sender;
		Integer index = (Integer) args[1];
		if (index < 0){
			p.setDisplayName(p.getName());
			return sendMessage(sender, "&2Removing Team. &6/bad giveTeam <index> &2 to give a team name");
		}
		TeamUtil.setTeamHead(index, p);
		String tname = TeamUtil.createTeamName(index);
		p.setDisplayName(tname);
		return sendMessage(sender, "&2Giving team " +index);
	}

	@MCCommand( cmds = {"giveHelm","gh"}, inGame=true, op=true, exact=2, usage="giveHelm <item>")
	public boolean giveHelm(CommandSender sender, Command command, String label, Object[] args) {
		Player p = (Player) sender;
		ItemStack is;
		try {
			is = InventoryUtil.parseItem((String) args[1]);
		} catch (Exception e) {
			return sendMessage(sender, "&e couldnt parse item " + args[1]);
		}
		p.getInventory().setHelmet(is);
		return sendMessage(sender, "&2Giving helm " +InventoryUtil.getCommonName(is));
	}
	

	@MCCommand( cmds = {"showListeners","sl"}, op=true, exact=1, usage="showListeners")
	public boolean showListeners(CommandSender sender, Command command, String label, Object[] args) {
		HashMap<Type, BukkitEventListener> types = MethodController.getEventListeners();
		for (BukkitEventListener bel: types.values()){
			Collection<ArenaListener> lists = bel.getMatchListeners();
			MapOfHash<Player,ArenaListener> lists2 = bel.getListeners();
			String str = Util.toCommaDelimitedString(bel.getPlayers());
			sendMessage(sender, "Event " + bel.getEvent() +", players="+str);
			for (Player p : lists2.keySet()){
				sendMessage(sender, bel.getEvent() +"  " + p.getName() +"  Listener  " + lists2.get(p));
			}

			for (ArenaListener al : lists){
				sendMessage(sender, "Listener " + al);
			}
		}
		return true;
	}
	
	@MCCommand(cmds={"addKill"}, op=true,min=2,usage="addKill <player>")
	public boolean arenaAddKill(CommandSender sender, Command cmd, String commandLabel, Object[] args) {
		OfflinePlayer pl = Util.findPlayer((String)args[1]); 
		if (pl == null){
			MC.sendMessage(sender,"&ePlayer &6" + args[1] +"&e not online.  checking offline player="+args[1]);}
		pl = Bukkit.getOfflinePlayer((String)args[1]); 

		Match am = ac.getMatch(pl);
		if (am == null){
			return sendMessage(sender,"&ePlayer " + pl.getName() +" is not in a match");}
		am.addKill(pl);
		return sendMessage(sender,pl.getName()+" has received a kill");
	}


	@MCCommand(cmds={"hidespawns"}, inGame=true, admin=true, arenas={1}, usage="hidespawns <arena>")
	public boolean arenaHideSpawns(CommandSender sender, Command cmd, String commandLabel, Object[] args) {
		Arena arena = (Arena) args[1];
		ArenaDebugger ad = ArenaDebugger.getDebugger(arena);
		ad.hideSpawns();
		ArenaDebugger.removeDebugger(ad);
		return sendMessage(sender,ChatColor.YELLOW+ "You are hiding spawns for " + arena.getName());
	}

	@MCCommand(cmds={"showspawns"}, inGame=true, admin=true, arenas={1}, usage="showspawns <arena>")
	public boolean arenaShowSpawns(CommandSender sender, Command cmd, String commandLabel, Object[] args) {
		Arena arena = (Arena) args[1];
		ArenaDebugger ad = ArenaDebugger.getDebugger(arena);
		ad.hideSpawns();
		ad.showSpawns();
		return sendMessage(sender,ChatColor.GREEN+ "You are showing spawns for " + arena.getName());
	}

	public static boolean sendMessage(final CommandSender sender, final String msg){
		return MessageController.sendMessage(sender, msg);
	}

}