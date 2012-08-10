package mc.alk.arena.executors;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.events.TournamentEvent;
import mc.alk.arena.events.util.NeverWouldJoinException;
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
	public boolean tournamentOpen(CommandSender sender, Command cmd, String commandLabel, Object[] args) {
		boolean auto = ((String)args[0]).equals("auto");
		String autoStr = auto? "auto" : "open";
		if (args.length < 5){
			sendMessage(sender,"&6/tourney "+autoStr+" <rated|unrated> <teamsize> <# inEvent> <arena type> [# of minutes: default 4]");
			return sendMessage(sender,"&eExample &6/tourney "+autoStr+" rated 2 skirmish");
		}
		if (event.isRunning() || event.isOpen()){
			return sendMessage(sender,"&eA tournament has already started");}
		/// Rating
		Rating rating = Rating.fromString((String)args[1]);
		if (rating == null || rating == Rating.UNKNOWN){
			return sendMessage(sender,"&6"+args[1] +" &cNot a valid tournament type.  &6Rated &eor &6Unrated");}
		/// Team size
		MinMax teamSize = Util.getMinMax((String)args[2]);
		if (teamSize == null){
			return sendMessage(sender,"&6"+args[2] +" &cNot a valid size for tournament inEvent. needs an &6integer");}
		/// # Teams
		MinMax nTeams= Util.getMinMax((String)args[3]);
		if (nTeams == null){
			return sendMessage(sender,"&6"+args[3] +" &cNot a number of inEvent. needs an integer or range. &68, 2+, 2-10, etc");}
		/// Params
		MatchParams mp = ParamController.getMatchParams((String) args[4]);
		if (mp == null){
			return sendMessage(sender,"&cCouldn't find parameters for &6"+args[4]);}
		mp = new MatchParams(mp);
		/// now we can finally make our tourney bukkitEvent
		
		mp.setTeamSizes(teamSize);
		mp.setNTeams(nTeams);
		mp.setRating(rating);
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
				event.autoEvent(mp, seconds);
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
