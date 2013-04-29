package mc.alk.arena.listeners.competition;

import mc.alk.arena.BattleArena;
import mc.alk.arena.competition.match.Match;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchTransitions;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.options.TransitionOption;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PotionSplashEvent;

public class PotionListener implements ArenaListener{
	MatchTransitions transitionOptions;
	Match match;

	public PotionListener(Match match){
		this.transitionOptions = match.getParams().getTransitionOptions();
		this.match = match;
	}

	@MatchEventHandler(needsPlayer=false)
	public void onPotionSplash(PotionSplashEvent event) {
		if (!event.isCancelled())
			return;
		if (event.getEntity().getShooter() instanceof Player){
			Player p = (Player) event.getEntity().getShooter();
			ArenaPlayer ap = BattleArena.toArenaPlayer(p);
			if (match.insideArena(ap) &&
					transitionOptions.hasOptionAt(match.getState(), TransitionOption.POTIONDAMAGEON)){
				event.setCancelled(false);
			}
		}
	}
}
