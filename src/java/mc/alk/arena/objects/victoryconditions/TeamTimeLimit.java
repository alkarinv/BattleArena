package mc.alk.arena.objects.victoryconditions;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.events.matches.MatchFinishedEvent;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.WinLossDraw;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.objects.victoryconditions.interfaces.DefinesTimeLimit;
import mc.alk.arena.util.Countdown;
import mc.alk.arena.util.Countdown.CountdownCallback;

public class TeamTimeLimit extends VictoryCondition implements DefinesTimeLimit, CountdownCallback {

    Countdown timer; /// Timer for when victory condition is time based
    int announceInterval;
    final ArenaTeam team;

    public TeamTimeLimit(Match match, ArenaTeam team) {
        super(match);
        this.team = team;
    }
    public void startCountdown(){
        timer = new Countdown(BattleArena.getSelf(),match.getParams().getMatchTime(), announceInterval, this);
    }

    @SuppressWarnings("UnusedParameters")
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
        if (match.isEnding())
            return false;
        if (remaining <= 0) {
            MatchResult cr = new MatchResult();
            cr.setResult(WinLossDraw.LOSS);
            cr.addLoser(team);
            match.endMatchWithResult(cr);
        } else {
            match.intervalTick(remaining);
        }
        return true;
    }


    @Override
    public int getTime() {
        return match.getParams().getMatchTime();
    }
}
