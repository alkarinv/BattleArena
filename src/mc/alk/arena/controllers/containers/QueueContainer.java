package mc.alk.arena.controllers.containers;

import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.queues.ArenaMatchQueue;

public class QueueContainer extends PlayerContainer{
	ArenaMatchQueue amq;

	@Override
	public LocationType getLocationType() {
		// TODO Auto-generated method stub
		return null;
	}

//	public QueueContainer(ArenaMatchQueue amq){
//		this.amq = amq;
//	}



//	protected void enteredQueue(ArenaPlayer player, ArenaTeam team, QueueResult qpp){
//		if (!InArenaListener.inQueue(player.getName())){
//			callEvent(new ArenaPlayerEnterQueueEvent(player,team,qpp));
//		}
//	}
//
//	protected void leftQueue(ArenaPlayer player, final ArenaTeam team){
//		if (InArenaListener.inQueue(player.getName())){
//			callEvent(new ArenaPlayerLeaveQueueEvent(player,team));
//		}
//	}
//
//	@ArenaEventHandler
//	public void onPlayerChangeWorld(PlayerTeleportEvent event){
//		if (event.isCancelled())
//			return;
//		if (event.getFrom().getWorld().getUID() != event.getTo().getWorld().getUID()){
//			ArenaPlayer ap = BattleArena.toArenaPlayer(event.getPlayer());
//			ParamTeamPair ptp = amq.removeFromQue(ap);
//			if (ptp != null){
////				unhandle(ptp.team);
//				ptp.team.sendMessage("&cYou have been removed from the queue for changing worlds");
//			} else {
//				unhandle(ap,null);
//			}
//		}
//	}

//	@ArenaEventHandler
//	public void onPlayerInteract(PlayerInteractEvent event){
//		if (event.isCancelled() || !Defaults.ENABLE_PLAYER_READY_BLOCK)
//			return;
//		final Block b = event.getClickedBlock();
//		if (b == null)
//			return;
//		/// Check to see if it's a sign
//		final Material m = b.getType();
//		if (m.equals(Defaults.READY_BLOCK)) {
//			final ArenaPlayer ap = BattleArena.toArenaPlayer(event.getPlayer());
//			if (ap.isReady()) /// they are already ready
//				return;
//			QueueResult qtp = amq.getQueuePos(ap);
//			if (qtp == null){
//				return;}
//			final Action action = event.getAction();
//			if (action == Action.LEFT_CLICK_BLOCK){ /// Dont let them break the block
//				event.setCancelled(true);}
//			MessageUtil.sendMessage(ap, "&2You ready yourself for the arena");
//			this.forceStart(qtp.params, true);
//		}
//	}

}
