package mc.alk.arena.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchCancelledEvent;
import mc.alk.arena.events.matches.MatchCompletedEvent;
import mc.alk.arena.listeners.TransitionListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.Duel;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.events.TransitionEventHandler;
import mc.alk.arena.objects.options.DuelOptions.DuelOption;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.objects.tournament.Matchup;
import mc.alk.arena.util.MessageUtil;

public class DuelController implements TransitionListener{
	List<Duel> formingDuels = new CopyOnWriteArrayList<Duel>();
	HashMap<String, Long> rejectTimers = new HashMap<String,Long>();
	HashMap<Matchup,Duel> ongoingDuels = new HashMap<Matchup,Duel>();

	public void addOutstandingDuel(Duel duel) {
		formingDuels.add(duel);
	}


	public Duel accept(ArenaPlayer player) {
		Duel d = getChallengedDuel(player);
		if (d != null){
			d.accept(player);
			if (d.isReady()){
				/// Do a final check to see if they have completed all the duel options
				if (!checkWager(d))
					return null;

				Team t = d.getChallengerTeam();
				Team t2 = d.makeChallengedTeam();
				List<Team> teams = new ArrayList<Team>();
				teams.add(t);
				teams.add(t2);
				Matchup m = new Matchup(d.getMatchParams(),teams);
				m.addTransitionListener(this);
				formingDuels.remove(d);
				ongoingDuels.put(m, d);
				BattleArena.getBAC().addMatchup(m);
			}
		}
		return d;
	}

	@TransitionEventHandler
	public void matchCancelled(MatchCancelledEvent event){
		Match match = event.getMatch();
		Matchup matchup = findMatchup(match);
		if (matchup == null)
			return;
		Duel d = ongoingDuels.remove(matchup);
		if (d == null)
			return;
		Double money = (Double) d.getDuelOptionValue(DuelOption.MONEY);
		if (money != null){
			refundMoney(money, match.getTeams());}
	}

	private void refundMoney(Double money, Collection<Team> teams) {
		for (Team t: teams){
			for (ArenaPlayer ap: t.getPlayers()){
				MessageUtil.sendMessage(ap,"&4[Duel] &6"+money+" "+Defaults.MONEY_STR+"&e has been refunded");
				MoneyController.add(ap.getName(), money);						
			}
		}		
	}

	@TransitionEventHandler
	public void matchComplete(MatchCompletedEvent event){
		Match match = event.getMatch();
		Matchup matchup = findMatchup(match);
		if (matchup == null)
			return;
		Duel d = ongoingDuels.remove(matchup);
		if (d == null)
			return;
		MatchResult mr = match.getResult();

		Double money = (Double) d.getDuelOptionValue(DuelOption.MONEY);
		if (money != null){
			if (mr.hasVictor()){
				Team t = mr.getVictor();
				double split = d.getTotalMoney() / t.size();
				for (ArenaPlayer ap: t.getPlayers()){
					MessageUtil.sendMessage(ap,"&4[Duel] &eYou have won &6" + split +" "+Defaults.MONEY_STR+"&e for your victory!");
					MoneyController.add(ap.getName(), split);
				}
			} else {
				refundMoney(money, mr.getDrawers());
			}
		}
	}

	public Matchup findMatchup(Match match){
		for (Matchup matchup: ongoingDuels.keySet()){
			if (matchup.getMatch().getID() == match.getID() ){
				return matchup;
			}
		}
		return null;
	}
	private boolean checkWager(Duel d) {
		Double wager = (Double) d.getDuelOptionValue(DuelOption.MONEY);
		if (wager == null)
			return true;
		HashSet<ArenaPlayer> players = new HashSet<ArenaPlayer>(d.getChallengedPlayers());
		players.addAll(d.getChallengerTeam().getPlayers());
		for (ArenaPlayer ap: players){
			if (MoneyController.balance(ap.getName()) < wager){
				MessageUtil.sendMessage(ap,"&4[Duel] &cYou don't have enough money to accept the wager!");
				cancelDuel(d, "&4[Duel]&6" + ap.getDisplayName()+" didn't have enough money for the wager");
				return false;
			} 		
		}
		for (ArenaPlayer ap: players){
			MessageUtil.sendMessage(ap,"&4[Duel] &6"+wager+" "+Defaults.MONEY_STR+"&e has been subtracted from your account");
			MoneyController.subtract(ap.getName(), wager);
		}
		d.setTotalMoney(((double)wager) * players.size());
		return true;
	}

	public Duel reject(ArenaPlayer player) {
		Duel d = getChallengedDuel(player);
		if (d != null){
			formingDuels.remove(d);
			rejectTimers.put(player.getName(), System.currentTimeMillis());
		}
		return d;
	}

	public boolean hasChallenger(ArenaPlayer player) {
		for (Duel d: formingDuels){
			if (d.hasChallenger(player)){
				return true;}
		}
		return false;
	}

	public Duel getDuel(ArenaPlayer player) {
		for (Duel d: formingDuels){
			if (d.hasChallenger(player)){
				return d;}
		}
		return getChallengedDuel(player);
	}

	public Duel getChallengedDuel(ArenaPlayer player) {
		for (Duel d: formingDuels){
			if (d.isChallenged(player)){
				return d;}
		}
		return null;
	}

	public boolean isChallenged(ArenaPlayer ap) {
		for (Duel d: formingDuels){
			if (d.isChallenged(ap))
				return true;
		}
		return false;
	}

	public Duel rescind(ArenaPlayer player) {
		Duel d = getDuel(player);
		if (d != null){
			formingDuels.remove(d);
		}
		return d;
	}


	public Long getLastRejectTime(ArenaPlayer ap) {
		Long t = rejectTimers.get(ap.getName());
		if (t == null)
			return t;
		if (Defaults.DUEL_CHALLENGE_INTERVAL*1000 < System.currentTimeMillis() - t){
			rejectTimers.remove(ap.getName());}
		return t;
	}


	public void cancelDuel(Duel d, String string) {
		// TODO Auto-generated method stub

	}

}
