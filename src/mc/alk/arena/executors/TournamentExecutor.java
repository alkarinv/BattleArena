package mc.alk.arena.executors;

import java.util.Arrays;
import java.util.HashSet;

import mc.alk.arena.competition.events.Event;
import mc.alk.arena.competition.events.TournamentEvent;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.Exceptions.InvalidEventException;
import mc.alk.arena.objects.Exceptions.InvalidOptionException;
import mc.alk.arena.objects.Exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.options.EventOpenOptions;
import mc.alk.arena.util.KeyValue;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class TournamentExecutor extends EventExecutor implements CommandExecutor {

	public TournamentExecutor(TournamentEvent tourney) {
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
		KeyValue<Boolean,Event> result = controller.getUniqueEvent(eventParams);
		Event event = result.value;
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
		final int max = mp.getMaxPlayers();
		final String maxPlayers = max == ArenaParams.MAX ? "&6any&2 number of players" : max+"&2 players";
		sendMessage(sender,"&2You have "+eoo.getOpenCmd()+"ed a &6" + event.getDetailedName() +
				" &2TeamSize=&6" + mp.getTeamSizeRange() +"&2 #Teams=&6"+
				mp.getNTeamRange() +"&2 supporting "+maxPlayers);
		return event;
	}
}
