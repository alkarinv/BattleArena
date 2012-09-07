package mc.alk.arena.objects.victoryconditions;

import java.util.List;

import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.teams.Team;

public abstract class VictoryCondition extends ChangeStateCondition  {
	final Integer matchEndTime;
	final Integer matchTimeInterval;
	
	int neededTeams;
	final VictoryType vt;

	public VictoryCondition(Match match){
		super(match);
		final MatchParams mp = match.getParams();

		this.matchEndTime = mp.getMatchTime();
		this.matchTimeInterval = mp.getIntervalTime();
		this.neededTeams = mp.getMinTeams();
		this.vt = mp.getVictoryType();
	}

	public String toString(){
		return getName();
	}

	public int getNeededTeams() {
		return neededTeams;
	}

	public void timeExpired() {}

	public void timeInterval(int remaining) {}

	public Team currentLeader() {return null;}

	public List<List<Team>> rankings() {return null;}

	public abstract boolean hasTimeVictory();

	public Integer matchEndTime() {return matchEndTime;}

	public Integer matchUpdateInterval() { return matchTimeInterval;}

	public String getName() {
		return "[VC "+vt.getName()+"]";
	}

	public abstract void playerLeft(ArenaPlayer p);

}
