package mc.alk.arena.executors;

import java.util.List;
import java.util.Map;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.competition.events.ReservedArenaEvent;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.EventState;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.exceptions.InvalidEventException;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.options.EventOpenOptions;
import mc.alk.arena.objects.options.EventOpenOptions.EventOpenOption;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.TimeUtil;

import org.apache.commons.lang.StringUtils;
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
	}

	@MCCommand(cmds={"open","auto"}, admin=true, order=1)
	public boolean open(CommandSender sender, EventParams eventParams, String[] args) {
		try {
			ReservedArenaEvent event = openIt(eventParams, args);
			Arena arena = event.getArena();
			final int max = arena.getParameters().getMaxPlayers();
			final String maxPlayers = max == ArenaParams.MAX ? "&6any&2 number of players" : max+"&2 players";
			sendMessage(sender,"&2You have "+args[0]+"ed a &6" + event.getDisplayName() +
					"&2 inside &6" + arena.getName() +" &2TeamSize=&6" + arena.getParameters().getTeamSizeRange() +"&2 #Teams=&6"+
					arena.getParameters().getNTeamRange() +"&2 supporting "+maxPlayers +"&2 at &5"+arena.getName() );
		} catch (InvalidEventException e) {
			sendMessage(sender, e.getMessage());
		} catch (InvalidOptionException e) {
			sendMessage(sender, e.getMessage());
		} catch (Exception e){
			sendMessage(sender, e.getMessage());
			Log.printStackTrace(e);
		}
		return true;
	}

	public ReservedArenaEvent openIt(EventParams eventParams, String[] args) throws InvalidEventException, InvalidOptionException{
		Event openevent = controller.getOpenEvent(eventParams);
		if (openevent != null){
			throw new InvalidEventException("&cThere is already an event open!");
		}
		if (!eventParams.valid()){
			throw new InvalidEventException("&cThe "+eventParams.getName()+" could not be opened due to the following reasons\n"+StringUtils.join(eventParams.getInvalidReasons(), ", "));}
		EventOpenOptions eoo = EventOpenOptions.parseOptions(args, null);
		Arena arena = eoo.getArena(eventParams,null);
		eventParams.intersect(arena.getParameters());

		arena.setParameters(eventParams);

		ReservedArenaEvent event = new ReservedArenaEvent(eventParams);

		checkOpenOptions(event, eventParams, args);
		openEvent(event, eventParams, eoo,arena);

		controller.addOpenEvent(event);
		return event;
	}

	@MCCommand(cmds={"ongoing"})
	public boolean eventOngoing(CommandSender sender, EventParams eventParams, String[] args) {
		Map<EventState, List<Event>> map = controller.getCurrentEvents(eventParams);
		sendMessage(sender,"&5----------- &2Ongoing Events &5-----------");
		for (EventState state: map.keySet()){
			List<Event> list = map.get(state);
			for (Event event: list){
				ReservedArenaEvent rae = (ReservedArenaEvent) event;
				Long opentime = rae.getTime(EventState.OPEN);
				Long starttime = rae.getTime(EventState.RUNNING);

				String openDate = opentime != null ? TimeUtil.convertLongToSimpleDate(opentime) : "N/A";
				String startDate = starttime != null ? TimeUtil.convertLongToSimpleDate(starttime) : "N/A";
				sendMessage(sender,"&2Arena=&5"+ rae.getArena().getName()+" &2 Opened: &6" + openDate +
						"  &2Started:&6"+startDate +"  &2state="+(state == EventState.OPEN ? "&a":"&c")+state);
			}
		}
		return true;
	}

	public static void openEvent(ReservedArenaEvent rae, EventParams ep, EventOpenOptions eoo, Arena arena) throws InvalidOptionException, InvalidEventException{
		if (rae.isOpen())
			throw new InvalidOptionException("&cThe event is already open");
		eoo.updateParams(ep);
		rae.setSilent(eoo.isSilent());
		if (eoo.hasOption(EventOpenOption.AUTO)){
			ep.setSecondsTillStart(eoo.getSecTillStart());
			ep.setAnnouncementInterval(eoo.getInterval());
			rae.autoEvent(ep, arena);
		} else {
			rae.openEvent(ep, arena);
		}
		if (eoo.hasOption(EventOpenOption.FORCEJOIN)){
			rae.addAllOnline();}

	}

}
