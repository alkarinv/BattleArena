package mc.alk.arena.executors;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.controllers.ArenaClassController;
import mc.alk.arena.controllers.MethodController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.TeleportController;
import mc.alk.arena.listeners.BukkitEventListener;
import mc.alk.arena.listeners.RListener;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.queues.QueueObject;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.serializers.InventorySerializer;
import mc.alk.arena.util.ExpUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.InventoryUtil.PInv;
import mc.alk.arena.util.MapOfTreeSet;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.NotifierUtil;
import mc.alk.arena.util.PermissionsUtil;
import mc.alk.arena.util.TeamUtil;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BattleArenaDebugExecutor extends CustomCommandExecutor{

	@MCCommand( cmds = {"enableDebugging"}, admin=true,min=3, usage="enableDebugging <code section> <true | false>")
	public void enableDebugging(CommandSender sender, String section, Boolean on){
		if (section.equalsIgnoreCase("transitions")){
			Defaults.DEBUG_TRANSITIONS = on;
		} else if(section.equalsIgnoreCase("virtualplayer")){
			Defaults.DEBUG_VIRTUAL = on;
		} else if(section.equalsIgnoreCase("tracking")){
			Defaults.DEBUG_TRACKING = on;
		} else if(section.equalsIgnoreCase("storage")){
			Defaults.DEBUG_STORAGE = on;
		} else if(section.equalsIgnoreCase("damage")){
//			Defaults.DEBUG_DAMAGE = on;
		} else if(section.equalsIgnoreCase("commands")){
			Defaults.DEBUG_COMMANDS = on;
		} else if(section.equalsIgnoreCase("teams")){
			Defaults.DEBUG_MATCH_TEAMS = on;
		} else {
			sendMessage(sender, "&cDebugging couldnt find code section &6"+ section);
			return;
		}
		sendMessage(sender, "&4[BattleArena] &2debugging for &6" + section +"&2 now &6" + on);
	}

	@MCCommand( cmds = {"giveTeam"}, op=true, usage="giveTeam <player> <team index>")
	public boolean giveTeamHelmOther(CommandSender sender, ArenaPlayer p, Integer index){
		TeamUtil.setTeamHead(index, p);
		return sendMessage(sender, p.getName() +" Given team " + index);
	}

	@MCCommand( cmds = {"giveTeam"}, op=true, usage="giveTeam <team index>")
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

	@MCCommand( cmds = {"giveHelm"}, op=true, exact=2, usage="giveHelm <item>")
	public boolean giveHelm(Player sender, Command command, String label, String[] args) {
		ItemStack is;
		try {
			is = InventoryUtil.parseItem(args[1]);
		} catch (Exception e) {
			return sendMessage(sender, "&e couldnt parse item " + args[1]);
		}
		sender.getInventory().setHelmet(is);
		return sendMessage(sender, "&2Giving helm " +InventoryUtil.getCommonName(is));
	}


	@MCCommand( cmds = {"showListeners"}, admin=true)
	public boolean showListeners(CommandSender sender, String args[]) {
		String limitToPlayer = args.length > 1 ? args[1] : null;

		EnumMap<org.bukkit.event.EventPriority, HashMap<Type, BukkitEventListener>> gels = MethodController.getEventListeners();
		for (org.bukkit.event.EventPriority bp: gels.keySet()){
			sendMessage(sender, "&4#### &f----!! Bukkit Priority=&5"+bp+"&f !!---- &4####");
			HashMap<Type, BukkitEventListener> types = gels.get(bp);
			for (BukkitEventListener bel: types.values()){
				MapOfTreeSet<String,RListener> lists2 = bel.getListeners();
				String str = MessageUtil.joinBukkitPlayers(bel.getPlayers(),", ");
				String has = bel.hasListeners() ? "&2true" : "&cfalse";
				sendMessage(sender, "---- Event &e" + bel.getEvent().getSimpleName() +"&f:"+has+"&f, players="+str);
				for (String p : lists2.keySet()){
					if (limitToPlayer != null && !p.equalsIgnoreCase(limitToPlayer))
						continue;
					TreeSet<RListener> rls = lists2.get(p);
					for (RListener rl : rls){
						sendMessage(sender, "!!! "+rl.getPriority() +"  " + p +"  Listener  " + rl.getListener().getClass().getSimpleName());
					}
				}
				EnumMap<EventPriority, List<RListener>> lists = bel.getMatchListeners();
				for (EventPriority ep: lists.keySet()){
					for (RListener rl : lists.get(ep)){
						sendMessage(sender, "!!! " + ep  + "  -  " + rl);
					}
				}
			}
		}
		return true;
	}

	@MCCommand(cmds={"addKill"}, admin=true,min=2,usage="addKill <player>")
	public boolean arenaAddKill(CommandSender sender, ArenaPlayer pl) {
		Match am = ac.getMatch(pl);
		if (am == null){
			return sendMessage(sender,"&ePlayer " + pl.getName() +" is not in a match");}
//		am.addKill(pl);
		Team t = am.getTeam(pl);
		if (t != null){
			t.addKill(pl);
		}
		return sendMessage(sender,pl.getName()+" has received a kill");
	}


	@MCCommand(cmds={"getExp"}, admin=true)
	public boolean getExp(Player player) {
		return sendMessage(player,ChatColor.GREEN+ "Experience  " + player.getTotalExperience() +" " + ExpUtil.getTotalExperience(player));
	}

	@MCCommand(cmds={"showVars"}, admin=true)
	public boolean showVars(CommandSender sender, String paramName) {
		MatchParams mp = findMatchParam(sender, paramName);
		if (mp == null)
			return true;
		ReflectionToStringBuilder rtsb = new ReflectionToStringBuilder(mp, ToStringStyle.MULTI_LINE_STYLE);
		sendMessage(sender, mp.toString());
		return sendMessage(sender, rtsb.toString());
	}

	@MCCommand(cmds={"showTransitions"}, admin=true)
	public boolean showTransitions(CommandSender sender, String paramName) {
		MatchParams mp = findMatchParam(sender, paramName);
		if (mp == null)
			return true;
		sendMessage(sender, mp.toString());
		MatchTransitions mt = mp.getTransitionOptions();
		if (mt == null){
			return sendMessage(sender, ChatColor.RED+"MatchTransitions are null");
		}
		sendMessage(sender, mt.getOptionString());
		return true;
	}

	@MCCommand(cmds={"showPlayerVars"}, admin=true)
	public boolean showPlayerVars(CommandSender sender, ArenaPlayer player) {
		ReflectionToStringBuilder rtsb = new ReflectionToStringBuilder(player, ToStringStyle.MULTI_LINE_STYLE);
		return sendMessage(sender, rtsb.toString());
	}

	@MCCommand(cmds={"showArenaVars"}, admin=true)
	public boolean showArenaVars(CommandSender sender, Arena arena) {
		ReflectionToStringBuilder rtsb = new ReflectionToStringBuilder(arena, ToStringStyle.MULTI_LINE_STYLE);
		return sendMessage(sender, rtsb.toString());
	}

	@MCCommand(cmds={"showMatchVars"}, admin=true)
	public boolean showMatchVars(CommandSender sender, Arena arena) {
		Match m = BattleArena.getBAController().getMatch(arena);
		if (m == null){
			return sendMessage(sender, "&cMatch not currently running in arena " + arena.getName());}
		ReflectionToStringBuilder rtsb = new ReflectionToStringBuilder(m, ToStringStyle.MULTI_LINE_STYLE);
		return sendMessage(sender, rtsb.toString());
	}

	@MCCommand(cmds={"version"}, admin=true)
	public boolean showVersion(CommandSender sender) {
		sendMessage(sender, BattleArena.getNameAndVersion());
		for (ArenaType at : ArenaType.getTypes()){
			String name = at.getPlugin().getName();
			String version = at.getPlugin().getDescription().getVersion();
			sendMessage(sender, at.getName() +"  " + name +"  " + version);
		}
		return true;
	}

	private MatchParams findMatchParam(CommandSender sender, String paramName) {
		MatchParams mp = ParamController.getMatchParamCopy(paramName);
		if (mp == null){
			sendMessage(sender, "&cCouldn't find matchparams mp=" + paramName);}
		return mp;
	}

	@MCCommand(cmds={"invalidReasons"}, admin=true)
	public boolean arenaInvalidReasons(CommandSender sender, Arena arena) {
		Collection<String> reasons = arena.getInvalidReasons();
		sendMessage(sender, "&eInvalid reasons for &6" + arena.getName());
		for (String reason: reasons){
			MessageUtil.sendMessage(sender, reason);
		}
		return true;
	}

	@MCCommand(cmds={"invalidQReasons"}, admin=true)
	public boolean matchQInvalidReasons(CommandSender sender, ArenaPlayer player, Arena arena) {
		QueueObject qo = BattleArena.getBAController().getQueueObject(player);
		if (qo == null){
			return sendMessage(sender, "&cThat player is not in a queue");}
		Collection<String> reasons = arena.getInvalidMatchReasons(qo.getMatchParams(), qo.getJoinOptions());
		sendMessage(sender, "&eInvalid reasons for &6" + arena.getName());
		for (String reason: reasons){
			MessageUtil.sendMessage(sender, reason);
		}
		return true;
	}

	@MCCommand(cmds={"verify"}, admin=true,usage="verify")
	public boolean arenaVerify(CommandSender sender) {
		String[] lines = ac.toDetailedString().split("\n");
		for (String line : lines){
			sendMessage(sender,line);}
		return true;
	}

	@MCCommand(cmds={"showq"}, admin=true)
	public boolean showQueue(CommandSender sender) {
		sendMessage(sender,ac.queuesToString());
		return true;
	}

	@MCCommand(cmds={"online"}, admin=true)
	public boolean arenaVerify(CommandSender sender, OfflinePlayer p) {
		return sendMessage(sender, "Player " + p.getName() +"  is " + p.isOnline());
	}

	@MCCommand(cmds={"purgeQueue"}, admin=true)
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

	@MCCommand(cmds={"hasPerm"}, admin=true)
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

	@MCCommand(cmds={"setexp"}, op=true)
	public boolean setExp(CommandSender sender, ArenaPlayer p, Integer exp) {
		ExpUtil.setTotalExperience(p.getPlayer(), exp);
		return sendMessage(sender,"&2Player's exp set to " + exp );
	}

	@MCCommand(cmds={"tp"}, admin=true)
	public boolean teleportToSpawn(ArenaPlayer sender, Arena arena, Integer spawnIndex) {
		if (spawnIndex < 1)
			spawnIndex=1;
		Location loc = arena.getSpawnLoc(spawnIndex-1);
		if (loc ==null){
			return sendMessage(sender,"&2Spawn " + spawnIndex +" doesn't exist");}
		TeleportController.teleport(sender.getPlayer(), loc);
		return sendMessage(sender,"&2Teleported to &6"+ spawnIndex +" &2loc=&6"+loc);
	}

	@MCCommand(cmds={"giveArenaClass"}, admin=true)
	public boolean giveArenaClass(CommandSender sender, String className, Player player) {
		ArenaClass ac = ArenaClassController.getClass(className);
		if (ac == null)
			return sendMessage(sender, "&cArena class " + className +" doesn't exist");
		ArenaClassController.giveClass(player, ac);
		return sendMessage(sender, "&2Arena class " + ac.getDisplayName() +"&2 given to &6" + player.getName());
	}

	@MCCommand(cmds={"allowAdminCommands"}, admin=true)
	public boolean allowAdminCommands(CommandSender sender, Boolean enable) {
		Defaults.ALLOW_ADMIN_CMDS_IN_Q_OR_MATCH = enable;
		return sendMessage(sender,"&2Admins can "+ (enable ? "&6use" : "&cnot use")+"&2 commands in match");
	}

	@MCCommand(cmds={"giveAdminPerms"}, op=true)
	public boolean giveAdminPerms(CommandSender sender, Player player, Boolean enable) {
		if (!PermissionsUtil.giveAdminPerms(player,enable)){
			return sendMessage(sender,"&cCouldn't change the admin perms of &6"+player.getName());}
		if (enable){
			return sendMessage(sender,"&2 "+player.getName()+" &6now has&2 admin perms");
		} else {
			return sendMessage(sender,"&2 "+player.getName()+" &4no longer has&2 admin perms");
		}
	}

	@MCCommand(cmds={"notify"}, admin=true)
	public boolean addNotifyListener(CommandSender sender, Player player, String type, Boolean enable) {
		if (enable){
			NotifierUtil.addListener(player, type);
			if (!sender.getName().equals(player.getName()))sendMessage(player,"&2 "+player.getName()+" &6now listening &2to " + type+" debugging messages");
			return sendMessage(sender,"&2 "+player.getName()+" &6now listening &2to " + type+" debugging messages");
		} else {
			NotifierUtil.removeListener(player, type);
			if (!sender.getName().equals(player.getName()))sendMessage(player,"&2 "+player.getName()+" &cstopped listening&2 to " + type+" debugging messages");
			return sendMessage(sender,"&2 "+player.getName()+" &cstopped listening&2 to " + type+" debugging messages");
		}
	}

}