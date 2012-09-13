package mc.alk.arena.executors;

import mc.alk.arena.competition.events.ReservedArenaEvent;
import mc.alk.arena.competition.events.util.NeverWouldJoinException;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.EventOpenOptions;
import mc.alk.arena.objects.EventOpenOptions.EventOpenOption;
import mc.alk.arena.objects.EventOpenOptions.InvalidOptionException;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;

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

	@MCCommand(cmds={"open","auto"}, admin=true, order=1)
	public boolean open(CommandSender sender, String[] args) {
		openIt(sender,args);
		return true;
	}

	public boolean openIt(CommandSender sender, String[] args){
		if (!(event instanceof ReservedArenaEvent)){
			sendMessage(sender,"&4The Event " + event.getName() +" is not type ReservedArenaEvent");
			return false;
		}
		MatchParams mp = checkOpenOptions(sender,event, ParamController.getMatchParamCopy(event.getName()), args);
		
		ReservedArenaEvent rae = (ReservedArenaEvent) event;
		Arena arena;
		EventOpenOptions eoo = null;
		try {
			eoo = EventOpenOptions.parseOptions(args);
			arena = openEvent(rae, mp, eoo);
		} catch (InvalidOptionException e) {
			sendMessage(sender, e.getMessage());
			return false;
		} catch (NeverWouldJoinException e) {
			sendMessage(sender, e.getMessage());
			return false;
		} catch (Exception e){
			e.printStackTrace();
			return false;
		}

		final int max = arena.getParameters().getMaxPlayers();
		final String maxPlayers = max == ArenaParams.MAX ? "&6any&2 number of players" : max+"&2 players";
		sendMessage(sender,"&2You have "+eoo.getOpenCmd()+"ed a &6" + event.getDetailedName() +
				"&2 inside &6" + arena.getName() +" &2TeamSize=&6" + arena.getParameters().getTeamSizeRange() +"&2 #Teams=&6"+
				arena.getParameters().getNTeamRange() +"&2 supporting "+maxPlayers +"&2 at &5"+arena.getName() );
		return true;
	}

	public Arena openEvent(ReservedArenaEvent rae, MatchParams mp, EventOpenOptions eoo) throws InvalidOptionException, NeverWouldJoinException{
		eoo.updateParams(mp);
		Arena arena = eoo.getArena(mp);
		//		System.out.println("mp = " + mp + "   sq = " + specificparams +"   teamSize="+teamSize +"   nTeams="+nTeams);
		arena.setParameters(mp);
		rae.setSilent(eoo.isSilent());
		if (eoo.hasOption(EventOpenOption.FORCEJOIN)){
			rae.openAllPlayersEvent(mp, arena);
		} else if (eoo.hasOption(EventOpenOption.AUTO)){
			rae.autoEvent(mp, arena, eoo.getSecTillStart(), eoo.getInterval());
		} else {
			rae.openEvent(mp, arena);
		}
		return arena;
	}

}
