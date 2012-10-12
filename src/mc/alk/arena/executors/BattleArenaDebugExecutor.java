package mc.alk.arena.executors;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.controllers.MethodController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.PlayerStoreController.PInv;
import mc.alk.arena.listeners.ArenaListener;
import mc.alk.arena.listeners.BukkitEventListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.serializers.InventorySerializer;
import mc.alk.arena.util.ExpUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.MapOfHash;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TeamUtil;
import mc.alk.arena.util.Util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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

	@MCCommand( cmds = {"enableDebugging"}, op=true,min=3, usage="enableDebugging <code section> <true | false>")
	public void enableDebugging(CommandSender sender, String section, Boolean on){
		if (section.equalsIgnoreCase("transitions")){
			Defaults.DEBUG_TRANSITIONS = on;
		} else if(section.equalsIgnoreCase("virtualplayer")){
			Defaults.DEBUG_VIRTUAL = on;
		} else if(section.equalsIgnoreCase("storage")){
			Defaults.DEBUG_STORAGE = on;
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


	@MCCommand(cmds={"getExp"}, inGame=true, admin=true)
	public boolean getExp(Player player) {
		return sendMessage(player,ChatColor.GREEN+ "Experience  " + player.getTotalExperience() +" " + ExpUtil.getTotalExperience(player));
	}

	@MCCommand(cmds={"showMatchParams"}, admin=true)
	public boolean showMatchParams(CommandSender sender, String paramName) {
		MatchParams mp = ParamController.getMatchParamCopy(paramName);
		if (mp == null){
			return sendMessage(sender, "&cCouldn't find matchparams mp=" + paramName);}
		return sendMessage(sender, mp.toString());
	}

	@MCCommand(cmds={"invalidReason"}, op=true)
	public boolean arenaAddKill(CommandSender sender, Arena arena) {
		Collection<String> reasons = arena.getInvalidReasons();
		sendMessage(sender, "&eInvalid reasons for &6" + arena.getName());
		for (String reason: reasons){
			MessageUtil.sendMessage(sender, reason);
		}
		return true;
	}

	@MCCommand(cmds={"verify"}, op=true,usage="verify")
	public boolean arenaVerify(CommandSender sender) {
		String[] lines = ac.toDetailedString().split("\n");
		for (String line : lines){
			sendMessage(sender,line);}
		return true;
	}
	@MCCommand(cmds={"online"}, op=true)
	public boolean arenaVerify(CommandSender sender, OfflinePlayer p) {
		return sendMessage(sender, "Player " + p.getName() +"  is " + p.isOnline());
	}

	@MCCommand(cmds={"purgeQueue"}, op=true)
	public boolean arenaPurgeQueue(CommandSender sender) {
		try {
			Collection<Team> teams = ac.purgeQueue();
			for (Team t: teams){
				t.sendMessage("&eYou have been &cremoved&e from the queue by an administrator");
			}
		} catch (Exception e){
			e.printStackTrace();
			sendMessage(sender,"&4error purging queue");
			return true;
		}
		sendMessage(sender,"&2Queue purged");
		return true;
	}

	@MCCommand(cmds={"hasPerm"}, op=true)
	public boolean hasPerm(CommandSender sender, String perm, Player p) {
		return sendMessage(sender, "Player " + p.getName() +"  hasPerm " + perm +" " +p.hasPermission(perm));
	}

	@MCCommand(cmds={"listInv"}, admin=true)
	public boolean listSaves(CommandSender sender, OfflinePlayer p) {
		Collection<String> dates = InventorySerializer.getDates(p.getName());
		if (dates == null){
			return sendMessage(sender, "There are no inventory saves for this player");
		}
		int i=0;
		sendMessage(sender, "Most recent inventory saves");
		for (String date: dates){
			sendMessage(sender, ++i +" : " + date);
		}
		return true;
	}

	@MCCommand(cmds={"listInv"}, admin=true)
	public boolean listSave(CommandSender sender, OfflinePlayer p, Integer index) {
		if (index < 0 || index > Defaults.NUM_INV_SAVES){
			return sendMessage(sender,"&c index must be between 1-"+Defaults.NUM_INV_SAVES);}
		PInv pinv = InventorySerializer.getInventory(p.getName(), index-1);
		if (pinv == null)
			return sendMessage(sender, "&cThis index doesn't have an inventory!");
		sendMessage(sender, "&6" + p.getName() +" inventory at save " + index);
		boolean has = false;
		for (ItemStack is: pinv.armor){
			if (is == null || is.getType() == Material.AIR) continue;
			sendMessage(sender, "&a armor: &6" + InventoryUtil.getItemString(is));
			has = true;
		}
		for (ItemStack is: pinv.contents){
			if (is == null || is.getType() == Material.AIR) continue;
			sendMessage(sender, "&b inv: &6" + InventoryUtil.getItemString(is));
			has = true;
		}
		if (!has){
			sendMessage(sender, "&cThis index doesn't have any items");}
		return true;
	}

	@MCCommand(cmds={"restoreInv"}, admin=true)
	public boolean restoreInv(CommandSender sender, ArenaPlayer p, Integer index) {
		if (index < 0 || index > Defaults.NUM_INV_SAVES){
			return sendMessage(sender,"&c index must be between 1-"+Defaults.NUM_INV_SAVES);}
		if (InventorySerializer.restoreInventory(p,index-1)){
			return sendMessage(sender, "&2Player inventory restored");
		} else {			
			return sendMessage(sender, "&cPlayer inventory could not be restored");
		}
	}

}