package mc.alk.arena.objects;


import mc.alk.arena.controllers.StateController;

public enum EventState implements CompetitionState{
	CLOSED,OPEN,RUNNING, FINISHED;
    int globalOrdinal;

    EventState() {
        globalOrdinal = StateController.register(this);
    }

    public int globalOrdinal() {
        return globalOrdinal;
    }
}
