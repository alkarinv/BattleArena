package mc.alk.arena.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alkarin
 *
 * Enum of MatchTransitions, and MatchStates
 */
public enum MatchState implements CompetitionState{
	NONE("None"), DEFAULTS("defaults"),
	ONENTER("onEnter"), ONLEAVE("onLeave"), //ONENTERWAITROOM("onEnterWaitRoom"),
	ONENTERARENA("onEnterArena"), ONLEAVEARENA("onLeaveArena"), //ONENTERWAITROOM("onEnterWaitRoom"),
	INQUEUE("inQueue"),INCOURTYARD("inCourtyard"),
	INLOBBY("inLobby"), INWAITROOM("inWaitroom"),INARENA("inArena"),INSPECTATE("inSpectate"),
    ONCREATE("onCreate"),PREREQS ("preReqs"), ONJOIN ("onJoin"), ONOPEN("onOpen"),
	ONBEGIN("onBegin"), ONPRESTART ("onPreStart"), ONSTART ("onStart"), ONVICTORY ("onVictory"),
	ONCOMPLETE ("onComplete"), ONCANCEL ("onCancel"), ONFINISH("onFinish"),
	ONSPAWN ("onSpawn"), ONDEATH ("onDeath"),
	WINNERS ("winners"), DRAWERS ("drawers"), LOSERS ("losers"),
	FIRSTPLACE ("firstPlace"), PARTICIPANTS("participants"),
	ONMATCHINTERVAL("onMatchInterval"), ONMATCHTIMEEXPIRED("onMatchTimeExpired"),
	ONCOUNTDOWNTOEVENT("onCountdownToEvent"),
	ONENTERQUEUE("onEnterQueue")
	;

	String name;
	MatchState(String name){
		this.name = name;
	}
	@Override
	public String toString(){
		return name;
	}

	public static MatchState fromString(String str){
		str = str.toUpperCase();
		try{
			return MatchState.valueOf(str);
		} catch (Exception e){
			if (str.equals("ONCOUNTDOWNTOEVENT")) return ONCOUNTDOWNTOEVENT;
			else if (str.equals("WINNER")) return WINNERS;
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

}
