package mc.alk.arena.listeners.competition;

import mc.alk.arena.BattleArena;
import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.options.TransitionOption;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PotionSplashEvent;

public class PotionListener implements ArenaListener{
	MatchTransitions transitionOptions;
	PlayerHolder holder;

	public PotionListener(PlayerHolder match){
		this.transitionOptions = match.getParams().getTransitionOptions();
		this.holder = match;
	}

	@ArenaEventHandler(needsPlayer=false)
	public void onPotionSplash(PotionSplashEvent event) {
		if (!event.isCancelled())
			return;
		if (event.getEntity().getShooter() instanceof Player){
			Player p = (Player) event.getEntity().getShooter();
			ArenaPlayer ap = BattleArena.toArenaPlayer(p);
			if (holder.isHandled(ap) &&
					transitionOptions.hasOptionAt(holder.getState(), TransitionOption.POTIONDAMAGEON)){
				event.setCancelled(false);
			}
		}
	}
}
