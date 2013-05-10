package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchFinishedEvent;
import mc.alk.arena.events.matches.MatchResultEvent;
import mc.alk.arena.events.matches.MatchStartEvent;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.victoryconditions.interfaces.DefinesTimeLimit;
import mc.alk.arena.util.Countdown;
import mc.alk.arena.util.Countdown.CountdownCallback;

public class TimeLimit extends VictoryCondition implements DefinesTimeLimit, CountdownCallback {

	Countdown timer = null; /// Timer for when victory condition is time based
	public TimeLimit(Match match) {
		super(match);
	}

	@ArenaEventHandler(priority=EventPriority.LOW)
	public void onStart(MatchStartEvent event){
		cancelTimers();
		timer = new Countdown(BattleArena.getSelf(),match.getParams().getMatchTime(), match.getParams().getIntervalTime(), this);
	}

	@ArenaEventHandler(priority=EventPriority.LOW)
	public void onVictory(MatchResultEvent event){
		if (event.isMatchEnding())
			cancelTimers();
	}

	@ArenaEventHandler(priority=EventPriority.LOW)
	public void onFinished(MatchFinishedEvent event){
		cancelTimers();
	}

	private void cancelTimers() {
		if (timer != null){
			timer.stop();
			timer =null;
		}
	}

	@Override
	public boolean intervalTick(int remaining){
		if (remaining <= 0){
			match.timeExpired();
		} else {
			match.intervalTick(remaining);
		}
		return true;
	}

	@Override
	public int getMatchTime() {
		return match.getParams().getMatchTime();
	}
}
