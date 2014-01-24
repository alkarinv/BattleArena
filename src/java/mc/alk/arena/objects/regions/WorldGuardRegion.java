package mc.alk.arena.objects.regions;

import java.util.Map;

import mc.alk.arena.controllers.WorldGuardController;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldGuardRegion implements ArenaRegion{
	protected String regionName;

	protected String regionWorld;

	World world;

	public WorldGuardRegion(){}

	public WorldGuardRegion(String regionWorld, String regionName){
		this.regionWorld = regionWorld;
		this.regionName = regionName;
	}

	@Override
	public Object yamlToObject(Map<String,Object> map, String value) {
		if (value == null)
			return null;
		String split[] = value.split(",");
		regionWorld = split[0];
		regionName = split[1];
		return new WorldGuardRegion(regionWorld, regionName);
	}

	@Override
	public Object objectToYaml() {
		return regionWorld+","+regionName;
	}

	@Override
	public boolean valid() {
		return regionName != null && regionWorld != null &&
				WorldGuardController.hasWorldGuard() &&
				WorldGuardController.hasRegion(regionWorld, regionName);
	}

	public String getRegionID() {
		return regionName;
	}

	public void setRegionID(String regionName) {
		this.regionName = regionName;
	}

	public String getRegionWorld() {
		return regionWorld;
	}

	public void setRegionWorld(String regionWorld) {
		this.regionWorld = regionWorld;
	}

	public World getWorld(){
		if (world == null){
			world = Bukkit.getWorld(regionWorld);}
		return world;
	}
}
