package test.mc.alk.arena.objects;

import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.arenas.Arena;

public class TestArena extends Arena{

    MatchState state;

    public TestArena(String name){
		this.name = name;
	}

    @Override
    public MatchState getMatchState() {
        return state;
    }

    public void setMatchState(MatchState matchState) {
        this.state = matchState;
    }
}
