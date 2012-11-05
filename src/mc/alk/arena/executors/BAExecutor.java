package mc.alk.arena.executors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.events.Event;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.controllers.ArenaAlterController;
import mc.alk.arena.controllers.DuelController;
import mc.alk.arena.controllers.EventController;
import mc.alk.arena.controllers.MobArenaInterface;
import mc.alk.arena.controllers.MoneyController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.PlayerController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.controllers.TeleportController;
import mc.alk.arena.events.arenas.ArenaCreateEvent;
import mc.alk.arena.events.arenas.ArenaDeleteEvent;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.Duel;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.Rating;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.options.DuelOptions;
import mc.alk.arena.objects.options.DuelOptions.DuelOption;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.objects.options.JoinOptions.JoinOption;
import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.objects.pairs.ParamTeamPair;
import mc.alk.arena.objects.pairs.QPosTeamPair;
import mc.alk.arena.objects.pairs.WantedTeamSizePair;
import mc.alk.arena.objects.teams.FormingTeam;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.serializers.ArenaSerializer;
import mc.alk.arena.serializers.ConfigSerializer;
import mc.alk.arena.serializers.MessageSerializer;
import mc.alk.arena.util.BTInterface;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TimeUtil;
import mc.alk.arena.util.Util;
import mc.alk.arena.util.Util.MinMax;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author alkarin
 *
 */
public class BAExecutor extends CustomCommandExecutor  {
	Map<String, Location> visitors = new HashMap<String, Location>();
	Set<String> disabled = new HashSet<String>();

	final TeamController teamc;
	final EventController ec;
	final DuelController dc;
	public BAExecutor(){
		super();
		this.ec = BattleArena.getEventController();
		this.teamc = BattleArena.getTeamController();
		this.dc = BattleArena.getSelf().getDuelController();
	}

	@MCCommand(cmds={"enable"},admin=true,usage="enable")
	public boolean arenaEnable(CommandSender sender, MatchParams mp, String[] args) {
		if (args.length > 1 && args[1].equalsIgnoreCase("all")){
			Set<String> set = new HashSet<String>(); /// Since some commands have aliases.. we just want the original name
			for (MatchParams param : ParamController.getAllParams()){
				disabled.remove(param.getName());
				set.add(param.getName());
			}
			for (String s: set){
				sendMessage(sender, "&2 type &6" + s + "&2 enabled");}
			return true;
		}
		disabled.remove(mp.getName());
		return sendMessage(sender, "&2 type &6" + mp.getName() + "&2 enabled");
	}

	@MCCommand(cmds={"disable"},admin=true,usage="disable")
	public boolean arenaDisable(CommandSender sender, MatchParams mp, String[] args) {
		if (args.length > 1 && args[1].equalsIgnoreCase("all")){
			Set<String> set = new HashSet<String>(); /// Since some commands have aliases.. we just want the original name
			for (MatchParams param : ParamController.getAllParams()){
				disabled.add(param.getName());
				set.add(param.getName());
			}
			for (String s: set){
				sendMessage(sender, "&5 type &6" + s + "&5 disabled");}
			return true;
		}
		disabled.add(mp.getName());
		return sendMessage(sender, "&5 type &6" + mp.getName() + "&5 disabled");
	}

	@MCCommand(cmds={"enabled"},admin=true)
	public boolean arenaCheckArenaTypes(CommandSender sender) {
		String types = ArenaType.getValidList();
		sendMessage(sender, "&e valid types are = &6"+types);
		return sendMessage(sender, "&5Enabled types = &6 " + ParamController.getAvaibleTypes(disabled));
	}

	@MCCommand(cmds={"join"},inGame=true,usage="join [options]")
	public boolean join(ArenaPlayer player, MatchParams mp, String args[]) {
		return join(player,mp,args,false);
	}

	public boolean join(ArenaPlayer player, MatchParams mp, String args[], boolean adminJoin){
		/// Check if this match type is disabled
		if (disabled.contains(mp.getName())){
			sendMessage(player, "&cThe &6" + mp.getName()+"&c is currently disabled");
			final String enabled = ParamController.getAvaibleTypes(disabled);
			if (enabled.isEmpty()){
				return sendMessage(player, "&cThe arena system and all types are currently disabled");
			} else {
				return sendMessage(player, "&cEnabled &6" + enabled);
			}
		}
		/// Check Perms
		if (!player.hasPermission("arena."+mp.getName().toLowerCase()+".join") &&
				!player.hasPermission("arena."+mp.getCommand().toLowerCase()+".join") ){
			return sendMessage(player, "&cYou don't have permission to join a &6" + mp.getCommand());}

		/// Can the player join this match/event at this moment?
		if (!canJoin(player)){
			return true;}

		/// Make a team for the new Player
		Team t = teamc.getSelfFormedTeam(player);
		if (t==null)
			t = TeamController.createTeam(player);
		mp = new MatchParams(mp);
		JoinOptions jp;
		WantedTeamSizePair wtsr = null;
		try {
			jp = JoinOptions.parseOptions(mp,t, player, Arrays.copyOfRange(args, 1, args.length));
			wtsr = (WantedTeamSizePair) jp.getOption(JoinOption.TEAMSIZE);
			mp.setTeamSize(wtsr.size);
			t.setJoinPreferences(jp);
		} catch (InvalidOptionException e) {
			return sendMessage(player, e.getMessage());
		} catch (Exception e){
			e.printStackTrace();
			jp = null;
		}
		/// Check to make sure at least one arena can be joined at some time
		Arena arena = ac.getArenaByMatchParams(mp,jp);
		if (arena == null){
			if (!wtsr.manuallySet){
				arena = ac.getArenaByNearbyMatchParams(mp,jp);
				if (arena != null){
					mp.setMinTeamSize(arena.getParameters().getMinTeamSize());
					mp.setMaxTeamSize(arena.getParameters().getMaxTeamSize());
				}
			}
			if (arena == null){
				Map<Arena, List<String>> reasons = ac.getNotMachingArenaReasons(mp, jp);
				if (!reasons.isEmpty()){
					for (Arena a : reasons.keySet()){
						List<String> rs = reasons.get(a);
						if (!rs.isEmpty())
							return sendMessage(player,"&c"+rs.get(0));
					}
				}
				return sendMessage(player,"&cA valid "+ mp.toPrettyString()+"&c arena has not been built");
			}
		}

		final MatchTransitions ops = mp.getTransitionOptions();
		if (ops == null){
			return sendMessage(player,"&cThis match type has no valid options, contact an admin to fix ");}
		//		BTInterface bti = BTInterface.getInterface(mp);
		//		if (bti.isValid()){
		//			bti.updateRanking(t);
		//		}
		/// Check ready
		if(!ops.teamReady(t)){
			t.sendMessage(ops.getRequiredString("&eYou need the following to join"));
			return true;
		}

		/// Check entrance fee
		if (!checkFee(mp, player)){
			return true;}

		/// Add them to the queue
		QPosTeamPair qpp = ac.addToQue(t, mp);
		if (qpp.pos== -2){
			t.sendMessage("&cTeam queue was busy.  Try again in a sec.");
		} else if (qpp.pos == -1){
			t.sendMessage("&cAn arena has not been built yet for that size of team");
		} else {
			t.sendMessage("&eYou have joined the queue for the &6"+ mp.toPrettyString()+ " &e.");
			int nplayers = mp.getMinTeams()*mp.getMinTeamSize();
			if (qpp.playersInQueue < nplayers && qpp.pos > 0){
				t.sendMessage("&ePosition: &6" + qpp.pos +"&e. Match start when &6" + nplayers+"&e players join. &6"+qpp.playersInQueue+"/"+nplayers);
			} else if (qpp.pos > 0){
				t.sendMessage("&ePosition: &6" + qpp.pos +"&e. your match will start when an arena is free");
			} else {
				t.sendMessage("&eYour match will start when an arena is free");
			}
		}
		return true;
	}

	@MCCommand(cmds={"leave"}, inGame=true, usage="leave")
	public boolean leave(ArenaPlayer p) {
		if (!canLeave(p)){
			return true;
		}
		Match am = ac.getMatch(p);
		if (am != null){
			am.onLeave(p);
			return sendMessage(p,"&eYou have left the match &6");
		}

		Event aec = insideEvent(p);
		if (aec != null){
			aec.leave(p);
			return sendMessage(p,"&eYou have left the "+aec.getName()+" event&6");
		}

		/// Are they even in a queue?
		if(!(ac.isInQue(p))){
			return sendMessage(p,"&eYou are not currently in a queue, use /arena join");}
		Team t = teamc.getSelfFormedTeam(p); /// They are in the queue, they are part of a team
		ParamTeamPair qtp = ac.removeFromQue(p);
		if (t!= null && t.size() > 1){
			t.sendMessage("&6The team has left the &6"+qtp.q.getName()+" queue&e. &6"+ p.getName() +"&e issued the command");
		} else {
			sendMessage(p,"&eYou have left the queue for the &6" + qtp.q.getName() );
		}
		refundFee(qtp.q, t);
		return true;
	}

	@MCCommand(cmds={"cancel"},admin=true,exact=2,usage="cancel <arenaname or player>")
	public boolean arenaCancel(CommandSender sender, String[] args) {
		Player player = Util.findPlayer(args[1]);
		if (player != null){
			ArenaPlayer ap = PlayerController.toArenaPlayer(player);
			if (ac.cancelMatch(ap)){
				return sendMessage(sender,"&2You have canceled the match for &6" + player.getName());
			} else {
				return sendMessage(sender,"&cMatch couldnt be found for &6" + player.getName());
			}
		}
		String arenaName = args[1];
		if (arenaName.equalsIgnoreCase("all")){
			return cancelAll(sender);}
		Arena arena = ac.getArena(arenaName);
		if (arena == null){
			return sendMessage(sender,"&cArena "+ arenaName + " not found");
		}
		if (ac.cancelMatch(arena)){
			return sendMessage(sender,"&2You have canceled the match in arena &6" + arenaName);
		} else {
			return sendMessage(sender,"&cError cancelling arena match");
		}
	}

	private boolean cancelAll(CommandSender sender) {
		ac.purgeQueue();
		ac.cancelAllArenas();
		ec.cancelAll();
		return sendMessage(sender,"&2You have cancelled all matches/events");
	}

	@MCCommand(cmds={"status"}, admin=true,min=2,usage="status <arena or player>")
	public boolean arenaStatus(CommandSender sender, String[] args) {
		Match am = null;
		String pormatch = args[1];
		Arena a = ac.getArena(pormatch);
		Player player;
		if (a == null){
			player = Util.findPlayer(pormatch);
			if (player == null)
				return sendMessage(sender,"&eCouldnt find arena or player=" + pormatch);
			ArenaPlayer ap = PlayerController.toArenaPlayer(player);
			am = ac.getMatch(ap);
			if (am == null){
				return sendMessage(sender,"&ePlayer " + pormatch +" is not in a match");}
		} else {
			am = ac.getArenaMatch(a);
			if (am == null){
				return sendMessage(sender,"&earena " + pormatch +" is not being used in a match");}
		}
		return sendMessage(sender,am.getMatchInfo());
	}

	@MCCommand(cmds={"winner"}, admin=true,min=2, usage="winner <player>")
	public boolean arenaSetVictor(CommandSender sender, ArenaPlayer ap) {
		Match am = ac.getMatch(ap);
		if (am == null){
			return sendMessage(sender,"&ePlayer " + ap.getName() +" is not in a match");}
		am.setVictor(ap);
		return sendMessage(sender,"&6" + ap.getName() +" has now won the match!");
	}

	@MCCommand(cmds={"resetElo"}, op=true, usage="resetElo")
	public boolean resetElo(CommandSender sender, MatchParams mp){
		BTInterface bti = new BTInterface(mp);
		if (!bti.isValid()){
			return sendMessage(sender,"&eThere is no tracking for " +mp);}
		bti.resetStats();
		return sendMessage(sender,mp.getPrefix()+" &2Elo's and stats for &6"+mp.getName()+"&2 now reset");
	}

	@MCCommand(cmds={"setElo"}, admin=true, usage="setElo <player> <ranking score>")
	public boolean setElo(CommandSender sender, MatchParams mp, OfflinePlayer player, Integer elo) {
		BTInterface bti = new BTInterface(mp);
		if (!bti.isValid()){
			return sendMessage(sender,"&eThere is no tracking for " +mp);}
		if (bti.setRanking(player, elo))
			return sendMessage(sender,"&6" + player.getName()+"&e now has &6" + elo +"&e ranking");
		else
			return sendMessage(sender,"&6Error setting ranking");
	}

	@MCCommand(cmds={"rank"}, inGame=true)
	public boolean rank(Player sender,MatchParams mp) {
		BTInterface bti = new BTInterface(mp);
		if (!bti.isValid()){
			return sendMessage(sender,"&eThere is no tracking for a " + mp.toPrettyString());
		}
		String rankMsg = bti.getRankMessage(sender);
		return MessageUtil.sendMessage(sender, rankMsg);
	}

	@MCCommand(cmds={"rank"})
	public boolean rankOther(CommandSender sender,MatchParams mp, OfflinePlayer player) {
		BTInterface bti = new BTInterface(mp);
		if (bti.isValid()){
			return sendMessage(sender,"&eThere is no tracking for a " + mp.toPrettyString());
		}
		String rankMsg = bti.getRankMessage(player);
		return MessageUtil.sendMessage(sender, rankMsg);
	}

	@MCCommand(cmds={"top"})
	public boolean top(CommandSender sender,MatchParams mp, String[] args) {
		final int length = args.length;
		int teamSize = 1;
		int x = 5;
		if (length > 1)try {x= Integer.valueOf(args[1]); } catch (Exception e){
			return sendMessage(sender, "&e top length " + args[1] +" is not a number");
		}
		if (length > 2)try {teamSize= Integer.valueOf(args[length-1]); } catch (Exception e){
			return sendMessage(sender, "&e team size " + args[length-1] +" is not a number");
		}
		mp.setTeamSize(teamSize);
		return getTop(sender, x, mp);
	}

	public boolean getTop(CommandSender sender, int x, MatchParams mp) {
		if (x < 1 || x > 100){
			x = 5;}
		BTInterface bti = new BTInterface(mp);
		if (!bti.isValid()){
			return sendMessage(sender,"&eThere is no tracking for a " + mp.toPrettyString());
		}

		final String teamSizeStr = (mp.getMinTeamSize() > 1 ? "teamSize=&6" + mp.getMinTeamSize(): "");
		final String arenaString = mp.getType().toPrettyString(mp.getMinTeamSize(), mp.getMaxTeamSize());

		final String headerMsg = "&4Top {x} Gladiators in &6" +arenaString+ "&e " + teamSizeStr;
		final String bodyMsg ="&e#{rank}&4 {name} - {wins}:{losses}&6[{ranking}]";

		bti.printTopX(sender, x, mp.getMinTeamSize(), headerMsg, bodyMsg);
		return true;
	}

	@MCCommand(cmds={"check"}, inGame=true, usage="check")
	public boolean arenaCheck(ArenaPlayer p) {
		if(ac.isInQue(p)){
			QPosTeamPair qpp = ac.getCurrentQuePos(p);
			if (qpp != null){
				return sendMessage(p,"&e"+qpp.q.toPrettyString()+"&e Queue Position: "+
						" &6" + (qpp.pos+1) +"&e. &6"+qpp.playersInQueue+" &eplayers in queue");
			}
		}
		return sendMessage(p,"&eYou are currently not in any arena queues.");
	}

	@MCCommand(cmds={"delete"}, admin=true, usage="delete <arena name>")
	public boolean arenaDelete(CommandSender sender, Arena arena) {
		new ArenaDeleteEvent(arena).callEvent();
		ac.deleteArena(arena);
		BattleArena.saveArenas();
		return sendMessage(sender,ChatColor.GREEN+ "You have deleted the arena &6" + arena.getName());
	}

	@MCCommand(cmds={"save"}, admin=true)
	public boolean arenaSave(CommandSender sender) {
		BattleArena.saveArenas(true);
		return sendMessage(sender,"&eArenas saved");
	}

	@MCCommand(cmds={"reload"}, admin=true)
	public boolean arenaReload(CommandSender sender, MatchParams mp) {
		Plugin plugin = mp.getType().getPlugin();
		if (ac.hasRunningMatches()){
			sendMessage(sender, "&cYou can't reload the config while matches are running");
			return sendMessage(sender, "&cYou can use &6/arena cancel all &6to cancel all matches");
		}
		if (plugin == BattleArena.getSelf()){
			for (ArenaType type : ArenaType.getTypes(plugin)){
				ac.removeAllArenas(type);
			}
			MessageSerializer.loadDefaults();
			BattleArena.getSelf().reloadConfig();
			ArenaSerializer.loadAllArenas(plugin);
		} else {
			ac.removeAllArenas(mp.getType());
			ConfigSerializer.reloadConfig(plugin, mp.getType());
			MessageSerializer.reloadConfig(mp.getName());
			ArenaSerializer.loadAllArenas(plugin, mp.getType());
		}
		return sendMessage(sender, "&6" + plugin.getName()+"&e configuration reloaded");
	}

	@MCCommand(cmds={"info"}, exact=1, usage="info")
	public boolean arenaInfo(CommandSender sender, MatchParams mp) {
		String info= TransitionOptions.getInfo(mp, mp.getName());
		return sendMessage(sender, info);
	}

	@MCCommand(cmds={"info"}, op=true, usage="info <arenaname>", order=1)
	public boolean info(CommandSender sender, Arena arena) {
		sendMessage(sender, arena.toDetailedString());
		Match match = ac.getMatch(arena);
		if (match != null){
			List<String> strs = new ArrayList<String>();
			for (Team t: match.getTeams()){
				strs.add("&5 -&e" + t.getDisplayName());}
			sendMessage(sender, "Teams: " + StringUtils.join(strs,", "));
		}
		//		final BAEventController controller = BattleArena.getBAEventController();
		//		Event event = controller.getEvent(arena);
		return true;
	}

	public boolean watch(CommandSender sender, String args[]) {
		if(args.length != 1){
			sendMessage(sender,ChatColor.YELLOW + "Usage: /watch <arena name>");
			sendMessage(sender,ChatColor.YELLOW + "Usage: /watch leave : to exit");
			return true;
		}
		if (!(sender instanceof Player))
			return true;
		Player p = (Player) sender;

		String value = args[0];
		String pname = p.getName();
		if (value.equalsIgnoreCase("leave")){
			if (visitors.containsKey(pname)){
				Location loc = visitors.get(pname);
				TeleportController.teleport(p, loc);
				visitors.remove(pname);
				sendMessage(sender,ChatColor.YELLOW + "You have stopped watching the Arena");
				return true;
			} else {
				sendMessage(sender,ChatColor.YELLOW + "You aren't waching an Arena");
				return true;
			}
		} else {
			Arena arena = ac.getArena(value.toLowerCase());
			if (arena == null){
				sendMessage(sender,ChatColor.YELLOW + "That arena doesnt exist!");
				return true;
			}
			if (arena.getVisitorLoc() == null){
				sendMessage(sender,ChatColor.YELLOW + "That arena doesnt allow visitors!");
				return true;
			}
			if (visitors.containsKey(pname)){
				/// Already have their old location.. dont store it
			} else {
				visitors.put(pname, p.getLocation());
			}
			TeleportController.teleport(p, arena.getVisitorLoc());
			sendMessage(sender,ChatColor.YELLOW + "You are now watching " + arena.getName() +" /watch leave : to leave");
		}
		return true;
	}

	@MCCommand(cmds={"create"}, inGame=true, admin=true, min=2,usage="create <arena name> [team size] [# teams]")
	public boolean arenaCreate(CommandSender sender, MatchParams mp, String name, String[] args) {
		if (Defaults.DEBUG) for (int i =0;i<args.length;i++){System.out.println("args=" + i + "   " + args[i]);}
		final String strTeamSize = args.length>2 ? (String) args[2] : "1+";
		final String strNTeams = args.length>3 ? (String) args[3] : "2+";

		if (ac.getArena(name) != null){
			return sendMessage(sender, "&cThere is already an arena named &6"+name);}
		Player p = (Player) sender;

		ArenaParams ap = new ArenaParams(ArenaType.ANY, Rating.ANY);
		try{
			ap.setTeamSizes(MinMax.valueOf(strTeamSize));
			ap.setNTeams(MinMax.valueOf(strNTeams));
		} catch(Exception e){
			return sendMessage(sender,"That size not recognized.  Examples: 1 or 2 or 1-5 or 2+");
		}

		ap.setType(mp.getType());

		Arena arena = ArenaType.createArena(name, ap);
		arena.setSpawnLoc(0, p.getLocation());
		ac.addArena(arena);
		new ArenaCreateEvent(arena).callEvent();

		sendMessage(sender,"&2You have created the arena &6" + arena);
		sendMessage(sender,"&2A spawn point has been created where you are standing");
		sendMessage(sender,"&2You can add/change spawn points using &6/arena alter " + arena.getName()+" <1,2,...,x : which spawn>");
		BattleArena.saveArenas();
		return true;
	}

	@MCCommand(cmds={"alter"}, admin=true)
	public boolean arenaAlter(CommandSender sender, Arena arena, String[] args) {
		ArenaAlterController.alterArena(sender, arena, args);
		return true;
	}

	@MCCommand(cmds={"rescind"},inGame=true)
	public boolean duelRescind(ArenaPlayer player) {
		if (!dc.hasChallenger(player)){
			return sendMessage(player,"&cYou haven't challenged anyone!");}
		Duel d = dc.rescind(player);
		Team t = d.getChallengerTeam();
		t.sendMessage("&4[Duel] &6" + player.getDisplayName()+"&2 has cancelled the duel challenge!");
		for (ArenaPlayer ap: d.getChallengedPlayers()){
			sendMessage(ap, "&4[Duel] &6"+player.getDisplayName()+"&2 has cancelled the duel challenge!");
		}
		return true;
	}

	@MCCommand(cmds={"reject"},inGame=true)
	public boolean duelReject(ArenaPlayer player) {
		if (!dc.isChallenged(player)){
			return sendMessage(player,"&cYou haven't been invited to a duel!");}
		Duel d = dc.reject(player);
		Team t = d.getChallengerTeam();
		String timeRem = TimeUtil.convertSecondsToString(Defaults.DUEL_CHALLENGE_INTERVAL);
		t.sendMessage("&4[Duel] &cThe duel was cancelled as &6" + player.getDisplayName()+"&c rejected your offer");
		t.sendMessage("&4[Duel] &cYou can challenge them again in " + timeRem);
		for (ArenaPlayer ap: d.getChallengedPlayers()){
			if (ap == player)
				continue;
			sendMessage(ap, "&4[Duel] &cThe duel was cancelled as &6" + player.getDisplayName()+"&c rejected the duel");
		}
		sendMessage(player, "&4[Duel] &cYou rejected the duel, you can't be challenged again for&6 "+timeRem);
		return true;
	}

	@MCCommand(cmds={"accept"},inGame=true)
	public boolean duelAccept(ArenaPlayer player) {
		if (!canJoin(player)){
			return true;}
		Duel d = dc.getDuel(player);
		if (d == null){
			return sendMessage(player,"&cYou haven't been invited to a duel!");}
		Double wager = (Double) d.getDuelOptionValue(DuelOption.MONEY);
		if (wager != null){
			if (MoneyController.balance(player.getName()) < wager){
				sendMessage(player,"&4[Duel] &cYou don't have enough money to accept the wager!");
				dc.cancelFormingDuel(d, "&4[Duel]&6" + player.getDisplayName()+" didn't have enough money for the wager");
				return true;
			}
		}

		if (dc.accept(player) == null){
			return true;
		}
		Team t = d.getChallengerTeam();
		t.sendMessage("&4[Duel] &6" + player.getDisplayName()+"&2 has accepted your duel offer!");
		for (ArenaPlayer ap: d.getChallengedPlayers()){
			if (ap == player)
				continue;
			sendMessage(ap, "&4[Duel] &6"+player.getDisplayName()+"&2 has accepted the duel offer");
		}
		return sendMessage(player,"&cYou have accepted the duel!");
	}

	@MCCommand(cmds={"duel"},inGame=true)
	public boolean duel(ArenaPlayer player, MatchParams mp, String args[]) {
		if (!player.hasPermission("arena."+mp.getName().toLowerCase()+".duel") &&
				!player.hasPermission("arena."+mp.getCommand().toLowerCase()+".duel") ){
			return sendMessage(player, "&cYou don't have permission to duel in a &6" + mp.getCommand());}
		if (dc.isChallenged(player)){
			sendMessage(player,"&4[Duel] &cYou have already been challenged to a duel!");
			return sendMessage(player,"&4[Duel] &6/"+mp.getCommand()+" reject&c to cancel the duel before starting your own");
		}
		/// Can the player join this match/event at this moment?
		if (!canJoin(player)){
			return true;}
		if (EventController.isEventType(mp.getName())){
			return sendMessage(player,"&4[Duel] &cYou can't duel someone in an Event type!");}

		/// Parse the duel options
		DuelOptions duelOptions = null;
		try {
			duelOptions = DuelOptions.parseOptions(player, Arrays.copyOfRange(args, 1, args.length));
		} catch (InvalidOptionException e1) {
			return sendMessage(player, e1.getMessage());
		}
		Double wager = (Double) duelOptions.getOptionValue(DuelOption.MONEY);
		if (wager != null){
			if (MoneyController.balance(player.getName()) < wager){
				return sendMessage(player,"&4[Duel] You can't afford that wager!");}
		}

		/// Announce warnings
		for (ArenaPlayer ap: duelOptions.getChallengedPlayers()){
			if (!canJoin(ap)){
				return sendMessage(player,"&4[Duel] &6"+ap.getDisplayName()+"&c is in a match, event, or queue");}
			if (dc.isChallenged(ap)){
				return sendMessage(player,"&4[Duel] &6"+ap.getDisplayName()+"&c already has been challenged!");}
			if (!player.hasPermission("arena."+mp.getName().toLowerCase()+".duel") &&
					!player.hasPermission("arena."+mp.getCommand().toLowerCase()+".duel") ){
				return sendMessage(player, "&6"+ap.getDisplayName()+"&c doesn't have permission to duel in a &6" + mp.getCommand());}

			Long grace = dc.getLastRejectTime(ap);
			if (grace != null && System.currentTimeMillis() - grace < Defaults.DUEL_CHALLENGE_INTERVAL*1000){
				return sendMessage(player,"&4[Duel] &6"+ap.getDisplayName()+"&c can't be challenged for &6"+
						TimeUtil.convertMillisToString(Defaults.DUEL_CHALLENGE_INTERVAL*1000 - (System.currentTimeMillis() - grace)));}
			if (wager != null){
				if (MoneyController.balance(ap.getName()) < wager){
					return sendMessage(player,"&4[Duel] &6"+ap.getDisplayName()+"&c can't afford that wager!");}
			}
		}

		/// Get our team1
		Team t = TeamController.getTeam(player);
		if (t == null){
			t = TeamController.createTeam(player);
		}
		for (ArenaPlayer ap: t.getPlayers()){
			if (wager != null){
				if (MoneyController.balance(ap.getName()) < wager){
					return sendMessage(player,"&4[Duel] Your teammate &6"+ap.getDisplayName()+"&c can't afford that wager!");}
			}
		}
		mp.setMinTeams(2);
		mp.setMaxTeams(2);
		int size = duelOptions.getChallengedPlayers().size();
		mp.setMinTeamSize(Math.min(t.size(), size));
		mp.setMaxTeamSize(Math.max(t.size(), size));
		/// set our default rating
		mp.setRated(Defaults.DUEL_ALLOW_RATED ? mp.isRated() : false);
		/// allow specified options to overrule
		if (duelOptions.hasOption(DuelOption.RATED))
			mp.setRated(true);
		else if (duelOptions.hasOption(DuelOption.UNRATED))
			mp.setRated(false);
		JoinOptions jp = null;
		/// Check to make sure at least one arena can be joined at some time
		Arena arena = ac.getArenaByMatchParams(mp,jp);
		if (arena == null){
			return sendMessage(player,"&cA valid arena has not been built for a " + mp.toPrettyString());}
		final MatchTransitions ops = mp.getTransitionOptions();
		if (ops == null){
			return sendMessage(player,"&cThis match type has no valid options, contact an admin to fix ");}

		Duel duel = new Duel(mp,t, duelOptions);

		/// Announce to the 2nd team
		String t2 = duelOptions.getChallengedTeamString();
		for (ArenaPlayer ap : duelOptions.getChallengedPlayers()){
			String other = duelOptions.getOtherChallengedString(ap);
			if (!other.isEmpty()){
				other = "and "+other+" ";
			}
			sendMessage(ap, "&4["+mp.getName()+" Duel] &6"+t.getDisplayName()+"&2 "+
					MessageUtil.hasOrHave(t.size())+" challenged you "+other+"to a &6" + mp.getName()+" &2duel!");
			sendMessage(ap, "&4[Duel] &2Options: &6" + duelOptions.optionsString(mp));
			sendMessage(ap, "&4[Duel] &6/"+mp.getCommand()+" accept &2: to accept. &6"+ mp.getCommand()+" reject &e: to reject");
		}

		sendMessage(player,"&4[Duel] &2You have sent a challenge to &6" + t2);
		sendMessage(player,"&4[Duel] &2You can rescind by typing &6/" + mp.getCommand()+" rescind");
		dc.addOutstandingDuel(duel);
		return true;
	}

	@MCCommand(cmds={"list"})
	public boolean arenaList(CommandSender sender, MatchParams mp, String[] args) {
		boolean all = args.length > 1 && (args[1]).equals("all");

		Collection<Arena> arenas = ac.getArenas().values();
		HashMap<ArenaType,Collection<Arena>> arenasbytype = new HashMap<ArenaType,Collection<Arena>>();
		for (Arena arena : arenas){
			Collection<Arena> as = arenasbytype.get(arena.getArenaType());
			if (as ==null){
				as = new ArrayList<Arena>();
				arenasbytype.put(arena.getArenaType(),as);
			}
			as.add(arena);
		}
		if (arenasbytype.isEmpty()){
			sendMessage(sender,"There are no arenas for type " + mp.getName());}
		for (ArenaType at : arenasbytype.keySet()){
			if (!all && !at.matches(mp.getType()))
				continue;
			Collection<Arena> as = arenasbytype.get(at);
			if (!as.isEmpty()){
				sendMessage(sender,"------ Arenas for Event type " + at.toString()+" ------");
				for (Arena arena : as){
					sendMessage(sender,arena.toSummaryString());
				}
			}
		}
		if (!all)
			sendMessage(sender,"&6/arena list all: &e to see all arenas");
		return sendMessage(sender,"&6/arena info <arenaname>&e: for details on an arena");
	}

	public boolean canLeave(ArenaPlayer p){
		return true;
		//		Match am = ac.getMatch(p);
		//		if (am != null){
		//			sendMessage(p,ChatColor.YELLOW + "You cant leave during a match!");
		//			return false;
		//		}
		//
		//		/// Let the Arena Event controllers handle people inside of events
		//		Event aec = insideEvent(p);
		//		if (aec != null && !aec.canLeave(p)){
		//			sendMessage(p, "&eYou can't leave the &6"+aec.getName()+"&e while its &6"+aec.getState());
		//			return false;
		//		}
		//
		//		return true;
	}

	public boolean canJoin(ArenaPlayer p) {
		/// Inside MobArena?
		if (MobArenaInterface.hasMobArena()){
			if (MobArenaInterface.insideMobArena(p)){
				sendMessage(p,"&cYou need to finish with MobArena first!");
				return false;
			}
		}
		/// Inside an Event?
		Event ae = insideEvent(p);
		if (ae != null){
			sendMessage(p, "&eYou need to leave the Event first. &6/" + ae.getCommand()+" leave");
			return false;
		}
		/// Inside the queue waiting for a match?
		QPosTeamPair qpp = ac.getCurrentQuePos(p);
		if(qpp != null && qpp.pos != -1){
			sendMessage(p,"&eYou are already in the " + qpp.q.toPrettyString() + " queue.");
			String cmd = qpp.q.getCommand();
			sendMessage(p,"&eType &6/"+cmd+" leave");
			return false;
		}
		/// Inside a match?
		Match am = ac.getMatch(p);
		if (am != null){
			sendMessage(p,"&eYou are already in a match.");
			return false;
		}

		/// Inside a forming team?
		if (teamc.inFormingTeam(p)){
			FormingTeam ft = teamc.getFormingTeam(p);
			if (ft.isJoining(p)){
				sendMessage(p,"&eYou have been invited to the team. " + ft.getDisplayName());
				sendMessage(p,"&eType &6/team join|decline");
			} else if (!ft.hasAllPlayers()){
				sendMessage(p,"&eYour team is not yet formed. &6/team disband&e to leave");
				sendMessage(p,"&eYou are still missing " + Util.playersToCommaDelimitedString(ft.getUnjoinedPlayers()) + " !!");
			}
			return false;
		}
		if (dc.hasChallenger(p)){
			sendMessage(p,"&cYou need to rescind your challenge first! &6/arena rescind");
			return false;
		}
		Team t = TeamController.getTeamNotTeamController(p);
		if (t != null){
			sendMessage(p,"&cYou need to leave first.  &6/arena leave");
			return false;
		}
		return true;
	}

	public Event insideEvent(ArenaPlayer p) {
		return EventController.insideEvent(p);
	}

	public boolean checkFee(MatchParams pi, ArenaPlayer p) {
		Set<ArenaPlayer> players = null;

		Team t = teamc.getSelfFormedTeam(p);
		if (t != null){
			players = t.getPlayers();
		} else {
			players = new HashSet<ArenaPlayer>();
			players.add(p);
		}
		final MatchTransitions tops = pi.getTransitionOptions();
		if (tops.hasEntranceFee()){
			Double fee = tops.getEntranceFee();
			if (fee == null || fee <= 0)
				return true;
			boolean hasEnough = true;
			for (ArenaPlayer player : players){
				boolean has = MoneyController.hasEnough(player.getName(), fee);
				hasEnough &= has;
				if (!has)
					sendMessage(player, "&eYou need &6"+fee+"&e to compete" );
			}
			if (!hasEnough){
				if (players.size() > 1){
					sendMessage(p,"&eYour team does not have enough money to compete");}
				return false;
			}
			for (ArenaPlayer player : players){
				MoneyController.subtract(player.getName(), fee);
				sendMessage(player, "&6"+fee+" has been subtracted from your account");
			}
		}
		return true;
	}

	public boolean refundFee(MatchParams pi, Team t) {
		final MatchTransitions tops = pi.getTransitionOptions();
		if (tops.hasEntranceFee()){
			Double fee = tops.getEntranceFee();
			if (fee == null || fee <= 0)
				return true;
			for (ArenaPlayer player : t.getPlayers()){
				MoneyController.add(player.getName(), fee);
				sendMessage(player, "&eYou have been refunded the entrance fee of &6"+fee );
			}
		}
		return true;
	}

	protected Arena getArena(String name){
		return ac.getArena(name);
	}

	@MCCommand( cmds = {"help","?"})
	public void help(CommandSender sender, Command command, String label, Object[] args){
		super.help(sender, command, args);
	}

	public static boolean checkPlayer(CommandSender sender) {
		if (!(sender instanceof Player)){
			sendMessage(sender, "&cYou need to be online for this command!");
			return false;
		}
		return true;
	}

	public void setDisabled(List<String> disabled) {
		this.disabled.addAll(disabled);
	}

	public Collection<String> getDisabled() {
		return this.disabled;
	}

}
