package test.mc.alk.arena;

import java.io.File;

import junit.framework.TestCase;
import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.controllers.BukkitInterface;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.executors.BAExecutor;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.exceptions.ConfigException;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.messaging.AnnouncementOptions;
import mc.alk.arena.serializers.ArenaSerializer;
import mc.alk.arena.serializers.BAClassesSerializer;
import mc.alk.arena.serializers.BaseConfig;
import mc.alk.arena.serializers.ConfigSerializer;
import mc.alk.arena.serializers.MessageSerializer;
import mc.alk.mc.MCServer;

import org.bukkit.entity.Player;

import test.mc.alk.arena.objects.TestPlugin;
import test.mc.alk.testbukkit.TestBukkitPlayer;
import test.mc.alk.testbukkit.TestBukkitServer;
import test.mc.alk.testbukkit.TestMCBukkitServer;


public class TestBattleArena extends TestCase{
	TestPlugin plugin = null;
	BattleArenaController bac;

	@Override
	protected void setUp(){
		plugin = new TestPlugin();
		ArenaType.register("arena", Arena.class, plugin);
		BukkitInterface.setServer(new TestBukkitServer());
		plugin.onEnable();

		/// Set test server
		MCServer.setInstance(new TestMCBukkitServer());

		/// load classes
		BAClassesSerializer classesSerializer = new BAClassesSerializer();
		classesSerializer.setConfig(new File("test_files/classes.yml"));
		classesSerializer.loadAll();
		/// Controller
		bac = new BattleArenaController();

		/// Messages
		MessageSerializer ms = new MessageSerializer("default");
		ms.setConfig(new File("default_files/messages.yml"));
		MessageSerializer.setDefaultConfig(ms);
		AnnouncementOptions an = new AnnouncementOptions();
		AnnouncementOptions.setDefaultOptions(an);

		/// Arenas
		ArenaSerializer as = new ArenaSerializer(plugin,new File("test_files/arenas.yml"));
		ArenaSerializer.setBAC(bac);
		as.loadArenas(plugin);
	}

	public void testCreateArena() {
		MatchParams mp = loadParams("Arena");
		assertNotNull(mp);

		Arena arena = ArenaType.createArena("testArena", mp);
		assertNotNull(arena);
		ParamController.addMatchType(mp);
		bac.addArena(arena);
	}

	public MatchParams loadParams(String node){
		BaseConfig bc = new BaseConfig( new File("test_files/competitions/"+node+"Config.yml"));
//		ConfigurationSection cs = bc.getConfigurationSection(node);
		MatchParams mp = null;
		try {
			ConfigSerializer config = new ConfigSerializer(plugin, bc.getFile(),node);
			mp = config.loadType();
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

	public void testQueue(){
		MatchParams mp = loadParams("Arena");
		Player p1 = new TestBukkitPlayer("p1");
		Player p2 = new TestBukkitPlayer("p2");
		ArenaPlayer ap1 = BattleArena.toArenaPlayer(p1);
		ArenaPlayer ap2 = BattleArena.toArenaPlayer(p2);
		String[] args = new String[]{""};

		BAExecutor exec = new BAExecutor();
		exec.join(ap1, mp, args);
		exec.join(ap2, mp, args);
		for (MatchParams params : ParamController.getAllParams()){
			System.out.println("param  =  "+ params);
		}
		delay(50);
		assertTrue(bac.isInQue(ap1));
	}

	private void delay(long millis) {
		try {Thread.sleep(millis);}catch(Exception e){}
	}

	public void testQuit(){
		System.exit(1);
	}
}
