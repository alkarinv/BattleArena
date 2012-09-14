package mc.alk.arena.objects;


/**
 * @author alkarin
 *
 * Enum of MatchTransitions, and MatchStates
 */
public enum MatchState {
	DEFAULTS("defaults"),
	ONOPEN("onOpen"), PREREQS ("preReqs"), ONJOIN ("onJoin"), ONPRESTART ("onPrestart"), ONSTART ("onStart"), ONVICTORY ("onVictory"),
	ONCOMPLETE ("onComplete"), ONCANCEL ("onCancel"), ONDEATH ("onDeath"), ONSPAWN ("onSpawn"), WINNER ("winner"),
	ONENTER("onEnter"), ONLEAVE("onLeave"), ONENTERWAITROOM("onEnterWaitRoom"),
	LOSERS ("losers"), FIRSTPLACE ("firstPlace"),
	ONMATCHINTERVAL("onMatchInterval"), ONMATCHTIMEEXPIRED("onMatchTimeExpired"),
	NONE("None"), ONCOUNTDOWNTOEVENT("onCountdownToEvent");
	String name;
	MatchState(String name){
		this.name = name;
	}
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
