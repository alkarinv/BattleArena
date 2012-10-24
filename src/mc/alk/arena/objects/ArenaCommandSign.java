package mc.alk.arena.objects;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.events.Event;
import mc.alk.arena.controllers.EventController;
import mc.alk.arena.executors.BAExecutor;
import mc.alk.arena.executors.EventExecutor;
import mc.alk.arena.util.SignUtil.ARENA_COMMAND;

public class ArenaCommandSign {
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
		String args[] = {options1,options2};
		switch (command){
		case JOIN:
			executor.join(player, mp, args, true);
			break;
		case LEAVE:
			executor.leave(player);
			break;
		case START:
			break;
		}
	}

	private void performEventAction(ArenaPlayer player) {
		Event event = EventController.getEvent(mp.getName());
		EventExecutor executor = EventController.getEventExecutor(event);
		String args[] = {options1,options2};
		switch (command){
		case JOIN:
			executor.eventJoin(player,args);
			break;
		case LEAVE:
			executor.leave(player);
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
