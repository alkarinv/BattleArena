package test.mc.alk.arena;

import java.io.File;
import java.lang.reflect.Field;

import junit.framework.TestCase;
import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
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
import mc.alk.arena.serializers.BAConfigSerializer;
import mc.alk.arena.serializers.BaseConfig;
import mc.alk.arena.serializers.ConfigSerializer;
import mc.alk.arena.serializers.MessageSerializer;
import mc.alk.mc.MCServer;

import org.bukkit.entity.Player;

import test.mc.alk.arena.objects.TestPlugin;
import test.mc.alk.testbukkit.TestBukkitPlayer;
import test.mc.alk.testbukkit.TestBukkitServer;
import test.mc.alk.testbukkit.TestMCBukkitServer;


public class TestQueue extends TestCase{
	TestPlugin plugin = null;
	BattleArenaController bac;
	private static final BAConfigSerializer baConfigSerializer = new BAConfigSerializer();
	BattleArena ba = new BattleArena();
	ArenaPlayer[] ap = new ArenaPlayer[10];

	@Override
	protected void setUp() throws Exception{
		plugin = new TestPlugin();
		ArenaType.register("arena", Arena.class, plugin);
		BukkitInterface.setServer(new TestBukkitServer());
		plugin.onEnable();

		/// Set test server
		MCServer.setInstance(new TestMCBukkitServer());
		baConfigSerializer.setConfig(new File("test_files/config.yml"));
		baConfigSerializer.loadDefaults();
		MatchParams mp = ParamController.getMatchParamCopy(Defaults.DEFAULT_CONFIG_NAME);
		assertNotNull(mp);
		for (int i=0;i<ap.length;i++){
			ap[i] = createArenaPlayer("p"+i);
		}

		/// load classes
		BAClassesSerializer classesSerializer = new BAClassesSerializer();
		classesSerializer.setConfig(new File("test_files/classes.yml"));
		classesSerializer.loadAll();
		/// Controller
		bac = new BattleArenaController(null);
		Field field = BattleArena.class.getDeclaredField("arenaController");
		field.setAccessible(true);
		field.set(null, bac);

		/// Messages
		MessageSerializer ms = new MessageSerializer("default",null);
		ms.setConfig(new File("default_files/messages.yml"));
		MessageSerializer.setDefaultConfig(ms);
		AnnouncementOptions an = new AnnouncementOptions();
		AnnouncementOptions.setDefaultOptions(an);

		mp = loadParams("Arena");
		assertNotNull(mp);

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
		ParamController.addMatchParams(mp);
		bac.addArena(arena);
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

	public static ArenaPlayer createArenaPlayer(String name){
		Player p1 = new TestBukkitPlayer(name);
		return BattleArena.toArenaPlayer(p1);
	}

	public void testMatchParams(){
		MatchParams op = ParamController.getMatchParamCopy("arena");
		MatchParams p = ParamController.getMatchParamCopy("arena");
		MatchParams ap = ParamController.copyParams(BattleArena.getArena("a2").getParams());
		assertEquals(ap.getParent().getName(), p.getName());
		assertEquals(ap.getParent().getParent().getName(), Defaults.DEFAULT_CONFIG_NAME);
//		System.out.println(""+new ReflectionToStringBuilder(ap.getParent().getParent(), ToStringStyle.MULTI_LINE_STYLE) );
		ap.flatten();
		p = ParamController.getMatchParamCopy("arena");
//		assertTrue(p.same(op));
//		System.out.println(""+new ReflectionToStringBuilder(ap, ToStringStyle.MULTI_LINE_STYLE) );
	}

	public void gtestQueue(){
		String[] args = new String[]{"join", "a1"};
		String[] args2 = new String[]{"join","a2"};
		assertNull(BattleArena.getArena("DoesntExist"));
		assertNotNull(BattleArena.getArena("a1"));
		assertNotNull(BattleArena.getArena("a2"));
		BAExecutor exec = new BAExecutor();

		exec.join(ap[0], loadParams("Arena"), args);
		exec.join(ap[1], loadParams("Arena"), args);

		exec.join(ap[2], loadParams("Arena"), args2);
		exec.join(ap[3], loadParams("Arena"), args2);
		exec.join(ap[4], loadParams("Arena"), args2);
		exec.join(ap[5], loadParams("Arena"), args2);
//		for (MatchParams params : ParamController.getAllParams()){
//			System.out.println("param  =  "+ params);
//		}
		delay(50);
	}

	private void delay(long millis) {
		try {Thread.sleep(millis);}catch(Exception e){}
	}

	public void testQuit(){
		System.exit(1);
	}
}
