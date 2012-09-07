package mc.alk.arena.executors;

import java.util.List;

import mc.alk.arena.Defaults;
import mc.alk.arena.competition.events.ReservedArenaEvent;
import mc.alk.arena.competition.events.util.NeverWouldJoinException;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.Rating;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.util.Util;
import mc.alk.arena.util.Util.MinMax;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ReservedArenaEventExecutor extends EventExecutor{
	/**
	 * Default constructor
	 * setEvent(...) should be called before using
	 */
	public ReservedArenaEventExecutor(){
		super();
	}

	/**
	 * Constructor specifying the Event to handle
	 * @param ReservedArenaEvent
	 */
	public ReservedArenaEventExecutor(ReservedArenaEvent ae){
		super();
		setEvent(ae);
	}

	/**
	 * set the Event for this executor to handle 
	 * @param ReservedArenaEvent
	 */
	public void setEvent(ReservedArenaEvent ae){
		if (event != null){
			event.cancelEvent();}
		this.event = ae;
	}

	@MCCommand(cmds={"open","auto","c"}, admin=true, order=1)
	public boolean open(CommandSender sender, String[] args) {
		if (!(event instanceof ReservedArenaEvent)){
			return sendMessage(sender,"&4The Event " + event.getName() +" is not type ReservedArenaEvent");			
		}
		ReservedArenaEvent rae = (ReservedArenaEvent) event;
		MatchParams params = ParamController.getMatchParamCopy(event.getName());
		if (params != null)
			rae.setParamInst(params); /// Update our params from ParamController
		MatchParams mp = rae.getParams();

		boolean continuous = args[0].equals("c");
		boolean auto = continuous || args[0].equals("auto") ;
		final String openStr = (auto?"auto" : "open");
		final String cmd = mp.getCommand();
		if (args.length < 1 || args.length > 6){
			sendMessage(sender,"&6/"+cmd+" " + openStr +" <rated|unrated> <teamsize> <nteams> [arenaname] [# of minutes: default 4]");
			return sendMessage(sender,"&eExample &6/ "+cmd+" rated 2 versus");
		}

		boolean silent = args[args.length-1].toString().equalsIgnoreCase("silent");
		if (rae.isRunning() || rae.isOpen()){
			return sendMessage(sender,"&cA "+cmd+" is already &6" + rae.getState());
		}
		/// Rated
		Rating rated = Rating.RATED;
		if (args.length>1){
			rated = Rating.fromString(args[1]);
			if (rated == Rating.UNKNOWN){
				return sendMessage(sender,"&6"+args[1] +" &cNot a valid "+cmd+" type.  &6Rated &eor &6Unrated");}
		}
		/// Team size
		MinMax teamSize = new MinMax(1,1);
		if (args.length>2){
			teamSize = Util.getMinMax(args[2]);
			if (teamSize == null){
				return sendMessage(sender,"&cCouldnt parse teamSize &6"+args[2]+" &e needs an &6integer");}
		}
		/// Number of Teams
		MinMax nTeams = new MinMax(2,ArenaParams.MAX);
		if (args.length > 3){
			nTeams = Util.getMinMax(args[3]);
			if (nTeams == null){
				return sendMessage(sender,"&cCouldnt parse number of teams &6"+args[3]+".&e Needs an integer or range. &68, 2+, 2-10, etc");}			
		}

		MatchParams specificparams = new MatchParams(mp);
		specificparams.setTeamSizes(teamSize);
		specificparams.setNTeams(nTeams);
		//		System.out.println("mp = " + mp + "   sq = " + specificparams +"   teamSize="+teamSize +"   nTeams="+nTeams);
		Arena arena;
		boolean autoFindArena = false;
		if (args.length> 4){
			arena = getArena(args[4]);			
			if (arena == null){
				return sendMessage(sender,"&cCouldnt find arena &6"+args[4]);}
		} else {
			arena = ac.getArenaByMatchParams(specificparams);
			if (arena == null){
				List<String> reasons = ac.getNotMachingArenaReasons(specificparams);
				sendMessage(sender,"&cCouldnt find an arena matching the params &6"+specificparams);
				for (String reason: reasons){
					sendMessage(sender,reason);					
				}
				return true;
			}
			autoFindArena = true;
		}
		ArenaParams ap = arena.getParameters();
		if (!ap.getType().matches(specificparams.getType())){
			return sendMessage(sender,"&c The event &6"+rae.getName()+"&c uses arena types &6"+specificparams.getType().getCompatibleTypes()+
					"&c but the arena &6"+arena.getName()+"&c is type &6" +arena.getArenaType().getName());
		}
		if (!ap.matchesNTeams(specificparams)){
			return sendMessage(sender,"&c The event &6"+rae.getName()+"&c needs &6"+specificparams.getMinTeams()+
					"&c players but the arena &6"+arena.getName()+"&c only supports &6" +ap.getMinTeams()+"&c players");
		}
		if (!ap.matchesTeamSize(specificparams)){
			return sendMessage(sender,"&c The event &6"+rae.getName()+"&c needs team size &6"+specificparams.getTeamSizeRange()+
					"&c but the arena &6"+arena.getName()+"&c supports team sizes &6" +ap.getTeamSizeRange());
		}

		final String arenaName = arena.getName();
		if (autoFindArena){
			arena = ac.nextArenaByMatchParams(specificparams);
			if (arena == null){
				return sendMessage(sender,"&cAll arenas matching those params are in use. wait till one is free ");}
		} else {
			arena = ac.reserveArena(arena);			
			if (arena == null){
				return sendMessage(sender,"&c Arena &6" +arenaName+"&c is currently in use, you'll have to wait till its free");}
		}
//		arena = ArenaType.createArena(arena);
		if (!arena.valid()){
			return sendMessage(sender,"&c Arena is not valid.");}
		arena.setParameters(specificparams);
		try {
			if (silent) rae.setSilent(silent);
			if (auto){
				int secTillStart = Defaults.AUTO_EVENT_COUNTDOWN_TIME;
				if (args.length > 4){
					try {secTillStart = Integer.valueOf((String)args[4]) *60;} catch (Exception e){}}
				int announceInterval = Defaults.ANNOUNCE_EVENT_INTERVAL;
				if (args.length > 5){
					try {secTillStart = Integer.valueOf((String)args[5]) *60;} catch (Exception e){}}
				if (continuous){
					rae.runContinuously(specificparams, arena, secTillStart,announceInterval);					
				} else {
					rae.autoEvent(specificparams, arena, secTillStart,announceInterval);					
				}
			} else {
				rae.openEvent(specificparams, arena);
			}
		} catch(NeverWouldJoinException e){
			rae.cancelEvent();
			return sendMessage(sender,ChatColor.RED+e.getMessage());
		}
		final int max = arena.getParameters().getMaxPlayers();
		final String maxPlayers = max == ArenaParams.MAX ? "&6any&2 number of players" : max+"&2 players";
		return sendMessage(sender,"&2You have "+openStr+"ed a &6" + rae.getDetailedName() +
				"&2 inside &6" + arena.getName() +" &2TeamSize=&6" + arena.getParameters().getTeamSizeRange() +"&2 #Teams=&6"+
				arena.getParameters().getNTeamRange() +"&2 supporting "+maxPlayers +"&2 at &5"+arena.getName() );			
	}

}
