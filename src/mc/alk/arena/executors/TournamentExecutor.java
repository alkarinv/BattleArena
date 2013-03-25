package mc.alk.arena.executors;

import java.util.Arrays;
import java.util.HashSet;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.competition.events.TournamentEvent;
import mc.alk.arena.controllers.BAEventController.SizeEventPair;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.exceptions.InvalidEventException;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.options.EventOpenOptions;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class TournamentExecutor extends EventExecutor implements CommandExecutor {

	public TournamentExecutor() {
		super();
	}

	@MCCommand(cmds={"open","auto"},admin=true)
	public boolean open(CommandSender sender, EventParams eventParams, String[] args) {
		try {
			openIt(sender,eventParams,args);
		} catch (InvalidEventException e) {
			sendMessage(sender,e.getMessage());
		} catch (Exception e){
			sendMessage(sender,e.getMessage());
			e.printStackTrace();
		}
		return true;
	}

	public Event openIt(CommandSender sender, EventParams eventParams, String[] args) throws InvalidEventException{
		SizeEventPair result = controller.getUniqueEvent(eventParams);
		Event event = result.event;
		if (event != null){
			sendMessage(sender,"&4There is already a tournament in progress");
			return null;
		}
		if (args.length < 2){
			sendMessage(sender,"&cIncorrect command: &6/tourney <open|auto> <matchType> [options...]");
			sendMessage(sender,"&cExample: &6/tourney auto arena");
			return null;
		}
		MatchParams mp = ParamController.getMatchParamCopy(args[1]);
		if (mp == null){
			sendMessage(sender, "&6" + args[1] +"&c is not a valid match type!");
			sendMessage(sender,"&cCommand: &6/tourney <open|auto> <matchType> [options...]");
			return null;
		}

		EventParams ep = new EventParams(mp);
		event = new TournamentEvent(eventParams);
		checkOpenOptions(event,ep , args);
		if (!isPowerOfTwo(ep.getMinTeams())){
			sendMessage(sender, "&cTournament nteams has to be a power of 2! like 2,4,8,16,etc");
			return null;
		}
		EventOpenOptions eoo = null;

		try {
			HashSet<Integer> ignoreArgs = new HashSet<Integer>(Arrays.asList(1)); /// ignore the matchType argument
			eoo = EventOpenOptions.parseOptions(args,ignoreArgs);
			openEvent(controller, event, ep, eoo);
		} catch (InvalidOptionException e) {
			sendMessage(sender, e.getMessage());
			return null;
		} catch (NeverWouldJoinException e) {
			sendMessage(sender, e.getMessage());
			return null;
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
		final int max = ep.getMaxPlayers();
		final String maxPlayers = max == ArenaParams.MAX ? "&6any&2 number of players" : max+"&2 players";
		sendMessage(sender,"&2You have "+eoo.getOpenCmd()+"ed a &6" + event.getDisplayName() +
				" &2TeamSize=&6" + ep.getTeamSizeRange() +"&2 #Teams=&6"+
				ep.getNTeamRange() +"&2 supporting "+maxPlayers);
		return event;
	}

    public static boolean isPowerOfTwo(int num)  {
       return num > 0 && (num == 1 || (num & 1) == 0 && isPowerOfTwo(num >> 1));
    }

	@MCCommand(cmds={"status"}, usage="status", order=1)
	public boolean eventStatus(CommandSender sender, EventParams eventParams, Arena arena, Integer round) {
		Event event = controller.getEvent(arena);
		if (event == null){
			return sendMessage(sender, "&cNo event could be found using that arena!");}
		StringBuilder sb = new StringBuilder(event.getStatus());
		return sendMessage(sender,sb.toString());
	}

	@MCCommand(cmds={"status"}, usage="status", order=2)
	public boolean eventStatus(CommandSender sender, EventParams eventParams, Integer round) {
		Event event = findUnique(sender, eventParams);
		if (event == null){
			return true;}
		StringBuilder sb = new StringBuilder(event.getStatus());
		return sendMessage(sender,sb.toString());
	}

}
