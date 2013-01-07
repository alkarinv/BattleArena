package mc.alk.arena.objects;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.objects.MatchResult.WinLossDraw;
import mc.alk.arena.objects.teams.Team;


public class CompetitionResult{
	Set<Team> victors = new HashSet<Team>();
	Set<Team> losers = new HashSet<Team>();
	Set<Team> drawers = new HashSet<Team>();
	WinLossDraw wld = WinLossDraw.UNKNOWN;

	public void setResult(WinLossDraw wld){
		this.wld = wld;
	}
	public void setVictor(Team vic) {
		this.victors.clear();
		this.victors.add(vic);
	}
	public void setVictors(Collection<Team> victors) {
		this.victors.clear();
		this.victors.addAll(victors);
	}
	public void setDrawers(Collection<Team> victors) {
		this.drawers.clear();
		this.drawers.addAll(victors);
	}

	public void setLosers(Collection<Team> losers) {
		this.losers.clear();
		this.losers.addAll(losers);
	}

	public void addLosers(Collection<Team> losers) {
		this.losers.addAll(losers);
	}

	public void addLoser(Team loser) {
		losers.add(loser);
	}

	public Set<Team> getVictors() {
		return victors;
	}

	public Set<Team> getLosers() {
		return losers;
	}

	public Set<Team> getDrawers(){
		return drawers;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("["+wld+",victor=" + victors + ",losers=" + losers +",drawers=" + drawers+"]");
		return sb.toString() + toPrettyString();
	}

	public String toPrettyString() {
		if (victors.isEmpty()){
			return "&eThere are no victors yet";}
		StringBuilder sb = new StringBuilder();
		for (Team t: victors){
			sb.append(t.getTeamSummary()+" ");}
		sb.append(" &ewins vs ");
		for (Team t: losers){
			sb.append(t.getTeamSummary()+" ");}

		return sb.toString();
	}

	public boolean isDraw() {
		return wld == WinLossDraw.DRAW;
	}
	public boolean isFinished(){
		return wld == WinLossDraw.WIN || wld == WinLossDraw.DRAW;
	}
	public boolean hasVictor() {
		return wld == WinLossDraw.WIN;
	}
}
