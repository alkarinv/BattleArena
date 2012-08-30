package mc.alk.arena.controllers;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import mc.alk.arena.Defaults;
import mc.alk.arena.events.Event;
import mc.alk.arena.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.serializers.BroadcastOptions;
import mc.alk.arena.util.BTInterface;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TimeUtil;
import mc.alk.tracker.TrackerInterface;
import mc.alk.tracker.objects.Stat;
import mc.alk.tracker.objects.VersusRecords.VersusRecord;

import org.bukkit.Bukkit;
import org.bukkit.Server;

import com.dthielke.herochat.Channel;


/**
 * 
 * @author alkarin
 *
 */
public class MessageController extends MessageUtil implements MatchMessageHandler, EventMessageHandler {

	final MatchParams mp;
	final Match match;
	final Event event;
	
	public static BroadcastOptions bos = new BroadcastOptions();
	public MessageController(Match m ){
		this.mp = m.getParams();
		this.match = m;
		this.event = null;
	}
	public MessageController(Event e ){
		this.mp = e.getParams();
		this.match = null;
		this.event = e;
	}

	public void sendOnPreStartMsg(List<Team> teams, Arena arena) {
		if (teams.size()==2){
			Team t1=teams.get(0);
			Team t2 = teams.get(1);
			t1.sendMessage(mp.getSendMatchWillBeginMessage());
			t2.sendMessage(mp.getSendMatchWillBeginMessage());

			Integer t1Elo = Defaults.DEFAULT_ELO.intValue(), t2Elo = Defaults.DEFAULT_ELO.intValue();
			BTInterface bti = new BTInterface(mp);
			if (bti.isValid()){			
				t1Elo = bti.getElo(t1);
				t2Elo = bti.getElo(t2);
			}

			t1.sendMessage(getMessageNP("match","prestart2v2", t2.getDisplayName(),t2Elo, mp.getSecondsToLoot()));
			t2.sendMessage(getMessageNP("match","prestart2v2", t1.getDisplayName(),t1Elo, mp.getSecondsToLoot()));
			Server server = Bukkit.getServer();
			if (bos.broadcastOnPrestart()){
				String msg = null;
				if (mp.isRated()){
					msg = colorChat(mp.getPrefix()+" "+getMessageNP("match", "server_prestart2v2",t1.getDisplayName(),t1Elo, t2.getDisplayName(),t2Elo));
				} else {
					msg = getMessageAddPrefix(mp.getPrefix(),"skirmish", "prestart2v2", t1.getDisplayName(),t1Elo,t2.getDisplayName(), t2Elo);
				}
				if (BroadcastOptions.hasHerochat() && BroadcastOptions.getOnPrestartChannel() != null){
					Channel ch = BroadcastOptions.getOnPrestartChannel();
					ch.announce(msg);
				} else {
					server.broadcastMessage(msg);							
				}
			}

		} else {
			for (Team t: teams){
				t.sendMessage(mp.getSendMatchWillBeginMessage());
				t.sendMessage(getMessageNP("match","prestart", mp.getSecondsTillMatch()));
			}
		}
	}

	public void sendOnStartMsg(List<Team> teams) {
		for (Team t: teams){
			t.sendMessage(getMessageNP("match", "start"));			
		}
	}

	public void sendOnVictoryMsg(Team victor, Collection<Team> losers,  MatchParams q) {
		//		System.out.println("sendMatchWonMessage " + victor.getName() +" " + losers +"   inside of pi="+q);

		if (losers.size()==1){
			Team loser  = null;
			for (Team t: losers){loser = t;break;}
			Server server = Bukkit.getServer();
			Integer vWins = 0, lWins = 0;
			Integer vLosses = 0, lLosses = 0;
			Integer vWinsOver = 0, lWinsOver = 0;
			Integer vElo = Defaults.DEFAULT_ELO.intValue(), lElo = Defaults.DEFAULT_ELO.intValue();
			BTInterface bti = new BTInterface(q);
			if (bti != null && bti.isValid()){			
				Stat tsvictor = bti.loadRecord(victor);
				Stat tsloser = bti.loadRecord(loser);
				if (tsvictor != null && tsloser != null){ /// Should never happen but obviously tracker has bugs
					vWins = tsvictor.getWins(); lWins = tsloser.getWins();
					vLosses = tsvictor.getLosses(); lLosses= tsloser.getLosses();
					VersusRecord orvictor = tsvictor.getRecordVersus(loser.getName());
					if (orvictor != null){
						vWinsOver= orvictor.wins; lWinsOver= orvictor.losses;
					}				
					vElo = tsvictor.getRanking(); lElo = tsloser.getRanking();				
				}
			}
			if (bos.broadcastOnVictory()){
				final String msg = getMessageAddPrefix(q.getPrefix(),"match", "server_victory2v2",
						victor.getDisplayName(),vElo,vWins,vLosses, 
						loser.getDisplayName(),lElo, lWins ,lLosses );
				if (BroadcastOptions.hasHerochat() && BroadcastOptions.getOnVictoryChannel() != null){
					Channel ch = BroadcastOptions.getOnVictoryChannel();
					ch.announce(msg);
				} else {
					server.broadcastMessage(msg);							
				}
			}

			victor.sendMessage(getMessageNP("match","victor_message2v2_1",loser.getDisplayName(), lElo));
			victor.sendMessage(getMessageNP("match","victor_message2v2_2",vWins, vLosses));
			victor.sendMessage(getMessageNP("match","victor_message2v2_3",loser.getDisplayName(),vWinsOver, lWinsOver));
			victor.sendMessage(getMessageNP("match","victor_message2v2_4",mp.getSecondsToLoot()));

			loser.sendMessage(getMessageNP("match","loser_message2v2_1",victor.getDisplayName(), vElo));
			loser.sendMessage(getMessageNP("match","loser_message2v2_2",lWins, lLosses));
			loser.sendMessage(getMessageNP("match","loser_message2v2_3",victor.getDisplayName(),lWinsOver, vWinsOver));
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (ArenaPlayer p : victor.getPlayers()){
				if (!victor.hasAliveMember(p))
					continue;
				if (!first) sb.append(", ");
				sb.append("&8" +p.getDisplayName() +"&e(&4" + p.getHealth() + "&e)" );
			}
			loser.sendMessage(getMessageNP("match","loser_message2v2_4",sb.toString()));
		}	else {
//			victor.sendMessage("&eYou have vanquished all foes!! and have &6"+q.getSecondsToLoot()+"&e matchEndTime to loot!");
			victor.sendMessage("&eYou have vanquished all foes!! match will end in " + q.getSecondsToLoot());
			for (Team loser: losers){
				loser.sendMessage("&eYou have been vanquished by &6" + victor.getDisplayName()+"&e!!!");
			}
			BTInterface bti = new BTInterface(q);
			if (bti.isValid()){			
				victor.sendMessage(q.getPrefix()+"&e Your new rating is &6("+bti.getElo(victor)+")");			
				for (Team loser: losers){
					loser.sendMessage("&eYou have been vanquished by &6" + victor.getDisplayName()+"&e!!!");
					loser.sendMessage(q.getPrefix()+"&e Your new rating is &6("+bti.getElo(loser)+")");
				}
			}

		}
	}

	public void sendYourTeamNotReadyMsg(Team t1) {
		t1.sendMessage(getMessageNP("match","your_team_not_ready"));
	}

	public void sendOtherTeamNotReadyMsg(Team t1) {
		t1.sendMessage(getMessageNP("match","other_team_not_ready"));
	}
	public void sendCountdownTillEvent(int seconds) {
		final String timeStr = TimeUtil.convertSecondsToString(seconds);
		final String msg = mp.getPrefix()+"&eStarts in " + timeStr +", &6/"+mp.getCommand()+" join&e, &6/"+ mp.getCommand()+" info";
		Bukkit.getServer().broadcastMessage(colorChat(msg));
	}

	public void sendEventStarting(Collection<Team> teams) {
		final int nTeams = teams.size();
		Server server = Bukkit.getServer();
		server.broadcastMessage(colorChat(mp.getPrefix()+"&6 " + nTeams + "&e "+
				teamsOrPlayers(mp.getMinTeamSize())+" will compete in a &6"+mp.getCommand()+"&e Event!"));
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
	
	public void sendEventWon(Team victor, Integer elo) {
		Bukkit.getServer().broadcastMessage(
				colorChat(mp.getPrefix()+"&e Congratulations to &c"+victor.getDisplayName()+"&6("+elo+")&e for winning!!"));		
	}
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

	public void sendEventCancelledDueToLackOfPlayers(Set<ArenaPlayer> competingPlayers) {
		MessageController.sendMessage(competingPlayers,mp.getPrefix()+"&e The Event has been cancelled b/c there weren't enough players");		
		
	}
	public void sendTeamJoinedEvent(Team t) {
		t.sendMessage("&eYou have joined the &6" + mp.getName());		
	}
	public void sendPlayerJoinedEvent(ArenaPlayer p) {
		sendMessage(p,"&eYou have joined the &6" + mp.getName());		
	}
	
	public void sendEventCancelled() {
		Bukkit.broadcastMessage(MessageController.colorChat(mp.getPrefix()+"&e has been cancelled!"));		
	}
	
	public void sendOnIntervalMsg(int remaining) {
		Team h = match.getVictoryCondition().currentLeader();
		TimeUtil.testClock();
		final String timeStr = TimeUtil.convertSecondsToString(remaining);
		String msg;
		if (h == null){
			msg = match.getParams().getPrefix()+"&e ends in &4" +timeStr;			
		} else {
			msg = match.getParams().getPrefix()+"&e ends in &4" +timeStr +".&6"+
					h.getDisplayName()+"&e leads with &2" + h.getNKills() +"&e kills &4"+h.getNDeaths()+"&e deaths";				
		}
		match.sendMessage(msg);		
	}
	public void sendTimeExpired() {}
	
	
}
