package mc.alk.arena.objects.modules;

import java.io.File;

public class BrokenArenaModule extends ArenaModule{

	String name;
	public BrokenArenaModule(String name){ this.name = name; this.setEnabled(false);}
	@Override
	public String getName() {return name;}
	@Override
	public String getVersion() {return "0.0";}
	@Override
	public void reloadConfig(){}
	@Override
	protected void saveDefaultConfig(){}
	@Override
	protected File getConfigFile(){return null;}

}
