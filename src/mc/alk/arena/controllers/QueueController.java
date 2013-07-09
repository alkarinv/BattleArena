package mc.alk.arena.controllers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Permissions;
import mc.alk.arena.controllers.messaging.MessageHandler;
import mc.alk.arena.events.BAEvent;
import mc.alk.arena.events.players.ArenaPlayerEnterQueueEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveEvent;
import mc.alk.arena.events.players.ArenaPlayerLeaveQueueEvent;
import mc.alk.arena.listeners.competition.InArenaListener;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.pairs.JoinResult;
import mc.alk.arena.objects.pairs.ParamTeamPair;
import mc.alk.arena.objects.queues.ArenaMatchQueue;
import mc.alk.arena.objects.queues.TeamJoinObject;
import mc.alk.arena.objects.teams.ArenaTeam;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class QueueController extends ArenaMatchQueue implements ArenaListener, Listener{
	final MethodController methodController = new MethodController();

	public QueueController(){
		super();
		Bukkit.getPluginManager().registerEvents(this, BattleArena.getSelf());
		methodController.addAllEvents(this);
	}

	private void callEvent(BAEvent event){
		methodController.callEvent(event);
		event.callEvent();
	}

	private void leftQueue(ArenaPlayer player, final ArenaTeam team, MatchParams params, ParamTeamPair ptp){
		if (InArenaListener.inQueue(player.getName())){
			callEvent(new ArenaPlayerLeaveQueueEvent(player,team, params,ptp));
			methodController.updateEvents(MatchState.ONLEAVE, player);
		}
	}

	@Override
	protected void leaveQueue(ArenaPlayer player, final ArenaTeam team, MatchParams params, ParamTeamPair ptp){
		callEvent(new ArenaPlayerLeaveQueueEvent(player,team, params,ptp));
		methodController.updateEvents(MatchState.ONLEAVE, player);
	}

	@Override
	public synchronized ParamTeamPair removeFromQue(ArenaPlayer player) {
		ParamTeamPair ptp = super.removeFromQue(player);
		if (ptp != null){
			leftQueue(player,ptp.team,ptp.q,ptp);
		}
		return ptp;
	}

	@ArenaEventHandler
	public void onArenaPlayerLeaveEvent(ArenaPlayerLeaveEvent event){
		ArenaPlayer player = event.getPlayer();
		ParamTeamPair ptp = removeFromQue(player);
		if (ptp != null){
			player.reset();
			event.addMessage(MessageHandler.getSystemMessage("you_left_queue",ptp.q.getName()));
		}
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
		if (event.getFrom().getWorld().getUID() != event.getTo().getWorld().getUID() &&
				!event.getPlayer().hasPermission(Permissions.TELEPORT_BYPASS_PERM)){
			ArenaPlayer ap = BattleArena.toArenaPlayer(event.getPlayer());
			ParamTeamPair ptp = removeFromQue(ap);
			if (ptp != null){
				ptp.team.sendMessage("&cYou have been removed from the queue for changing worlds");
				leftQueue(ap, ptp.team, ptp.q, ptp);
			}
		}
	}

	@Override
	public JoinResult join(TeamJoinObject tqo, boolean shouldStart) {
		JoinResult jr = super.join(tqo, shouldStart);
		for (ArenaTeam t: tqo.getTeams()){
			for (ArenaPlayer ap: t.getPlayers()){
				ArenaPlayerEnterQueueEvent event = new ArenaPlayerEnterQueueEvent(ap,t,tqo,jr);
				callEvent(event);
			}
			methodController.updateEvents(MatchState.ONENTER, t.getPlayers());
		}

		return jr;
	}
}
