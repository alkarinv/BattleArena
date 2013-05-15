package mc.alk.arena.objects.scoreboard;

public enum ArenaDisplaySlot {
	SIDEBAR, PLAYER_LIST, BELOW_NAME, NONE;

	public ArenaDisplaySlot swap(){
		return ArenaDisplaySlot.swapValue(this);
	}
	public static ArenaDisplaySlot swapValue(ArenaDisplaySlot slot){
		switch(slot){
		case PLAYER_LIST:
			return ArenaDisplaySlot.SIDEBAR;
		case SIDEBAR:
			return ArenaDisplaySlot.PLAYER_LIST;
		case BELOW_NAME:
		case NONE:
		default:
			return null;
		}
	}
}
