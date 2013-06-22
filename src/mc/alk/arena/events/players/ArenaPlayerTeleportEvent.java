package mc.alk.arena.events.players;

import mc.alk.arena.objects.ArenaLocation;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.LocationType;
import mc.alk.arena.objects.TeleportDirection;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.teams.ArenaTeam;

public class ArenaPlayerTeleportEvent extends ArenaPlayerEvent{
	final ArenaTeam team;
	final ArenaLocation src;
	final ArenaLocation dest;
	final TeleportDirection direction;
	final ArenaType arenaType;

	public ArenaPlayerTeleportEvent(ArenaType at, ArenaPlayer arenaPlayer, ArenaTeam team,
			ArenaLocation src, ArenaLocation dest, TeleportDirection direction) {
		super(arenaPlayer);
		this.arenaType = at;
		this.team = team;
		this.src = src;
		this.dest = dest;
		this.direction = direction;
	}

	public ArenaType getArenaType(){
		return arenaType;
	}

	public ArenaTeam getTeam() {
		return team;
	}

	public TeleportDirection getDirection(){
		return direction;
	}

	public LocationType getSrcType(){
		return src.getType();
	}

	public LocationType getDestType(){
		return dest.getType();
	}

	public ArenaLocation getSrcLocation() {
		return src;
	}

	public ArenaLocation getDestLocation() {
		return dest;
	}
}
