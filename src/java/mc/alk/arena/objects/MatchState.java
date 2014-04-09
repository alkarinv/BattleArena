package mc.alk.arena.objects;

import mc.alk.arena.controllers.StateController;
import mc.alk.arena.objects.options.TransitionOption;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alkarin
 *
 * Enum of StateGraph, and MatchStates
 */
public enum MatchState implements CompetitionTransition{
	NONE("None"), DEFAULTS("defaults"),
	ONENTER("onEnter"), ONLEAVE("onLeave"), //ONENTERWAITROOM("onEnterWaitRoom"),
	ONENTERARENA("onEnterArena"), ONLEAVEARENA("onLeaveArena"), //ONENTERWAITROOM("onEnterWaitRoom"),
	INQUEUE("inQueue"),INCOURTYARD("inCourtyard"),
	INLOBBY("inLobby"), INWAITROOM("inWaitroom"),INSPECTATE("inSpectate"),
    INARENA("inArena"),
    ONCREATE("onCreate"),PREREQS ("preReqs"), ONJOIN ("onJoin"), INJOIN("inJoin"),
    ONOPEN("onOpen"), INOPEN("inOpen"),ONBEGIN("onBegin"),
    ONPRESTART ("onPreStart"), INPRESTART("inPrestart"),
    ONSTART ("onStart"), INGAME("inGame"),
    ONVICTORY ("onVictory"), INVICTORY("inVictory"),
	ONCOMPLETE ("onComplete"), ONCANCEL ("onCancel"), ONFINISH("onFinish"),
	ONSPAWN ("onSpawn"), ONDEATH ("onDeath"),
	WINNERS ("winners"), DRAWERS ("drawers"), LOSERS ("losers"),
	ONMATCHINTERVAL("onMatchInterval"), ONMATCHTIMEEXPIRED("onMatchTimeExpired"),
	ONCOUNTDOWNTOEVENT("onCountdownToEvent"),
	ONENTERQUEUE("onEnterQueue")
	;

    final String name;
    final int globalOrdinal;

    MatchState(String name){
		this.name = name;
        this.globalOrdinal = StateController.register(this);
    }

	@Override
	public String toString(){
		return name;
	}

    @Override
    public int globalOrdinal() {
        return globalOrdinal;
    }

    public static MatchState fromString(String str){
		str = str.toUpperCase();
		try{
			return MatchState.valueOf(str);
		} catch (Exception e){
			if (str.equals("ONCOUNTDOWNTOEVENT")) return ONCOUNTDOWNTOEVENT;
            else if (str.equals("WINNER")) return WINNERS;
            else if (str.equals("INSTART")) return INGAME;
			return null;
		}
	}

	public boolean isRunning() {
		return this == MatchState.ONSTART;
	}

	public static List<MatchState> getStates(MatchState beginState, MatchState endState) {
		List<MatchState> list = new ArrayList<MatchState>();
		boolean start = false;
		for (MatchState state : MatchState.values()){
			if (state == endState){
				break;}
			if (state == beginState){
				start = true;}
			if (start){
				list.add(state);}
		}
		return list;
	}

    public MatchState getCorrectState(StateOption option){
        if (!(option instanceof TransitionOption))
            return this;
        TransitionOption op = (TransitionOption) option;
        switch (this){
            case NONE:
                break;
            case DEFAULTS:
                break;
            case ONENTER:
                break;
            case ONLEAVE:
                break;
            case ONENTERARENA:
                break;
            case ONLEAVEARENA:
                break;
            case INQUEUE:
                break;
            case INCOURTYARD:
                break;
            case INLOBBY:
                break;
            case INWAITROOM:
                break;
            case INSPECTATE:
                break;
            case INARENA:
                break;
            case ONCREATE:
                break;
            case PREREQS:
                break;
            case ONJOIN:
                if (op.isState())
                    return INOPEN;
                break;
            case INJOIN:
                break;
            case ONOPEN:
                if (op.isState())
                    return INOPEN;
                break;
            case INOPEN:
                if (op.isTransition())
                    return INOPEN;
                break;
            case ONBEGIN:
                break;
            case ONPRESTART:
                if (op.isState())
                    return INPRESTART;
                break;
            case INPRESTART:
                if (op.isTransition())
                    return ONPRESTART;
                break;
            case ONSTART:
                if (op.isState())
                    return INGAME;
                break;
            case INGAME:
                if (op.isTransition())
                    return ONSTART;
                break;
            case ONVICTORY:
                if (op.isState())
                    return INVICTORY;
                break;
            case INVICTORY:
                if (op.isTransition())
                    return ONVICTORY;
                break;
            case ONCOMPLETE:
                break;
            case ONCANCEL:
                break;
            case ONFINISH:
                break;
            case ONSPAWN:
                break;
            case ONDEATH:
                break;
            case WINNERS:
                break;
            case DRAWERS:
                break;
            case LOSERS:
                break;
            case ONMATCHINTERVAL:
                break;
            case ONMATCHTIMEEXPIRED:
                break;
            case ONCOUNTDOWNTOEVENT:
                break;
            case ONENTERQUEUE:
                break;
        }
        return this;
    }

}
