package mc.alk.arena.executors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.EventController;
import mc.alk.arena.controllers.MessageController;
import mc.alk.arena.controllers.MoneyController;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.controllers.PlayerController;
import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.controllers.TeleportController;
import mc.alk.arena.events.Event;
import mc.alk.arena.match.Match;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.ArenaType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.ParamTeamPair;
import mc.alk.arena.objects.QPosTeamPair;
import mc.alk.arena.objects.Rating;
import mc.alk.arena.objects.TransitionOptions;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.teams.FormingTeam;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.serializers.ConfigSerializer;
import mc.alk.arena.util.BTInterface;
import mc.alk.arena.util.Util;
import mc.alk.arena.util.Util.MinMax;
import mc.alk.tracker.TrackerInterface;
import mc.alk.tracker.objects.StatType;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 
 * @author alkarin
 *
 */
public class BAExecutor extends CustomCommandExecutor  {

	TeamController teamc; 
	EventController ec;
	Map<String, Location> visitors = new HashMap<String, Location>();
	Set<String> disabled = new HashSet<String>();

	public BAExecutor(){
		super();
		this.ec = BattleArena.getEC();
		this.teamc = BattleArena.getTC();
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

	@MCCommand(cmds={"checkTypes"},admin=true,usage="disable")
	public boolean arenaCheckArenaTypes(CommandSender sender) {
		String types = ArenaType.getValidList();
		return sendMessage(sender, "&5 valid types are = &6"+types);
	}

	@MCCommand(cmds={"join"},inGame=true,usage="join")
	public boolean join(ArenaPlayer player, MatchParams mp) {
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
		if (!(player.hasPermission("arena."+mp.getCommand()+".join"))){
			return sendMessage(player, "&cYou don't have permission to join a &6" + mp.getCommand());}
		
		/// Can the player join this match/event at this moment?
		if (!canJoin(player)){
			return true;}
		
		/// Make a team for the new Player
		Team t = teamc.getSelfTeam(player);
		if (t==null)
			t = TeamController.createTeam(player);
		final int teamSize = t == null ? 1 : t.size();

		mp.setTeamSize(teamSize);
		
		/// Find a valid arena
		Arena arena = ac.getArenaByMatchParams(mp);
		if (arena == null){
			return sendMessage(player,"A valid arena has not been built for a " + mp.toPrettyString());}
		final MatchTransitions ops = mp.getTransitionOptions();
		if (ops == null){
			return sendMessage(player,"This match type has no valid options, contact an admin to fix ");}

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
			t.sendMessage("&eTeam queue was busy.  Try again in a sec.");
		} else if (qpp.pos == -1){
			t.sendMessage("&eAn arena has not been built yet for that size of team");			
		} else {
			t.sendMessage("&eYou have joined the queue for the &6"+ mp.toPrettyString()+ " &e.");
			int nplayers = mp.getMinTeams()*mp.getMinTeamSize();
			if (qpp.pos < nplayers && qpp.pos > 0){
				t.sendMessage("&ePosition: &6" + qpp.pos +"&e. match will start when &6" + nplayers+"&e players join");				
			} else if (qpp.pos > 0){				
				t.sendMessage("&ePosition: &6" + qpp.pos +"&e. your match will start when an arena is free");				
			} else {
				t.sendMessage("&eYour match will start when an arena is free");				
			}
		}
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
		String arenaName = (String) args[1];
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
		ac.cancelAllArenas();
		ec.cancelAll();
		return sendMessage(sender,"&2You have cancelled all matches/events");						
	}

	@MCCommand(cmds={"verify"}, op=true,usage="verify")
	public boolean arenaVerify(CommandSender sender) {
		String[] lines = ac.toDetailedString().split("\n");
		for (String line : lines){
			sendMessage(sender,line);}
		return true;
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
		TrackerInterface bti = BTInterface.getInterface(mp);
		if (bti == null){
			return sendMessage(sender,"&eThere is no tracking for " +mp);}
		bti.resetStats();
		return sendMessage(sender,mp.getPrefix()+" &2Elo's and stats for &6"+mp.getName()+"&2 now reset");
	}

	@MCCommand(cmds={"setElo"}, admin=true, usage="setElo <player> <ranking score>")
	public boolean setElo(CommandSender sender, MatchParams mp, OfflinePlayer player, Integer elo) {
		TrackerInterface bti = BTInterface.getInterface(mp);
		if (bti == null){
			return sendMessage(sender,"&eThere is no tracking for " +mp);}
		if (bti.setRanking(player, elo))
			return sendMessage(sender,"&6" + player.getName()+"&e now has &6" + elo +"&e ranking");
		else 
			return sendMessage(sender,"&6Error setting ranking");
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

	public boolean getTop(CommandSender sender, int x, MatchParams pi) {
		if (x < 1 || x > 100){
			x = 5;}
		TrackerInterface bti = BTInterface.getInterface(pi);
		if (bti == null){
			System.err.println("BattleArena couldnt find interface for " + pi);
			return sendMessage(sender,"&eThere is no tracking for a " + pi.toPrettyString());
		}

		final String teamSizeStr = (pi.getMinTeamSize() > 1 ? "teamSize=&6" + pi.getMinTeamSize(): "");
		final String arenaString = pi.getType().toPrettyString(pi.getMinTeamSize());

		final String headerMsg = "&4Top {x} Gladiators in &6" +arenaString+ "&e " + teamSizeStr;
		final String bodyMsg ="&e#{rank}&4 {name} - {wins}:{losses}&6[{ranking}]";

		bti.printTopX(sender, StatType.RANKING, x, pi.getMinTeamSize(), headerMsg, bodyMsg);
		return true;
	}

	@MCCommand(cmds={"leave"}, inGame=true, usage="leave")
	public boolean arenaLeave(ArenaPlayer p) {
		if (!canLeave(p)){
			return true;
		}
		/// Are they even in a queue?
		if(!(ac.isInQue(p))){
			return sendMessage(p,"&eYou are not currently in a queue, use /arena join");}
		Team t = teamc.getSelfTeam(p); /// They are in the queue, they are part of a team
		ParamTeamPair qtp = ac.removeFromQue(p);
		if (t!= null && t.size() > 1){
			t.sendMessage("&6The team has left the &6"+qtp.q.getName()+" queue&e. &6"+ p.getName() +"&e issued the command");	
		} else {
			sendMessage(p,"&eYou have left the queue for the &6" + qtp.q.getName() );	
		}
		refundFee(qtp.q, t);
		return true;
	}

	@MCCommand(cmds={"check"}, inGame=true, usage="check")
	public boolean arenaCheck(ArenaPlayer p) {
		if(ac.isInQue(p)){
			QPosTeamPair qpp = ac.getCurrentQuePos(p);
			return sendMessage(p,"&eArena Queue Position: " + qpp.q.toPrettyString() + " " + (qpp.pos+1));
		}
		return sendMessage(p,"&eYou are currently not in any arena queues.");
	}

	@MCCommand(cmds={"delete"}, admin=true, usage="delete <arena name>")
	public boolean arenaDelete(CommandSender sender, Arena arena) {
		ac.removeArena(arena);
		return sendMessage(sender,ChatColor.GREEN+ "You have deleted the arena &6" + arena.getName());
	}

	@MCCommand(cmds={"save"}, admin=true)
	public boolean arenaSave(CommandSender sender, Command cmd, String commandLabel, Object[] args) {
		BattleArena.getSelf().saveArenas(true);
		return sendMessage(sender,"&eArenas saved");
	}

	@MCCommand(cmds={"reload"}, admin=true)
	public boolean arenaReload(CommandSender sender) {
		ac.removeAllArenas();
		ConfigSerializer.loadAll();
		MessageController.load();
		BattleArena.getSelf().loadArenas();
		return sendMessage(sender,"&eArena ymls reloaded");
	}

	@MCCommand(cmds={"info"}, exact=1, usage="info")
	public boolean arenaInfo(CommandSender sender, MatchParams mp) {
		String info= TransitionOptions.getInfo(mp, mp.getName());
		return sendMessage(sender, info);
	}

	@MCCommand(cmds={"info"}, op=true, usage="info <arenaname>", order=1)
	public boolean info(CommandSender sender, Arena arena) {
		return sendMessage(sender, arena.toDetailedString());
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

	@MCCommand(cmds={"alter"}, inGame=true, admin=true)
	public boolean arenaAlter(CommandSender sender, Arena arena, String[] args) {
		if (args.length < 3){
			sendMessage(sender,ChatColor.YELLOW+ "Usage: /arena alter <arenaname> <size|type|1|2|3...|vloc|waitroom> <value>");
			sendMessage(sender,ChatColor.YELLOW+ "Example: /arena alter MainArena teamSize 3+ ");
			sendMessage(sender,ChatColor.YELLOW+ "Example: /arena alter MainArena nTeams 2 ");
			sendMessage(sender,ChatColor.YELLOW+ "Example: /arena alter MainArena type deathmatch ");
			sendMessage(sender,ChatColor.YELLOW+ "      or /arena alter MainArena waitroom 1 ");
			//			sendMessage(sender,ChatColor.YELLOW+ "      or /arena alter MainArena spawnitem <itemname>:<matchEndTime between spawn> ");
			return true;
		}
		String arenaName = arena.getName();
		String changetype = args[2];
		String value = null;
		if (args.length > 3)
			value = (String) args[3];
		Player p = (Player) sender;
		if (Defaults.DEBUG) System.out.println("alterArena " + arenaName +":" + changetype + ":" + value);

		Location loc = null;
		int locindex = -1;
		try{locindex = Integer.parseInt(changetype);}catch(Exception e){}
		if (changetype.equalsIgnoreCase("teamSize")){
			final MinMax mm = Util.getMinMax(value);
			if (mm == null){
				sendMessage(sender,"size "+ value + " not found");
				return true;
			} else {
				ac.removeArena(arena);
				arena.getParameters().setTeamSizes(mm);
				ac.addArena(arena);
				sendMessage(sender,"&2Altered arena team size to &6" + value);
			}
		} 
		else if (changetype.equalsIgnoreCase("nTeams")){
			final MinMax mm = Util.getMinMax(value);
			if (mm == null){
				sendMessage(sender,"size "+ value + " not found");
				return true;
			} else {
				ac.removeArena(arena);
				arena.getParameters().setNTeams(mm);
				ac.addArena(arena);
				sendMessage(sender,"&2Altered arean number of teams to &6" + value);
			}
		} 
		else if (changetype.equalsIgnoreCase("type")){
			ArenaType t = ArenaType.fromString(value);
			if (t == null){
				sendMessage(sender,"&ctype &6"+ value + "&c not found. valid types=&6"+ArenaType.getValidList());
				return true;
			}
			ac.removeArena(arena);
			arena.getParameters().setType(t);
			ac.addArena(arena);
			sendMessage(sender,"&2Altered arena type to &6" + value);
		} else if (locindex > 0){
			ac.removeArena(arena);
			loc = parseLocation(p,value);
			if (loc == null){
				loc = p.getLocation();}
			arena.setSpawnLoc(locindex-1,loc);			
			ac.addArena(arena);
			sendMessage(sender,"&2spawn location &6" + changetype +"&2 set to location=&6" + Util.getLocString(loc));
		} else if (changetype.equalsIgnoreCase("v")){
			ac.removeArena(arena);
			loc = parseLocation(p,value);
			if (loc == null){
				loc = p.getLocation();}
			arena.setVisitorLoc(loc);			
			ac.addArena(arena);
			sendMessage(sender,"&2visitor spawn location set to location=&6" + Util.getLocString(loc));
		} else if (changetype.equalsIgnoreCase("waitroom") || changetype.equalsIgnoreCase("wr")){
//			System.out.println(" value = " + value);
			try{locindex = Integer.parseInt(value);}catch(Exception e){}
			if (locindex <= 0)
				return sendMessage(sender,"&cWait room spawn location &6" + value+"&c needs to be an integer. 1,2,..,x");

			ac.removeArena(arena);
			loc = p.getLocation();
			arena.setWaitRoomSpawnLoc(locindex-1,loc);			
			ac.addArena(arena);
			sendMessage(sender,"&2waitroom location &6" + locindex +"&2 set to location=&6" + Util.getLocString(loc));
		} else {
			sendMessage(sender,"&cType &6"+ changetype + "&c not found. Valid options are type|size|1|2|3...");
			return true;
		}
		BattleArena.getSelf().saveArenas();
		return true;
	}

	@MCCommand(cmds={"create"}, admin=true, min=2,usage="create <arena name> [team size] [# teams]")
	public boolean arenaCreate(CommandSender sender, MatchParams mp, String name, String[] args) {
		if (Defaults.DEBUG) for (int i =0;i<args.length;i++){System.out.println("args=" + i + "   " + args[i]);}

		final String strTeamSize = args.length>2 ? (String) args[2] : "1+";
		final String strNTeams = args.length>3 ? (String) args[3] : "2+";

		if (ac.getArena(name) != null){
			return sendMessage(sender, "&cThere is already an arena named &6"+name);}
		Player p = (Player) sender;

		ArenaParams ap = new ArenaParams(ArenaType.ANY, Rating.ANY);
		MinMax mm = Util.getMinMax(strTeamSize);
		if (mm == null){
			return sendMessage(sender,"That size not recognized.  Examples: 1 or 2 or 1-5 or 2+");}
		ap.setTeamSizes(mm);

		mm = Util.getMinMax(strNTeams);
		if (mm == null){
			return sendMessage(sender,"That size not recognized.  Examples: 1 or 2 or 1-5 or 2+");}
		ap.setNTeams(mm);
		ap.setType(mp.getType());
		Arena arena = ArenaType.createArena(name, ap);
		arena.setSpawnLoc(0, p.getLocation());
		ac.addArena(arena);

		sendMessage(sender,"&2You have created the arena &6" + arena);
		sendMessage(sender,"&2A spawn point has been created where you are standing");
		sendMessage(sender,"&2You can add/change spawn points using &6/arena alter " + arena.getName()+" <1,2,...,x : which spawn>");
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

		for (ArenaType at : arenasbytype.keySet()){
			if (!all && !at.matches(mp.getType()))
				continue;
			Collection<Arena> as = arenasbytype.get(at);
			if (!as.isEmpty()){
				sendMessage(sender,"------ Arenas for bukkitEvent type " + at.toString()+" ------");			
				for (Arena arena : as){
					sendMessage(sender,arena.toSummaryString());
				}				
			}
		}
		if (!all)
			sendMessage(sender,"&6/arena list all: &e to see all arenas");			
		return sendMessage(sender,"&6/arena info <arenaname>&e: for details on an arena");	
	}


	public Location parseLocation(CommandSender sender, String svl) {
		if (!(sender instanceof Player))
			return null;
		Player p = (Player) sender;
		//		System.out.println(" location=" + svl);
		if (svl == null)
			return null;
		String as[] = svl.split(":");
		if (svl.contains(",")){
			as = svl.split(",");
		}
		World w = p.getWorld();
		if (w == null){
			return null;}
		if (as.length < 3){
			return null;}
		try {
			int x = Integer.valueOf(as[0]);
			int y = Integer.valueOf(as[1]);
			int z = Integer.valueOf(as[2]);
			return new Location(w,x,y,z);
		} catch (Exception e){
			return null;
		}
	}

	public boolean canLeave(ArenaPlayer p){
		Match am = ac.getMatch(p);
		if (am != null){
			sendMessage(p,ChatColor.YELLOW + "You cant leave during a match!");
			return false;
		}

		/// Let the Arena bukkitEvent controllers handle people inside of events
		Event aec = insideEvent(p);
		if (aec != null && !aec.canLeave(p)){
			sendMessage(p, "&eYou can't leave the &6"+aec.getName()+"&e while its &6"+aec.getState());
			return false;
		}

		return true;
	}

	public boolean canJoin(ArenaPlayer p) {
		/// Inside an Event?
		Event ae = insideEvent(p);
		if (ae != null){
			sendMessage(p, "&eYou need to leave the bukkitEvent first. &6/" + ae.getCommand()+" leave");
			return false;
		}
		/// Inside the queue waiting for a match?
		QPosTeamPair qpp = ac.getCurrentQuePos(p);
		if(qpp.pos != -1){
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
		return true;
	}

	public Event insideEvent(ArenaPlayer p) {
		return EventController.insideEvent(p);
	}

	public boolean checkFee(MatchParams pi, ArenaPlayer p) {
		Set<ArenaPlayer> players = null;

		Team t = teamc.getSelfTeam(p);
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

}
