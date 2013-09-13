package mc.alk.arena.controllers;

import java.util.HashSet;
import java.util.List;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
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
import mc.alk.arena.util.CommandUtil;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.PermissionsUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class QueueController extends ArenaMatchQueue implements ArenaListener, Listener{
	final MethodController methodController = new MethodController();
	private static HashSet<String> disabledCommands = new HashSet<String>();

	public QueueController(){
		super();
		methodController.addAllEvents(this);
		try{Bukkit.getPluginManager().registerEvents(this, BattleArena.getSelf());} catch(Exception e){}
	}

	private void callEvent(BAEvent event){
		methodController.callEvent(event);
	}

	private void leftQueue(ArenaPlayer player, final ArenaTeam team, MatchParams params, ParamTeamPair ptp){
		if (InArenaListener.inQueue(player.getName())){
			methodController.updateEvents(MatchState.ONLEAVE, player);
			callEvent(new ArenaPlayerLeaveQueueEvent(player,team, params,ptp));
		}
	}

	@Override
	protected void leaveQueue(ArenaPlayer player, final ArenaTeam team, MatchParams params, ParamTeamPair ptp){
		if (InArenaListener.inQueue(player.getName())){
			methodController.updateEvents(MatchState.ONLEAVE, player);
			callEvent(new ArenaPlayerLeaveQueueEvent(player,team, params,ptp));
		}
	}

	@Override
	public synchronized ParamTeamPair removeFromQue(ArenaPlayer player) {
		ParamTeamPair ptp = super.removeFromQue(player);
		if (ptp != null){
			leftQueue(player,ptp.team,ptp.params,ptp);
		}
		return ptp;
	}

	@ArenaEventHandler
	public void onArenaPlayerLeaveEvent(ArenaPlayerLeaveEvent event){
		ArenaPlayer player = event.getPlayer();
		ParamTeamPair ptp = removeFromQue(player);
		if (ptp != null){
			player.reset();
			event.addMessage(MessageHandler.getSystemMessage("you_left_queue",ptp.params.getName()));
		}
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
			}
		}
	}

	@Override
	public JoinResult join(TeamJoinObject tqo, boolean shouldStart) {
		JoinResult jr = super.join(tqo, shouldStart);
		switch(jr.status){
		case ADDED_TO_ARENA_QUEUE:
		case ADDED_TO_QUEUE:
			for (ArenaTeam t: tqo.getTeams()){
				for (ArenaPlayer ap: t.getPlayers()){
					ArenaPlayerEnterQueueEvent event = new ArenaPlayerEnterQueueEvent(ap,t,tqo,jr);
					callEvent(event);
				}
				methodController.updateEvents(MatchState.ONENTER, t.getPlayers());
			}
			break;
		case NONE:
			break;
		case ERROR:
		case ADDED_TO_EXISTING_MATCH:
		case STARTED_NEW_GAME:
			return jr;
		default:
			break;
		}
		return jr;
	}

	public static void setDisabledCommands(List<String> commands) {
		for (String s: commands){
			disabledCommands.add("/" + s.toLowerCase());}
	}

	@ArenaEventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
		if (!event.isCancelled() && CommandUtil.shouldCancel(event, disabledCommands)){
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED+"You cannot use that command when you are in the queue");
			if (PermissionsUtil.isAdmin(event.getPlayer())){
				MessageUtil.sendMessage(event.getPlayer(),"&cYou can set &6/bad allowAdminCommands true: &c to change");}
		}
	}

	@ArenaEventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if (event.isCancelled() || !Defaults.ENABLE_PLAYER_READY_BLOCK)
			return;
		if (event.getClickedBlock().getType().equals(Defaults.READY_BLOCK)) {
			final ArenaPlayer ap = BattleArena.toArenaPlayer(event.getPlayer());
			if (ap.isReady()) /// they are already ready
				return;
			JoinResult qtp = getQueuePos(ap);
			if (qtp == null){
				return;}
			final Action action = event.getAction();
			if (action == Action.LEFT_CLICK_BLOCK){ /// Dont let them break the block
				event.setCancelled(true);}
			MessageUtil.sendMessage(ap, "&2You ready yourself for the arena");
			this.forceStart(qtp.params, true);
		}
	}
}
