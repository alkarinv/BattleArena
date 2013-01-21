package mc.alk.arena.objects.regions;

import mc.alk.arena.controllers.PylamoController;

import org.bukkit.configuration.ConfigurationSection;

public class PylamoRegion implements ArenaRegion{
	String regionName;

	public PylamoRegion(){}

	public PylamoRegion(String regionName){
		this.regionName = regionName;
	}

	@Override
	public Object yamlToObject(ConfigurationSection cs, String value) {
		regionName = value;
		return new PylamoRegion(regionName);
	}

	@Override
	public Object objectToYaml() {
		return regionName;
	}

	public void setID(String id) {
		regionName = id;
	}

	public String getID() {
		return regionName;
	}
	public boolean valid(){
		return regionName != null && PylamoController.enabled();
	}
}
