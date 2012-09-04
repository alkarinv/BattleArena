package mc.alk.arena.controllers.messaging;

import java.util.Collection;
import java.util.Set;

import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.EventMessageHandler;
import mc.alk.arena.events.Event;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.BTInterface;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TimeUtil;
import mc.alk.tracker.TrackerInterface;

import org.bukkit.Bukkit;
import org.bukkit.Server;


/**
 * 
 * @author alkarin
 *
 */
public class EventMessageImpl extends MessageUtil implements EventMessageHandler {

	final MatchParams mp;
	final Event event;
	
	public EventMessageImpl(Event e ){
		this.mp = e.getParams();
		this.event = e;
	}
	
	@Override
	public void sendCountdownTillEvent(int seconds) {
		final String timeStr = TimeUtil.convertSecondsToString(seconds);
		final String msg = mp.getPrefix()+"&eStarts in " + timeStr +", &6/"+mp.getCommand()+" join&e, &6/"+ mp.getCommand()+" info";
		Bukkit.getServer().broadcastMessage(colorChat(msg));
	}
	
	@Override
	public void sendEventStarting(Collection<Team> teams) {
		final int nTeams = teams.size();
		Server server = Bukkit.getServer();
		int minTeamSize = 1;
		for (Team t: teams){
			if (t.size() > 1){
				minTeamSize = t.size();
				break;
			}
		}
		server.broadcastMessage(colorChat(mp.getPrefix()+"&6 " + nTeams + "&e "+
				teamsOrPlayers(minTeamSize)+" will compete in a &6"+mp.getCommand()+"&e Event!"));
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		TrackerInterface bti = BTInterface.getInterface(mp);
		for (Team t: teams){
			if (!first) sb.append("&e, ");
			Integer elo = (int) ((bti != null) ? BTInterface.loadRecord(bti, t).getRanking() : Defaults.DEFAULT_ELO);
			sb.append("&c"+t.getDisplayName()+"&6(" + elo +")");
			first = false;
		}
		final String msg = colorChat("&eParticipants: " + sb.toString());
		for (Team t: teams){
			for (ArenaPlayer p: t.getPlayers()){
				p.sendMessage(msg);
			}
		}
	}
	
	@Override
	public void sendEventWon(Team victor, Integer elo) {
		Bukkit.getServer().broadcastMessage(
				colorChat(mp.getPrefix()+"&e Congratulations to &c"+victor.getDisplayName()+"&6("+elo+")&e for winning!!"));		
	}
	
	@Override
	public void sendEventOpenMsg() {
		final String prefix = mp.getPrefix();
		Server server = Bukkit.getServer();
		server.broadcastMessage(Log.colorChat(prefix + "&e A " + mp.toPrettyString() +" Event is opening!"));
		server.broadcastMessage(Log.colorChat(prefix + "&e Type &6/" + mp.getCommand()+" join&e, or &6/" + mp.getCommand()+" info &efor info"));			
		if (mp.getSize() > 1){
			server.broadcastMessage(Log.colorChat(prefix + "&e You can join solo and you will be matched up, or you can create a team"));	
			server.broadcastMessage(Log.colorChat(prefix + "&e &6/team create <player1> <player2>..."));	
		}		
	}
	
	@Override
	public void sendEventCancelledDueToLackOfPlayers(Set<ArenaPlayer> competingPlayers) {
		EventMessageImpl.sendMessage(competingPlayers,mp.getPrefix()+"&e The Event has been cancelled b/c there weren't enough players");		
		
	}
	
	@Override
	public void sendTeamJoinedEvent(Team t) {
		t.sendMessage("&eYou have joined the &6" + mp.getName());		
	}
	
	@Override
	public void sendPlayerJoinedEvent(ArenaPlayer p) {
		sendMessage(p,"&eYou have joined the &6" + mp.getName());		
	}
	
	@Override
	public void sendEventCancelled() {
		Bukkit.broadcastMessage(EventMessageImpl.colorChat(mp.getPrefix()+"&e has been cancelled!"));		
	}
		
}
