package mc.alk.arena.objects;


public enum MatchState {
	ONOPEN("onOpen"), PREREQS ("preReqs"), ONJOIN ("onJoin"), ONPRESTART ("onPrestart"), ONSTART ("onStart"), ONVICTORY ("onVictory"),
	ONCOMPLETE ("onComplete"), ONCANCEL ("onCancel"), ONDEATH ("onDeath"), ONSPAWN ("onSpawn"), WINNER ("winner"),
	ONENTER("onEnter"), ONLEAVE("onLeave"), ONENTERWAITROOM("onEnterWaitRoom"),
	LOSERS ("losers"), FIRSTPLACE ("firstPlace"),
	NONE("None");
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
