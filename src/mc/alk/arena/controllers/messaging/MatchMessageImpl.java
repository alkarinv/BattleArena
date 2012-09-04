package mc.alk.arena.controllers.messaging;

import java.util.Collection;
import java.util.List;

import mc.alk.arena.Defaults;
import mc.alk.arena.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.messaging.Channel;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.util.BTInterface;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TimeUtil;
import mc.alk.tracker.objects.Stat;
import mc.alk.tracker.objects.VersusRecords.VersusRecord;


/**
 * 
 * @author alkarin
 *
 */
public class MatchMessageImpl extends MessageUtil implements MatchMessageHandler {

	final MatchParams mp;
	final Match match;
	

	public MatchMessageImpl(Match m ){
		this.mp = m.getParams();
		this.match = m;
	}
	
	@Override
	public void sendOnPreStartMsg(Channel serverChannel,List<Team> teams) {
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
			
			String serverMsg = null;
			if (mp.isRated()){
				serverMsg = colorChat(mp.getPrefix()+" "+getMessageNP("match", "server_prestart2v2",t1.getDisplayName(),t1Elo, t2.getDisplayName(),t2Elo));
			} else {
				serverMsg = getMessageAddPrefix(mp.getPrefix(),"skirmish", "prestart2v2", t1.getDisplayName(),t1Elo,t2.getDisplayName(), t2Elo);
			}
			serverChannel.broadcast(serverMsg);
			
		} else {
			for (Team t: teams){
				t.sendMessage(mp.getSendMatchWillBeginMessage());
				t.sendMessage(getMessageNP("match","prestart", mp.getSecondsTillMatch()));
			}
		}
	}

	@Override
	public void sendOnStartMsg(Channel serverChannel, List<Team> teams) {
		for (Team t: teams){
			t.sendMessage(getMessageNP("match", "start"));			
		}
		if (serverChannel != Channel.NullChannel)
			serverChannel.broadcast(getMessageNP("match","start"));
	}
	
	@Override
	public void sendOnVictoryMsg(Channel serverChannel, Team victor, Collection<Team> losers) {
		//		System.out.println("sendMatchWonMessage " + victor.getName() +" " + losers +"   inside of pi="+q);
		MatchParams q = match.getParams();
		if (losers.size()==1){
			Team loser  = null;
			for (Team t: losers){loser = t;break;}
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
					VersusRecord orvictor = tsvictor.getRecordVersus(tsloser);
					if (orvictor != null){
						vWinsOver= orvictor.wins; lWinsOver= orvictor.losses;
					}				
					vElo = tsvictor.getRanking(); lElo = tsloser.getRanking();				
				}
			}
			if (serverChannel != Channel.NullChannel){
				final String msg = getMessageAddPrefix(q.getPrefix(),"match", "server_victory2v2",
						victor.getDisplayName(),vElo,vWins,vLosses, 
						loser.getDisplayName(),lElo, lWins ,lLosses );
				serverChannel.broadcast(msg);
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
	
	public void sendOnIntervalMsg(Channel serverChannel, int remaining) {
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
	public void sendTimeExpired(Channel serverChannel) {}
	
}
