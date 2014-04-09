package mc.alk.arena.listeners.competition;

import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.options.TransitionOption;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements ArenaListener{
	PlayerHolder holder;

	public BlockBreakListener(PlayerHolder holder){
		this.holder = holder;
	}

	@ArenaEventHandler(priority=EventPriority.HIGH)
	public void onPlayerBlockBreak(BlockBreakEvent event) {
        if (holder.hasOption(TransitionOption.BLOCKBREAKOFF)) {
            event.setCancelled(true);
        } else if (holder.hasOption(TransitionOption.BLOCKBREAKON)) {
            event.setCancelled(false);
        }
    }
}
