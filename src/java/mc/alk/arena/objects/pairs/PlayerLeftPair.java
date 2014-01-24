package mc.alk.arena.objects.pairs;

public class PlayerLeftPair {
	final boolean left;
	final String msg;
	
	public PlayerLeftPair(boolean result, String msg){
		this.left = result;
		this.msg = msg;
	}
	public PlayerLeftPair(boolean result){
		this.left = result;
		this.msg = null;
	}
}
