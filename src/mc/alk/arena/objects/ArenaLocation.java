package mc.alk.arena.objects;

import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.util.SerializerUtil;

import org.bukkit.Location;

public class ArenaLocation {
	final PlayerHolder ph;
	final Location location;
	final LocationType type;

	public ArenaLocation(PlayerHolder ph, Location location, LocationType type){
		this.ph = ph;
		this.location = location;
		this.type = type;
	}
	public LocationType getType() {
		return this.type;
	}
	public Location getLocation(){
		return this.location;
	}
	public PlayerHolder getPlayerHolder(){
		return ph;
	}
	@Override
	public String toString(){
		return "[LocationType loc="+SerializerUtil.getLocString(location) +" type="+type+"]";
	}
}
