package mc.alk.arena.objects;

public class PlayerLeaveResult {
	final boolean left;
	final String msg;
	
	public PlayerLeaveResult(boolean result, String msg){
		this.left = result;
		this.msg = msg;
	}
	public PlayerLeaveResult(boolean result){
		this.left = result;
		this.msg = null;
	}
}
