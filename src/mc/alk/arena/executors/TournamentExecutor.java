package mc.alk.arena.executors;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.events.TournamentEvent;
import mc.alk.arena.events.util.NeverWouldJoinException;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.Rating;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.util.Util;
import mc.alk.arena.util.Util.MinMax;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class TournamentExecutor extends EventExecutor implements CommandExecutor {

	public TournamentExecutor(TournamentEvent tourney) {
		super();
		this.setEvent(tourney);
	}

	@MCCommand(cmds={"open","auto"},admin=true)
	public boolean tournamentOpen(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		boolean auto = (args[0]).equals("auto");
		String autoStr = auto? "auto" : "open";
		if (args.length < 5){
			sendMessage(sender,"&6/tourney "+autoStr+" <rated|unrated> <teamsize> <# players> <arena type> [# of minutes: default 4]");
			return sendMessage(sender,"&eExample &6/tourney "+autoStr+" rated 2 skirmish");
		}
		if (event.isRunning() || event.isOpen()){
			return sendMessage(sender,"&eA tournament has already started");}
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
		/// Params
		MatchParams mp = ParamController.getMatchParamCopy((String) args[4]);
		if (mp == null){
			return sendMessage(sender,"&cCouldn't find parameters for &6"+args[4]);}
		/// now we can finally make our tourney event
		
		mp.setTeamSizes(teamSize);
		mp.setNTeams(nTeams);
		mp.setRating(rated);
		/// Check to see if at least 1 arena matches these conditions
		Arena a = ac.getArenaByMatchParams(mp);
		if (a ==null){
			sendMessage(sender,"&cThere are no arenas that can handle the parameters you specified");
			return sendMessage(sender,"&cMatch params = " + mp);
		}
		try {
			if (auto){
				int seconds = Defaults.AUTO_EVENT_COUNTDOWN_TIME;
				if (args.length > 4){
					try {seconds = Integer.valueOf((String)args[4]) *60;} catch (Exception e){}}
				event.autoEvent(mp, seconds,Defaults.ANNOUNCE_EVENT_INTERVAL);
				return sendMessage(sender,"&eYou have autoed a " + event.getDetailedName());
			} else {
				event.openEvent(mp);
				return sendMessage(sender,"&eYou have opened a " + event.getDetailedName());			
			}
		} catch(NeverWouldJoinException e){
			return sendMessage(sender,ChatColor.RED+e.getMessage());
		}
	}
}
