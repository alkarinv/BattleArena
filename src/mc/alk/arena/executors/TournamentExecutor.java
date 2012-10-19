package mc.alk.arena.executors;

import java.util.Arrays;
import java.util.HashSet;

import mc.alk.arena.competition.events.TournamentEvent;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.EventOpenOptions;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.Exceptions.InvalidOptionException;
import mc.alk.arena.objects.Exceptions.NeverWouldJoinException;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class TournamentExecutor extends EventExecutor implements CommandExecutor {

	public TournamentExecutor(TournamentEvent tourney) {
		super();
		this.setEvent(tourney);
	}

	@MCCommand(cmds={"open","auto"},admin=true)
	public boolean open(CommandSender sender, String[] args) {
		openIt(sender,args);
		return true;
	}

	public boolean openIt(CommandSender sender, String[] args){
		if (!(event instanceof TournamentEvent)){
			sendMessage(sender,"&4The Event " + event.getName() +" is not type TournamentEvent");
			return false;
		}
		if (args.length < 2){
			sendMessage(sender,"&cIncorrect command: &6/tourney <open|auto> <matchType> [options...]");
			sendMessage(sender,"&cExample: &6/tourney auto arena");
			return false;
		}
		if (event.isOpen()){
			return sendMessage(sender,"&4The Tournament Event " + event.getName() +" is already open");
		}
		MatchParams mp = ParamController.getMatchParamCopy(args[1]);
		if (mp == null){
			sendMessage(sender, "&6" + args[1] +"&c is not a valid match type!");
			return sendMessage(sender,"&cCommand: &6/tourney <open|auto> <matchType> [options...]");
		}		
		if (!checkOpenOptions(sender,event,mp , args)){
			return false;
		}
		EventParams ep = new EventParams(mp);

		EventOpenOptions eoo = null;
		
		try {
			HashSet<Integer> ignoreArgs = new HashSet<Integer>(Arrays.asList(1)); /// ignore the matchType argument
			eoo = EventOpenOptions.parseOptions(args,ignoreArgs);
			openEvent(event, ep, eoo);
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
		final int max = mp.getMaxPlayers();
		final String maxPlayers = max == ArenaParams.MAX ? "&6any&2 number of players" : max+"&2 players";
		sendMessage(sender,"&2You have "+eoo.getOpenCmd()+"ed a &6" + event.getDetailedName() +
				" &2TeamSize=&6" + mp.getTeamSizeRange() +"&2 #Teams=&6"+
				mp.getNTeamRange() +"&2 supporting "+maxPlayers);
		return true;
	}
}
