package mc.alk.arena.objects.scoreboard;

import mc.alk.scoreboardapi.scoreboard.SAPIDisplaySlot;

public enum ArenaDisplaySlot {
	SIDEBAR, PLAYER_LIST, BELOW_NAME, NONE;

	public SAPIDisplaySlot toSAPI() {
		switch(this){
		case BELOW_NAME: return SAPIDisplaySlot.BELOW_NAME;
		case NONE:return SAPIDisplaySlot.NONE;
		case PLAYER_LIST:return SAPIDisplaySlot.PLAYER_LIST;
		case SIDEBAR:return SAPIDisplaySlot.SIDEBAR;
		default: return null;
		}
	}
}
