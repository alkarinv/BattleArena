package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchFinishedEvent;
import mc.alk.arena.events.matches.MatchStartEvent;
import mc.alk.arena.events.matches.MatchVictoryEvent;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.util.Countdown;
import mc.alk.arena.util.Countdown.CountdownCallback;

public class TimeLimit extends VictoryCondition implements CountdownCallback {

	Countdown timer = null; /// Timer for when victory condition is time based
	public TimeLimit(Match match) {
		super(match);
	}

	@MatchEventHandler
	public void onStart(MatchStartEvent event){
		cancelTimers();
		timer = new Countdown(BattleArena.getSelf(),match.getParams().getMatchTime(), match.getParams().getIntervalTime(), this);
	}

	@MatchEventHandler
	public void onVictory(MatchVictoryEvent event){
		cancelTimers();
	}

	@MatchEventHandler
	public void onFinished(MatchFinishedEvent event){
		cancelTimers();
	}

	private void cancelTimers() {
		if (timer != null){
			timer.stop();
			timer =null;
		}
	}

	public boolean intervalTick(int remaining){
		if (remaining <= 0){
			match.timeExpired();
		} else {
			match.intervalTick(remaining);
		}
		return true;
	}


}
