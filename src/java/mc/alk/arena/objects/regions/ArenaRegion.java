package mc.alk.arena.objects.regions;

import mc.alk.arena.objects.YamlSerializable;

public interface ArenaRegion extends YamlSerializable{
	public boolean valid();

    public String getID();

    public String getWorldName();

}
