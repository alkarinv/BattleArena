package mc.alk.arena.executors;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.ArenaDebugger;
import mc.alk.arena.controllers.MethodController;
import mc.alk.arena.listeners.ArenaListener;
import mc.alk.arena.listeners.BukkitEventListener;
import mc.alk.arena.match.Match;
import mc.alk.arena.match.PerformTransition;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.util.ExpUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.MapOfHash;
import mc.alk.arena.util.TeamUtil;
import mc.alk.arena.util.Util;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
	public void enableDebugging(CommandSender sender, String section, Boolean on){

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

	@MCCommand( cmds = {"giveTeam","gt"}, online={1}, op=true, usage="giveTeam <player> <team index>")
	public boolean giveTeamHelmOther(CommandSender sender, ArenaPlayer p, Integer index){
		TeamUtil.setTeamHead(index, p);
		return sendMessage(sender, p.getName() +" Given team " + index);
	}
	
	@MCCommand( cmds = {"giveTeam","gt"}, inGame=true, op=true, usage="giveTeam <team index>")
	public boolean giveTeamHelm(ArenaPlayer p, Integer index){
		if (index < 0){
			p.getPlayer().setDisplayName(p.getName());
			return sendMessage(p, "&2Removing Team. &6/bad giveTeam <index> &2 to give a team name");
		}
		TeamUtil.setTeamHead(index, p);
		String tname = TeamUtil.createTeamName(index);
		p.getPlayer().setDisplayName(tname);
		return sendMessage(p, "&2Giving team " +index);
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
	

	@MCCommand( cmds = {"showListeners","sl"}, op=true, usage="showListeners")
	public boolean showListeners(CommandSender sender) {
		HashMap<Type, BukkitEventListener> types = MethodController.getEventListeners();
		for (BukkitEventListener bel: types.values()){
			Collection<ArenaListener> lists = bel.getMatchListeners();
			MapOfHash<String,ArenaListener> lists2 = bel.getListeners();
			String str = Util.toCommaDelimitedString(bel.getPlayers());
			sendMessage(sender, "Event " + bel.getEvent() +", players="+str);
			for (String p : lists2.keySet()){
				sendMessage(sender, bel.getEvent() +"  " + p +"  Listener  " + lists2.get(p));
			}

			for (ArenaListener al : lists){
				sendMessage(sender, "Listener " + al);
			}
		}
		return true;
	}
	
	@MCCommand(cmds={"addKill"}, op=true,min=2,usage="addKill <player>")
	public boolean arenaAddKill(CommandSender sender, ArenaPlayer pl) {
		Match am = ac.getMatch(pl);
		if (am == null){
			return sendMessage(sender,"&ePlayer " + pl.getName() +" is not in a match");}
		am.addKill(pl);
		return sendMessage(sender,pl.getName()+" has received a kill");
	}


	@MCCommand(cmds={"hidespawns"}, admin=true, usage="hidespawns <arena>")
	public boolean arenaHideSpawns(CommandSender sender, Arena arena) {
		ArenaDebugger ad = ArenaDebugger.getDebugger(arena);
		ad.hideSpawns();
		ArenaDebugger.removeDebugger(ad);
		return sendMessage(sender,ChatColor.YELLOW+ "You are hiding spawns for " + arena.getName());
	}

	@MCCommand(cmds={"showspawns"}, admin=true, usage="showspawns <arena>")
	public boolean arenaShowSpawns(CommandSender sender, Arena arena) {
		ArenaDebugger ad = ArenaDebugger.getDebugger(arena);
		ad.hideSpawns();
		ad.showSpawns();
		return sendMessage(sender,ChatColor.GREEN+ "You are showing spawns for " + arena.getName());
	}

	@MCCommand(cmds={"getExp"}, inGame=true, admin=true)
	public boolean getExp(Player player) {
		return sendMessage(player,ChatColor.GREEN+ "Experience  " + player.getTotalExperience() +" " + ExpUtil.getTotalExperience(player));
	}

}