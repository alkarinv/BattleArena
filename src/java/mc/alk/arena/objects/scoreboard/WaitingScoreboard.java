package mc.alk.arena.objects.scoreboard;

import mc.alk.arena.objects.joining.JoinResponseHandler;

public interface WaitingScoreboard extends JoinResponseHandler {

    ArenaScoreboard getScoreboard();

    void setRemainingSeconds(int seconds);
}
