package mc.alk.arena.executors;

import mc.alk.arena.Defaults;
import mc.alk.arena.events.ReservedArenaEvent;
import mc.alk.arena.events.util.NeverWouldJoinException;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.ArenaType;
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
	 * Constructor specifying the bukkitEvent to handle
	 * @param ReservedArenaEvent
	 */
	public ReservedArenaEventExecutor(ReservedArenaEvent ae){
		super();
		setEvent(ae);
	}

	/**
	 * set the bukkitEvent for this executor to handle 
	 * @param ReservedArenaEvent
	 */
	public void setEvent(ReservedArenaEvent ae){
		if (event != null){
			event.cancelEvent();}
		this.event = ae;
	}

	@MCCommand(cmds={"open","auto"}, admin=true, order=1)
	public boolean open(CommandSender sender, String[] args) {
		if (!(event instanceof ReservedArenaEvent)){
			return sendMessage(sender,"&4The bukkitEvent " + event.getName() +" is not type ReservedArenaEvent");			
		}
		ReservedArenaEvent rae = (ReservedArenaEvent) event;
		MatchParams mp = rae.getParams();

		boolean auto = args[0].equals("auto");
		final String openStr = (auto?"auto" : "open");
		final String cmd = mp.getCommand();
		if (args.length < 4){
			sendMessage(sender,"&6/"+cmd+" " + openStr +" <rated|unrated> <teamsize> <nteams> [arenaname] [# of minutes: default 4]");
			return sendMessage(sender,"&eExample &6/ "+cmd+" rated 2 versus");
		}
		boolean silent = args[args.length-1].toString().equalsIgnoreCase("silent");
		if (rae.isRunning() || rae.isOpen()){
			return sendMessage(sender,"&cA "+cmd+" is already &6" + rae.getState());
		}
		Rating rated = Rating.fromString(args[1]);
		if (rated == Rating.UNKNOWN){
			return sendMessage(sender,"&6"+args[1] +" &cNot a valid "+cmd+" type.  &6Rated &eor &6Unrated");}

		MinMax teamSize = Util.getMinMax(args[2]);
		if (teamSize == null){
			return sendMessage(sender,"&cCouldnt parse teamSize &6"+args[2]);}

		MinMax nTeams = Util.getMinMax(args[3]);
		if (nTeams == null){
			return sendMessage(sender,"&cCouldnt parse number of inEvent &6"+args[3]);}
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
				return sendMessage(sender,"&cCouldnt find an arena matching the params &6"+specificparams);}
			autoFindArena = true;
		}
		ArenaParams ap = arena.getParameters();
		if (!ap.getType().matches(specificparams.getType())){
			return sendMessage(sender,"&c The bukkitEvent &6"+rae.getName()+"&c uses arena types &6"+specificparams.getType().getCompatibleTypes()+
					"&c but the arena &6"+arena.getName()+"&c is type &6" +arena.getArenaType().getName());
		}
		if (!ap.matchesNTeams(specificparams)){
			return sendMessage(sender,"&c The bukkitEvent &6"+rae.getName()+"&c needs &6"+specificparams.getMinTeams()+
					"&c inEvent but the arena &6"+arena.getName()+"&c only supports &6" +ap.getMinTeams()+"&c inEvent");
		}
		if (!ap.matchesTeamSize(specificparams)){
			return sendMessage(sender,"&c The bukkitEvent &6"+rae.getName()+"&c needs team size &6"+specificparams.getTeamSizeRange()+
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
		arena = ArenaType.createArena(arena);
		if (!arena.valid()){
			return sendMessage(sender,"&c Arena is not valid.");}
		arena.setParameters(specificparams);
		try {
			if (silent) rae.setSilent(silent);
			if (auto){
				int seconds = Defaults.AUTO_EVENT_COUNTDOWN_TIME;
				if (args.length > 4){
					try {seconds = Integer.valueOf((String)args[4]) *60;} catch (Exception e){}}
				rae.autoEvent(specificparams, arena, seconds);
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
