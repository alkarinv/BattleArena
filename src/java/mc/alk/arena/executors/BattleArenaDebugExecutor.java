package mc.alk.arena.executors;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.controllers.ArenaClassController;
import mc.alk.arena.controllers.MethodController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.RoomController;
import mc.alk.arena.controllers.TeleportController;
import mc.alk.arena.controllers.containers.RoomContainer;
import mc.alk.arena.listeners.custom.BukkitEventListener;
import mc.alk.arena.listeners.custom.TimingStat;
import mc.alk.arena.objects.ArenaClass;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.queues.QueueObject;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.ExpUtil;
import mc.alk.arena.util.InventoryUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.NotifierUtil;
import mc.alk.arena.util.PermissionsUtil;
import mc.alk.arena.util.SerializerUtil;
import mc.alk.arena.util.TeamUtil;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BattleArenaDebugExecutor extends CustomCommandExecutor{

	@MCCommand( cmds = {"enableDebugging","ed"}, admin=true,usage="enableDebugging <code section> <true | false>")
	public void enableDebugging(CommandSender sender, String section, Boolean on){
		if (section.equalsIgnoreCase("transitions")){
			Defaults.DEBUG_TRANSITIONS = on;
		} else if(section.equalsIgnoreCase("virtualplayer") || section.equalsIgnoreCase("vp")){
			Defaults.DEBUG_VIRTUAL = on;
		} else if(section.equalsIgnoreCase("tracking")){
			Defaults.DEBUG_TRACKING = on;
		} else if(section.equalsIgnoreCase("storage")){
			Defaults.DEBUG_STORAGE = on;
		} else if(section.equalsIgnoreCase("damage")){
			//			Defaults.DEBUG_DAMAGE = on;
			//		} else if(section.equalsIgnoreCase("q")){
			//						Defaults.DEBUGQ = on;
		} else if(section.equalsIgnoreCase("commands")){
			Defaults.DEBUG_COMMANDS = on;
		} else if(section.equalsIgnoreCase("debug")){
			Defaults.DEBUG_MSGS = on;
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
		String tname = TeamUtil.getTeamName(index);
		p.getPlayer().setDisplayName(tname);
		return sendMessage(p, "&2Giving team " +index);
	}

	@MCCommand( cmds = {"giveHelm"}, op=true, exact=2, usage="giveHelm <item>")
	public boolean giveHelm(Player sender, String[] args) {
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
		String limitToPlayer = args.length > 1 ? args[1] : "";
		return MethodController.showAllListeners(sender,limitToPlayer);
	}

	@MCCommand(cmds={"addKill"}, admin=true,min=2,usage="addKill <player>")
	public boolean arenaAddKill(CommandSender sender, ArenaPlayer pl) {
		Match am = ac.getMatch(pl);
		if (am == null){
			return sendMessage(sender,"&ePlayer " + pl.getName() +" is not in a match");}
		//		am.addKill(pl);
		ArenaTeam t = am.getTeam(pl);
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
	public boolean showVars(CommandSender sender, String paramName, String[] args) {
		MatchParams mp = findMatchParam(sender, paramName);
		if (mp == null)
			return true;
		sendMessage(sender, mp.toString());
        if (args.length > 3 && args[3].equals("parent")){
            return sendMessage(sender, new ReflectionToStringBuilder(mp.getParent().getParent(), ToStringStyle.MULTI_LINE_STYLE)+"");
        } else if (args.length > 2 && args[2].equals("parent")){
                return sendMessage(sender, new ReflectionToStringBuilder(mp.getParent(), ToStringStyle.MULTI_LINE_STYLE)+"");
		} else {
			return sendMessage(sender, new ReflectionToStringBuilder(mp, ToStringStyle.MULTI_LINE_STYLE)+"");
		}
	}

	@MCCommand(cmds={"showTransitions"}, admin=true)
	public boolean showTransitions(CommandSender sender, String paramName) {
		MatchParams mp = findMatchParam(sender, paramName);
        return mp == null || sendMessage(sender, mp.toString());
    }

	@MCCommand(cmds={"showPlayerVars"}, admin=true)
	public boolean showPlayerVars(CommandSender sender, ArenaPlayer player) {
		ReflectionToStringBuilder rtsb = new ReflectionToStringBuilder(player, ToStringStyle.MULTI_LINE_STYLE);
		return sendMessage(sender, rtsb.toString());
	}

	@MCCommand(cmds={"showArenaVars"}, admin=true)
	public boolean showArenaVars(CommandSender sender, Arena arena, String[] args) {
        if (args.length > 4 && args[4].equals("parent")){
            return sendMessage(sender, new ReflectionToStringBuilder(arena.getParams().getParent().getParent(), ToStringStyle.MULTI_LINE_STYLE)+"");
        } else if (args.length > 3 && args[3].equals("parent")){
			return sendMessage(sender, new ReflectionToStringBuilder(arena.getParams().getParent(), ToStringStyle.MULTI_LINE_STYLE)+"");
		} else if (args.length > 3 && args[3].equals("transitions")){
			return sendMessage(sender, new ReflectionToStringBuilder(arena.getParams().getTransitionOptions(), ToStringStyle.MULTI_LINE_STYLE)+"");
		} else if (args.length > 2 && args[2].equals("waitroom")){
			return sendMessage(sender, new ReflectionToStringBuilder(arena.getWaitroom(), ToStringStyle.MULTI_LINE_STYLE)+"");
		}  else if (args.length > 2 && args[2].equals("params")){
			return sendMessage(sender, new ReflectionToStringBuilder(arena.getParams(), ToStringStyle.MULTI_LINE_STYLE)+"");
		} else {
			return sendMessage(sender, new ReflectionToStringBuilder(arena, ToStringStyle.MULTI_LINE_STYLE)+"");
		}
	}

	@MCCommand(cmds={"showMatchVars"}, admin=true)
	public boolean showMatchVars(CommandSender sender, Arena arena, String[] vars) {
		Match m = BattleArena.getBAController().getMatch(arena);
		if (m == null){
			return sendMessage(sender, "&cMatch not currently running in arena " + arena.getName());}
		if (vars.length > 2 && vars[2].equals("transitions")){
			return sendMessage(sender, m.getParams().getTransitionOptions().getOptionString());
		}

		if (vars.length > 2){
			String param = vars[2];
			boolean sb = vars.length > 3 && Boolean.valueOf(vars[3]);
			for(Field field : Match.class.getDeclaredFields()){
				if (field.getName().equalsIgnoreCase(param)){
					field.setAccessible(true);
					try {
						if (sb){
							return sendMessage(sender, "&2Parameter " + param +" = <"+field.get(m) +">" );
						} else {
							ReflectionToStringBuilder rtsb = new ReflectionToStringBuilder(field.get(m), ToStringStyle.MULTI_LINE_STYLE);
							return sendMessage(sender, rtsb.toString());
						}
					} catch (Exception e) {
						return sendMessage(sender, "&cError getting param "+param+" : msg=" + e.getMessage());
					}
				}
			}
			return sendMessage(sender, "&cThe param &6"+param+ "&c does not exist in &6" + m.getClass().getSimpleName());
		} else {
			ReflectionToStringBuilder rtsb = new ReflectionToStringBuilder(m, ToStringStyle.MULTI_LINE_STYLE);
			return sendMessage(sender, rtsb.toString());
		}
	}

	@MCCommand(cmds={"showLobbyVars"}, admin=true)
	public boolean showLobbyVars(CommandSender sender, String arenatype) {
		ArenaType type = ArenaType.fromString(arenatype);
		if (type == null){
			return sendMessage(sender, "&cArenaType not found &6" + arenatype);}

		RoomContainer lobby = RoomController.getLobby(type);
		if (lobby == null){
			return sendMessage(sender, "&cThere is no lobby for &6" + type.getName());}
		ReflectionToStringBuilder rtsb = new ReflectionToStringBuilder(lobby, ToStringStyle.MULTI_LINE_STYLE);
		sendMessage(sender, rtsb.toString());
		rtsb = new ReflectionToStringBuilder(lobby.getParams().getTransitionOptions(), ToStringStyle.MULTI_LINE_STYLE);
		return sendMessage(sender, rtsb.toString());
	}

	private MatchParams findMatchParam(CommandSender sender, String paramName) {
		MatchParams mp = ParamController.getMatchParams(paramName);
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

	@MCCommand(cmds={"showClass"}, op=true)
	public boolean showClass(CommandSender sender, String stringClass) {
		final Class<?> clazz;
		try {
			clazz = Class.forName(stringClass);
		} catch (ClassNotFoundException e) {
			return sendMessage(sender, "&cClass " + stringClass +" not found");
		}
		ReflectionToStringBuilder rtsb = new ReflectionToStringBuilder(clazz, ToStringStyle.MULTI_LINE_STYLE);
		return sendMessage(sender, rtsb.toString());
	}

	@MCCommand(cmds={"showAMQ"}, op=true)
	public boolean showAMQ(CommandSender sender) {
		ReflectionToStringBuilder rtsb = new ReflectionToStringBuilder(BattleArena.getBAController().getArenaMatchQueue(), ToStringStyle.MULTI_LINE_STYLE);
		return sendMessage(sender, rtsb.toString());
	}

	@MCCommand(cmds={"showBAC"}, op=true)
	public boolean showBAC(CommandSender sender) {
		ReflectionToStringBuilder rtsb = new ReflectionToStringBuilder(BattleArena.getBAController(), ToStringStyle.MULTI_LINE_STYLE);
		return sendMessage(sender, rtsb.toString());
	}

	@MCCommand(cmds={"verify"}, admin=true)
	public boolean arenaVerify(CommandSender sender) {
		String[] lines = ac.toStringQueuesAndMatches().split("\n");
		for (String line : lines){
			sendMessage(sender,line);}
		return true;
	}

	@MCCommand(cmds={"showAllArenas"}, admin=true)
	public boolean arenaShowAllArenas(CommandSender sender) {
		String[] lines = ac.toStringArenas().split("\n");
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
			Collection<ArenaTeam> teams = ac.purgeQueue();
			for (ArenaTeam t: teams){
				t.sendMessage("&eYou have been &cremoved&e from the queue by an administrator");
			}
		} catch (Exception e){
			Log.printStackTrace(e);
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

	@MCCommand(cmds={"setexp"}, op=true)
	public boolean setExp(CommandSender sender, ArenaPlayer p, Integer exp) {
		ExpUtil.setTotalExperience(p.getPlayer(), exp);
		return sendMessage(sender,"&2Player's exp set to " + exp );
	}

	@MCCommand(cmds={"tp"}, admin=true)
	public boolean teleportToSpawn(ArenaPlayer sender, Arena arena, Integer spawnIndex) {
		return teleportToSpawn(sender,arena,LocationType.ARENA, spawnIndex);
	}

	@MCCommand(cmds={"tp"}, admin=true)
	public boolean teleportToSpawn(ArenaPlayer sender, Arena arena, String type, Integer spawnIndex) {
		try{
			return teleportToSpawn(sender,arena,LocationType.valueOf(type.toUpperCase()), spawnIndex);
		} catch (IllegalArgumentException e){
			return sendMessage(sender,"&c" + e.getMessage());
		}
	}

	public boolean teleportToSpawn(ArenaPlayer sender, Arena arena, LocationType type, Integer spawnIndex) {
		if (spawnIndex < 1)
			spawnIndex=1;
		final Location loc;
		switch(type){
		case ANY:
		case ARENA:
			loc = arena.getSpawn(spawnIndex-1, false);
			break;
		case WAITROOM:
			loc = arena.getWaitroom()!= null ? arena.getWaitroom().getSpawn(spawnIndex-1, false) : null;
			break;
		case HOME:
			loc = sender.getOldLocation();
			break;
		case LOBBY:
			loc = arena.getLobby()!= null ? arena.getLobby().getSpawn(spawnIndex-1, false) : null;
			break;
		case SPECTATE:
			loc = arena.getSpectatorRoom()!= null ? arena.getSpectatorRoom().getSpawn(spawnIndex-1, false) : null;
			break;
		case NONE:
		case COURTYARD:
		case CUSTOM:
		default:
			loc = null;
			break;
		}
		if (loc ==null){
			return sendMessage(sender,"&2Spawn " + spawnIndex +" doesn't exist for " + type);}
		TeleportController.teleport(sender.getPlayer(), loc);
		return sendMessage(sender,"&2Teleported to &6"+ type +" " + spawnIndex +" &2loc=&6"+SerializerUtil.getBlockLocString(loc));
	}

	@MCCommand(cmds={"giveArenaClass"}, admin=true)
	public boolean giveArenaClass(CommandSender sender, String className, Player player) {
		ArenaClass ac = ArenaClassController.getClass(className);
		if (ac == null)
			return sendMessage(sender, "&cArena class " + className +" doesn't exist");
		ArenaClassController.giveClass(BattleArena.toArenaPlayer(player), ac);
		return sendMessage(sender, "&2Arena class " + ac.getDisplayName() +"&2 given to &6" + player.getName());
	}

	@MCCommand(cmds={"allowAdminCommands"}, admin=true)
	public boolean allowAdminCommands(CommandSender sender, Boolean enable) {
		Defaults.ALLOW_ADMIN_CMDS_IN_Q_OR_MATCH = enable;
		return sendMessage(sender,"&2Admins can "+ (enable ? "&6use" : "&cnot use")+"&2 commands in match");
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

	@MCCommand(cmds={"giveArenaAdminPerms"}, op=true)
	public boolean giveArenaAdminPerms(CommandSender sender, Player player, Boolean enable) {
		if (!PermissionsUtil.giveAdminPerms(player,enable)){
			return sendMessage(sender,"&cCouldn't change the admin perms of &6"+player.getName());}
		if (enable){
			return sendMessage(sender,"&2 "+player.getName()+" &6now has&2 admin perms");
		} else {
			return sendMessage(sender,"&2 "+player.getName()+" &4no longer has&2 admin perms");
		}
	}

	@MCCommand(cmds={"giveWGPerms"}, op=true)
	public boolean giveWorldGuardPerms(CommandSender sender, Player player, Boolean enable) {
		if (!PermissionsUtil.giveWGPerms(player,enable)){
			return sendMessage(sender,"&cCouldn't change the admin perms of &6"+player.getName());}
		if (enable){
			return sendMessage(sender,"&2 "+player.getName()+" &6now has&2 wg perms");
		} else {
			return sendMessage(sender,"&2 "+player.getName()+" &4no longer has&2 wg perms");
		}
	}

	@MCCommand(cmds={"showContainers"}, admin=true)
	public boolean showContainers(CommandSender sender, String args[]) {
		MatchParams p = null;
		if (args.length > 1){
			p = ParamController.getMatchParamCopy(args[1]);
		}
		if (p == null){
			sendMessage(sender, "&5Lobbies");
			for (RoomContainer c : RoomController.getLobbies()){
				sendMessage(sender," &2" + c.getName() +" : &6" + c.getContainerState().getState());
			}
		}
		sendMessage(sender, "&5Arenas");
		for (Arena a: BattleArena.getBAController().getArenas().values()){
			if (p != null && a.getArenaType() != p.getType())
				continue;
			sendMessage(sender," &2" + a.getName() +" - &6" + a.getContainerState().getState());
			if (a.getWaitroom() != null)
				sendMessage(sender,"   &2   - &6" + a.getWaitroom().getName() +" : &6"+a.getWaitroom().getContainerState().getState());
		}
		return true;
	}

	@MCCommand(cmds={"setTimings"}, admin=true)
	public boolean setTimings(CommandSender sender, boolean set) {
		BukkitEventListener.setTimings(set);
		sendMessage(sender, "&2Timings now " +set);
		return true;
	}

	@MCCommand(cmds={"timings"}, admin=true)
	public boolean showTimings(CommandSender sender, String[] args) {
		boolean useMs = args.length >1 && args[1].equalsIgnoreCase("ms");
		Map<String,TimingStat> timings = BukkitEventListener.getTimings();
		sendMessage(sender, BattleArena.getNameAndVersion() +" "+(useMs ? "time(ms)" : "time(ns)"));
		long gtotal = 0;
		for (Entry<String,TimingStat> entry : timings.entrySet()){
			TimingStat t = entry.getValue();
			long total = useMs ? t.totalTime/1000000 : t.totalTime;
			gtotal += total;
			long avg = useMs ? t.getAverage()/1000000 : t.getAverage();
			sendMessage(sender, "    " +entry.getKey() +" Time: "+total +" Count: "+t.count +" Avg: " +avg);
		}
		sendMessage(sender, "    Total time "+gtotal + (useMs ? " time(ms)" : " time(ns)"));
		return true;
	}
}
