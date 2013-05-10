package mc.alk.arena.objects;

import mc.alk.arena.util.SerializerUtil;

import org.bukkit.Location;

public class ArenaLocation {
	Location location;
	LocationType type;

	public ArenaLocation(Location location, LocationType type){
		this.location = location;
		this.type = type;
	}
	public LocationType getType() {
		return this.type;
	}
	public Location getLocation(){
		return this.location;
	}
	@Override
	public String toString(){
		return "[LocationType loc="+SerializerUtil.getLocString(location) +" type="+type+"]";
	}
}
