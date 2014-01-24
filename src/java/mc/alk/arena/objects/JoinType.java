package mc.alk.arena.objects;


public enum JoinType {
	QUEUE("Queue"), JOINPHASE("JoinPhase");

	final String name;
	private JoinType(String name){
		this.name = name;
	}
	@Override
	public String toString(){return name;}

	public static JoinType fromString(String str){
		str = str.toUpperCase();
		return JoinType.valueOf(str);
	}
}
