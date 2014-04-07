package mc.alk.arena.objects;

import mc.alk.arena.objects.teams.ArenaTeam;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;

/**
 * @author alkarin
 */
public class MatchResult implements CompetitionResult {
    Set<ArenaTeam> victors = new HashSet<ArenaTeam>();
    Set<ArenaTeam> losers = new HashSet<ArenaTeam>();
    Set<ArenaTeam> drawers = new HashSet<ArenaTeam>();
    WinLossDraw wld = WinLossDraw.UNKNOWN;
    SortedMap<Integer, Collection<ArenaTeam>> ranks;

    public MatchResult(){}
    public MatchResult(CompetitionResult r) {
        this.wld = r.getResult();
        victors.addAll(r.getVictors());
        losers.addAll(r.getLosers());
        drawers.addAll(r.getDrawers());
    }

    /**
     * Changes the outcome type of this match to the given type.
     * Example, adding winners to this match will not change the outcome,
     * unless this match is set to a WinLossDraw.WIN
     * @param wld The WinLossDraw type.
     */
    @Override
    public void setResult(WinLossDraw wld){
        this.wld = wld;
    }

    @Override
    public void setVictor(ArenaTeam vic) {
        this.victors.clear();
        this.victors.add(vic);
        wld = WinLossDraw.WIN;
    }

    @Override
    public void setVictors(Collection<ArenaTeam> victors) {
        this.victors.clear();
        this.victors.addAll(victors);
        wld = WinLossDraw.WIN;
    }

    @Override
    public void setDrawers(Collection<ArenaTeam> drawers) {
        this.drawers.clear();
        this.drawers.addAll(drawers);
        wld = WinLossDraw.DRAW;
    }

    @Override
    public void setLosers(Collection<ArenaTeam> losers) {
        this.losers.clear();
        this.losers.addAll(losers);
    }

    @Override
    public void addLosers(Collection<ArenaTeam> losers) {
        this.losers.addAll(losers);
    }

    @Override
    public void addLoser(ArenaTeam loser) {
        losers.add(loser);
    }

    @Override
    public Set<ArenaTeam> getVictors() {
        return victors;
    }

    @Override
    public Set<ArenaTeam> getLosers() {
        return losers;
    }

    @Override
    public void removeLosers(Collection<ArenaTeam> teams){
        losers.removeAll(teams);
    }

    @Override
    public void removeDrawers(Collection<ArenaTeam> teams){
        drawers.removeAll(teams);
    }
    @Override
    public void removeVictors(Collection<ArenaTeam> teams){
        victors.removeAll(teams);
    }

    @Override
    public Set<ArenaTeam> getDrawers(){
        return drawers;
    }

    @Override
    public String toString(){
        return "[" + wld + ",victor=" + victors + ",losers=" + losers + ",drawers=" + drawers + "]" + toPrettyString();
    }

    @Override
    public String toPrettyString() {
        if (victors.isEmpty()){
            return "&eThere are no victors yet";}
        StringBuilder sb = new StringBuilder();
        for (ArenaTeam t: victors){
            sb.append(t.getTeamSummary()).append(" ");}
        sb.append(" &ewins vs ");
        for (ArenaTeam t: losers){
            sb.append(t.getTeamSummary()).append(" ");}

        return sb.toString();
    }

    @Override
    public boolean isUnknown() {
        return wld == WinLossDraw.UNKNOWN;
    }
    @Override
    public boolean isDraw() {
        return wld == WinLossDraw.DRAW;
    }
    @Override
    public boolean isWon(){
        return hasVictor();
    }
    @Override
    public boolean isLost() {
        return wld == WinLossDraw.LOSS;
    }
    @Override
    public boolean isFinished(){
        return wld == WinLossDraw.WIN || wld == WinLossDraw.DRAW;
    }
    @Override
    public boolean hasVictor() {
        return wld == WinLossDraw.WIN;
    }
    @Override
    public WinLossDraw getResult(){
        return wld;
    }

    @Override
    public SortedMap<Integer, Collection<ArenaTeam>> getRanking() {
        return ranks;
    }

    @Override
    public void setRanking(SortedMap<Integer, Collection<ArenaTeam>> ranks) {
        this.ranks = ranks;
    }
}
