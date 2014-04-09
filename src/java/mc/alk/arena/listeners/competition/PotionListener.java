package mc.alk.arena.listeners.competition;

import mc.alk.arena.BattleArena;
import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.options.TransitionOption;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PotionSplashEvent;

public class PotionListener implements ArenaListener{
	PlayerHolder holder;

	public PotionListener(PlayerHolder holder){
		this.holder = holder;
	}

	@ArenaEventHandler(needsPlayer=false)
	public void onPotionSplash(PotionSplashEvent event) {
		if (!event.isCancelled())
			return;
		if (event.getEntity().getShooter() instanceof Player){
			Player p = (Player) event.getEntity().getShooter();
			ArenaPlayer ap = BattleArena.toArenaPlayer(p);
			if (holder.isHandled(ap) && holder.hasOption(TransitionOption.POTIONDAMAGEON)){
				event.setCancelled(false);
			}
		}
	}
}
