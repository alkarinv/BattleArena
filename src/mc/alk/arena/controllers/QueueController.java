package mc.alk.arena.controllers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveQueueEvent;
import mc.alk.arena.listeners.competition.InArenaListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.pairs.JoinResult;
import mc.alk.arena.objects.pairs.ParamTeamPair;
import mc.alk.arena.objects.queues.ArenaMatchQueue;
import mc.alk.arena.objects.queues.TeamJoinObject;
import mc.alk.arena.objects.teams.ArenaTeam;

import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class QueueController extends ArenaMatchQueue {
	final MethodController methodController = new MethodController();

	private void callEvent(BAEvent event){
		methodController.callEvent(event);
		event.callEvent();
	}

	private void leftQueue(ArenaPlayer player, final ArenaTeam team, MatchParams params){
		if (InArenaListener.inQueue(player.getName())){
			callEvent(new ArenaPlayerLeaveQueueEvent(player,team, params));
		}
	}

	@Override
	public synchronized ParamTeamPair removeFromQue(ArenaPlayer player) {
		ParamTeamPair ptp = super.removeFromQue(player);
		if (ptp != null){
			leftQueue(player,ptp.team,ptp.q);
		}
		return ptp;
	}

	@ArenaEventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		ArenaPlayer ap = BattleArena.toArenaPlayer(event.getPlayer());
		ParamTeamPair ptp = removeFromQue(ap);
		if (ptp != null){
			ap.reset();}
	}

	@ArenaEventHandler
	public void onPlayerChangeWorld(PlayerTeleportEvent event){
		if (event.isCancelled())
			return;
		if (event.getFrom().getWorld().getUID() != event.getTo().getWorld().getUID()){
			ArenaPlayer ap = BattleArena.toArenaPlayer(event.getPlayer());
			ParamTeamPair ptp = removeFromQue(ap);
			if (ptp != null){
//				unhandle(ptp.team,ptp.q);
				ptp.team.sendMessage("&cYou have been removed from the queue for changing worlds");
			} else {
//				unhandle(ap,null,null);
			}
		}
	}


	@Override
	public JoinResult join(TeamJoinObject tqo, boolean shouldStart) {
		return super.join(tqo, shouldStart);
	}
}
