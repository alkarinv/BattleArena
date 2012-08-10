package mc.alk.arena.objects.teams;

import org.bukkit.entity.Player;

public interface TeamHandler {
//	/*
//	 * Player has left Minecraft, kicked, quit,..
//	 */
//	public boolean onPlayerExit(Player p,Team team);

	public boolean canLeave(Player p);
	public boolean leave(Player p);
}
