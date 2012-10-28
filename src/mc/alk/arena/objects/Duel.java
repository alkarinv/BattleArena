package mc.alk.arena.objects;

import java.util.Collection;
import java.util.HashMap;

import mc.alk.arena.controllers.TeamController;
import mc.alk.arena.objects.options.DuelOptions;
import mc.alk.arena.objects.options.DuelOptions.DuelOption;
import mc.alk.arena.objects.teams.Team;

public class Duel {
	final MatchParams mp;

	final Team challenger;
	final HashMap<ArenaPlayer,Boolean> challengedPlayers = new HashMap<ArenaPlayer, Boolean>();
	final DuelOptions options;
	Double totalMoney = null;

	public Duel(MatchParams mp, Team challenger, DuelOptions options){
		this.mp = mp;
		this.challenger = challenger;
		this.options = options;
		for (ArenaPlayer ap: options.getChallengedPlayers()){
			challengedPlayers.put(ap, false);}
	}

	public boolean isChallenged(ArenaPlayer ap) {
		return challengedPlayers.containsKey(ap);
	}

	public boolean hasChallenger(ArenaPlayer player) {
		return challenger.hasMember(player);
	}

	public void accept(ArenaPlayer player) {
		challengedPlayers.put(player, true);
	}

	public Double getTotalMoney() {
		return totalMoney;
	}

	public void setTotalMoney(Double totalMoney) {
		this.totalMoney = totalMoney;
	}

	public boolean isReady() {
		for (Boolean r: challengedPlayers.values()){
			if (!r)
				return false;
		}
		return true;
	}

	public Team getChallengerTeam() {
		return challenger;
	}

	public Team makeChallengedTeam() {
		return TeamController.createCompositeTeam(challengedPlayers.keySet());
	}

	public MatchParams getMatchParams() {
		return mp;
	}

	public Collection<ArenaPlayer> getChallengedPlayers() {
		return challengedPlayers.keySet();
	}
	public Object getDuelOptionValue(DuelOption option){
		return options.getOptionValue(option);
	}
}
