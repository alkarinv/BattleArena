package mc.alk.arena.objects.signs;

import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.EventController;
import mc.alk.arena.executors.BAExecutor;
import mc.alk.arena.executors.EventExecutor;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.MatchParams;

public class ArenaCommandSign {
	public static enum ARENA_COMMAND{
		JOIN, LEAVE, START;
	}

	MatchParams mp;
	ARENA_COMMAND command;
	String options1;
	String options2;

	public ArenaCommandSign(MatchParams mp, ARENA_COMMAND cmd, String op1, String op2) {
		this.mp = mp;
		this.command = cmd;
		this.options1 = op1;
		this.options2 = op2;
	}

	public void performAction(ArenaPlayer player) {
		if (mp instanceof EventParams){
			performEventAction(player);
		} else {
			performMatchAction(player);
		}
	}

	private void performMatchAction(ArenaPlayer player) {
		BAExecutor executor = BattleArena.getBAExecutor();
		String args[];
		switch (command){
		case JOIN:
			args = new String[]{"join", options1,options2};
			executor.join(player, mp, args, true);
			break;
		case LEAVE:
			args = new String[]{"leave", options1,options2};
			executor.leave(player,mp,true);
			break;
		case START:
			break;
		}
	}

	private void performEventAction(ArenaPlayer player) {
		EventParams ep = (EventParams)mp;
		EventExecutor executor = EventController.getEventExecutor(ep.getType().getName());
		String args[];
		switch (command){
		case JOIN:
			args = new String[]{"join", options1,options2};
			executor.join(player, ep, args, true);
			break;
		case LEAVE:
			args = new String[]{"leave", options1,options2};
			executor.leave(player,ep,true);
			break;
		case START:
//			executor.start();
			break;
		}
	}

	public MatchParams getMatchParams() {
		return mp;
	}

	public ARENA_COMMAND getCommand() {
		return command;
	}

}
