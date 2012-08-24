package mc.alk.arena.objects.teams;

import mc.alk.arena.objects.ArenaPlayer;

public interface TeamHandler {
//	/*
//	 * Player has left Minecraft, kicked, quit,..
//	 */
//	public boolean onPlayerExit(Player p,Team team);

	public boolean canLeave(ArenaPlayer p);
	public boolean leave(ArenaPlayer p);
}
