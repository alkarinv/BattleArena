package mc.alk.arena.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener{

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled()) return;
		final Block clickedBlock = event.getClickedBlock();
		if (clickedBlock == null) return; /// This has happenned, minecraft is a strange beast
		final Material clickedMat = clickedBlock.getType();

		/// If this is an uninteresting block get out of here as quickly as we can
		if (!(clickedMat.equals(Material.SIGN) || clickedMat.equals(Material.SIGN_POST) 
				|| 	clickedMat.equals(Material.WALL_SIGN))) {
			return;
		}
		/// TODO complete
	}
}
