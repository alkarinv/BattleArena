package mc.alk.arena.objects.queues;

import mc.alk.arena.competition.util.TeamJoinFactory;
import mc.alk.arena.competition.util.TeamJoinHandler;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.exceptions.NeverWouldJoinException;
import mc.alk.arena.objects.options.JoinOptions;

import java.util.Collection;

public class WaitingObject {
    protected boolean joinable = true;
    protected final TeamJoinHandler jh;
    protected final MatchParams params;
    protected final QueueObject originalQueuedObject;
    protected final Arena arena;

    public WaitingObject(QueueObject qo) throws NeverWouldJoinException {
        this.params = qo.getMatchParams();
        this.originalQueuedObject = qo;
        this.arena = qo.getJoinOptions().getArena();
        if (qo instanceof MatchTeamQObject){
            this.jh = TeamJoinFactory.createTeamJoinHandler(qo.getMatchParams(), qo.getTeams());
            this.joinable = false;
        } else {
            this.jh = TeamJoinFactory.createTeamJoinHandler(qo.getMatchParams());
            this.joinable = true;
        }
    }

    public boolean matches(QueueObject qo) {
        return joinable &&
                (arena != null ?
                        arena.matches(qo.getMatchParams(), qo.getJoinOptions()) :
                        params.matchesIgnoreNTeams(qo.getMatchParams()));
    }

    public TeamJoinHandler.TeamJoinResult join(TeamJoinObject qo) {
        return jh.joiningTeam(qo);
    }

    public boolean hasEnough() {
        return jh.hasEnough(params.getAllowedTeamSizeDifference());
    }

    public boolean isFull() {
        return jh.isFull();
    }

    public MatchParams getParams() {
        return params;
    }

    public Arena getArena() {
        return arena;
    }

    public Collection<ArenaPlayer> getPlayers() {
        return jh.getPlayers();
    }

    public JoinOptions getJoinOptions() {
        return this.originalQueuedObject.getJoinOptions();
    }

    public Collection<ArenaListener> getArenaListeners(){
        return this.originalQueuedObject.getListeners();
    }

    public QueueObject getOriginalQueuedObject() {
        return originalQueuedObject;
    }

    public String toString() {
        return "[WO " + (arena != null ? arena.getName() : "") + params.getDisplayName() + "]";
    }
}
