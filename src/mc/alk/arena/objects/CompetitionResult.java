package mc.alk.arena.objects;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.objects.MatchResult.WinLossDraw;
import mc.alk.arena.objects.teams.ArenaTeam;


/**
 * Represents the result of a competition.
 * Modifying this object will modify the outcome of the match.
 */
public class CompetitionResult{
	Set<ArenaTeam> victors = new HashSet<ArenaTeam>();
	Set<ArenaTeam> losers = new HashSet<ArenaTeam>();
	Set<ArenaTeam> drawers = new HashSet<ArenaTeam>();
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

	public void setVictor(ArenaTeam vic) {
		this.victors.clear();
		this.victors.add(vic);
		wld = WinLossDraw.WIN;
	}

	public void setVictors(Collection<ArenaTeam> victors) {
		this.victors.clear();
		this.victors.addAll(victors);
		wld = WinLossDraw.WIN;
	}

	public void setDrawers(Collection<ArenaTeam> drawers) {
		this.drawers.clear();
		this.drawers.addAll(drawers);
		wld = WinLossDraw.DRAW;
	}

	public void setLosers(Collection<ArenaTeam> losers) {
		this.losers.clear();
		this.losers.addAll(losers);
	}

	public void addLosers(Collection<ArenaTeam> losers) {
		this.losers.addAll(losers);
	}

	public void addLoser(ArenaTeam loser) {
		losers.add(loser);
	}

	public Set<ArenaTeam> getVictors() {
		return victors;
	}

	public Set<ArenaTeam> getLosers() {
		return losers;
	}

	public void removeLosers(Collection<ArenaTeam> teams){
		losers.removeAll(teams);
	}

	public void removeDrawers(Collection<ArenaTeam> teams){
		drawers.removeAll(teams);
	}
	public void removeVictors(Collection<ArenaTeam> teams){
		victors.removeAll(teams);
	}

	public Set<ArenaTeam> getDrawers(){
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
		for (ArenaTeam t: victors){
			sb.append(t.getTeamSummary()+" ");}
		sb.append(" &ewins vs ");
		for (ArenaTeam t: losers){
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
