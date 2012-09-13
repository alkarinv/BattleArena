package mc.alk.arena.executors;

import mc.alk.arena.competition.events.TournamentEvent;
import mc.alk.arena.competition.events.util.NeverWouldJoinException;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.EventOpenOptions;
import mc.alk.arena.objects.EventOpenOptions.InvalidOptionException;
import mc.alk.arena.objects.MatchParams;

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
		MatchParams mp = checkOpenOptions(sender,event, ParamController.getMatchParamCopy(args[1]), args);
		EventOpenOptions eoo = null;
		
		try {
			eoo = EventOpenOptions.parseOptions(args);
			openEvent(event, mp, eoo);
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
