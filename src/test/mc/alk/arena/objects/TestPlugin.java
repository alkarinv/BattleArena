package test.mc.alk.arena.objects;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import mc.alk.arena.objects.victoryconditions.HighestKills;
import mc.alk.arena.objects.victoryconditions.LastManStanding;
import mc.alk.arena.objects.victoryconditions.NLives;
import mc.alk.arena.objects.victoryconditions.NoTeamsLeft;
import mc.alk.arena.objects.victoryconditions.OneTeamLeft;
import mc.alk.arena.objects.victoryconditions.TimeLimit;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.util.Log;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;

import com.avaje.ebean.EbeanServer;


public class TestPlugin implements Plugin{

	@Override
	public void onEnable() {
		System.out.println("onEnable");

		/// Register our different Victory Types
		VictoryType.register(HighestKills.class, this);
		VictoryType.register(NLives.class, this);
		VictoryType.register(LastManStanding.class, this);
		VictoryType.register(TimeLimit.class, this);
		VictoryType.register(OneTeamLeft.class, this);
		VictoryType.register(NoTeamsLeft.class, this);
	}

	@Override
	public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		return false;
	}

	@Override
	public File getDataFolder() {
		return null;
	}

	@Override
	public PluginDescriptionFile getDescription() {
		return null;
	}

	@Override
	public FileConfiguration getConfig() {
		return null;
	}

	@Override
	public InputStream getResource(String filename) {
		return null;
	}

	@Override
	public void saveConfig() {

	}

	@Override
	public void saveDefaultConfig() {

	}

	@Override
	public void saveResource(String resourcePath, boolean replace) {

	}

	@Override
	public void reloadConfig() {
	}

	@Override
	public PluginLoader getPluginLoader() {
		return null;
	}

	@Override
	public Server getServer() {
		return null;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void onDisable() {
		Log.info("onDisable");
	}

	@Override
	public void onLoad() {
		Log.info("onLoad");
	}


	@Override
	public boolean isNaggable() {
		return false;
	}

	@Override
	public void setNaggable(boolean canNag) {

	}

	@Override
	public EbeanServer getDatabase() {
		return null;
	}

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return null;
	}

	@Override
	public Logger getLogger() {
		return null;
	}

	@Override
	public String getName() {
		return "TestBattleArenaPlugin";
	}

}
