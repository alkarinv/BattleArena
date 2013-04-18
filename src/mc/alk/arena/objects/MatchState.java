package mc.alk.arena.objects;

/**
 * @author alkarin
 *
 * Enum of MatchTransitions, and MatchStates
 */
public enum MatchState implements CompetitionState{
	NONE("None"), DEFAULTS("defaults"),
	ONENTER("onEnter"), ONLEAVE("onLeave"), //ONENTERWAITROOM("onEnterWaitRoom"),
	INQUEUE("inQueue"),
	INCOURTYARD("inCourtyard"), INLOBBY("inLobby"), INWAITROOM("inWaitroom"),INARENA("inArena"),
	PREREQS ("preReqs"), ONJOIN ("onJoin"), ONOPEN("onOpen"),
	ONBEGIN("onBegin"), ONPRESTART ("onPrestart"), ONSTART ("onStart"), ONVICTORY ("onVictory"),
	ONCOMPLETE ("onComplete"), ONCANCEL ("onCancel"), ONFINISH("onFinish"),
	ONSPAWN ("onSpawn"), ONDEATH ("onDeath"),
	WINNER ("winner"),LOSERS ("losers"),
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
	public static MatchState fromName(String str){
		str = str.toUpperCase();
		return MatchState.valueOf(str);
	}

	public boolean isRunning() {
		return this == MatchState.ONSTART;
	}
}
