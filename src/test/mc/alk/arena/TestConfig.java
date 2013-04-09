package test.mc.alk.arena;

import java.io.File;

import junit.framework.TestCase;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.BukkitInterface;
import mc.alk.arena.controllers.CompetitionController;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.RegisteredCompetition;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.exceptions.ConfigException;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.serializers.BaseConfig;
import mc.alk.arena.serializers.ConfigSerializer;
import test.mc.alk.arena.objects.TestPlugin;
import test.mc.alk.testbukkit.TestBukkitServer;

public class TestConfig extends TestCase{
	TestPlugin plugin = null;
	BattleArenaController bac;

	@Override
	protected void setUp(){
		plugin = new TestPlugin();
		ArenaType.register("arena", Arena.class, plugin);
		BukkitInterface.setServer(new TestBukkitServer());
		plugin.onEnable();
	}

	public MatchParams loadParams(String node){
		BaseConfig bc = new BaseConfig( new File("test_files/competitions/"+node+"Config.yml"));
//		ConfigurationSection cs = bc.getConfigurationSection(node);
		MatchParams mp = null;
		try {
			ConfigSerializer config = new ConfigSerializer(plugin, bc.getFile(),node);
			mp = config.loadMatchParams();
//			mp = ConfigSerializer.setTypeConfig(plugin, "arena", cs);
		} catch (ConfigException e) {
			e.printStackTrace();
			fail();
		} catch (InvalidOptionException e) {
			e.printStackTrace();
			fail();
		}
		return mp;
	}

	public void testChange(){
		String comp = "Arena";
		MatchParams mp = loadParams(comp);
		assertNotNull(mp);
		ConfigSerializer cs = new ConfigSerializer(plugin,
				new File("test_files/competitions/"+comp+"Config.yml"),comp);
		RegisteredCompetition rc = new RegisteredCompetition(plugin, comp);
		CompetitionController.addRegisteredCompetition(rc);
//		rc.setSerializer(cs);
	}
}
