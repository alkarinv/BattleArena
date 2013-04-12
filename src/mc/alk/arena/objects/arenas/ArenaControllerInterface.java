package mc.alk.arena.objects.arenas;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchResult;
import mc.alk.arena.objects.teams.ArenaTeam;

/**
 *
 * @author alkarin
 *
 * helper class to access private members of arena
 * I sorely want the ability to have a friend class like in C++
 */
public class ArenaControllerInterface {
	final Arena arena;
	public ArenaControllerInterface(Arena arena){
		this.arena = arena;
	}

	public void onOpen(){arena.privateOnOpen();}
	public void onBegin(){arena.privateOnBegin();}
	public void onPrestart(){arena.privateOnPrestart();}
	public void onStart(){arena.privateOnStart();}
	public void onVictory(MatchResult result) {arena.privateOnVictory(result);}
	public void onComplete(){arena.privateOnComplete();}
	public void onFinish(){arena.privateOnFinish();}
	public void onCancel(){arena.privateOnCancel();}
	public void onEnter(ArenaPlayer p, ArenaTeam t) {arena.privateOnEnter(p,t);}
	public void onEnterWaitRoom(ArenaPlayer p, ArenaTeam t) {arena.privateOnEnterWaitRoom(p,t);}
	public void onLeave(ArenaPlayer p, ArenaTeam t) {arena.privateOnLeave(p,t);}
	public void onJoin(ArenaPlayer p, ArenaTeam t) {arena.privateOnJoin(p,t);}
	public void create() {arena.privateCreate();}
	public void init() {arena.privateInit();}
	public void delete() {arena.privateDelete();}
}
