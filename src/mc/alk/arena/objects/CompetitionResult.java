package mc.alk.arena.objects;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.objects.MatchResult.WinLossDraw;
import mc.alk.arena.objects.teams.Team;


/**
 * Represents the result of a competition.
 * Modifying this object will modify the outcome of the match.
 */
public class CompetitionResult{
	Set<Team> victors = new HashSet<Team>();
	Set<Team> losers = new HashSet<Team>();
	Set<Team> drawers = new HashSet<Team>();
	WinLossDraw wld = WinLossDraw.UNKNOWN;

	/**
	 * Changes the outcome type of this match to the given type.
	 * Example, adding winners to this match will not change the outcome,
	 * unless this match is set to a WinLossDraw.WIN
	 * @param wld The WinLossDraw type.
	 */
	public void setResult(WinLossDraw wld){
		this.wld = wld;
	}

	public void setVictor(Team vic) {
		this.victors.clear();
		this.victors.add(vic);
		wld = WinLossDraw.WIN;
	}

	public void setVictors(Collection<Team> victors) {
		this.victors.clear();
		this.victors.addAll(victors);
		wld = WinLossDraw.WIN;
	}

	public void setDrawers(Collection<Team> drawers) {
		this.drawers.clear();
		this.drawers.addAll(drawers);
		wld = WinLossDraw.DRAW;
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

	public boolean isUnknown() {
		return wld == WinLossDraw.UNKNOWN;
	}
	public boolean isDraw() {
		return wld == WinLossDraw.DRAW;
	}
	public boolean isWon(){
		return hasVictor();
	}
	public boolean isLost() {
		return wld == WinLossDraw.LOSS;
	}
	public boolean isFinished(){
		return wld == WinLossDraw.WIN || wld == WinLossDraw.DRAW;
	}
	public boolean hasVictor() {
		return wld == WinLossDraw.WIN;
	}
	public WinLossDraw getResult(){
		return wld;
	}
}
