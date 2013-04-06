package mc.alk.arena.objects;

import mc.alk.arena.serializers.ArenaSerializer;
import mc.alk.arena.serializers.ConfigSerializer;
import mc.alk.arena.serializers.MessageSerializer;

import org.bukkit.plugin.Plugin;

public class RegisteredCompetition {
	final Plugin plugin;
	final String competitionName;
	ConfigSerializer configSerializer;
	ArenaSerializer arenaSerializer;

	public RegisteredCompetition(Plugin plugin, String competitionName){
		this.plugin = plugin;
		this.competitionName = competitionName;
	}

	public ConfigSerializer getConfigSerializer() {
		return configSerializer;
	}

	public void setConfigSerializer(ConfigSerializer serializer) {
		this.configSerializer = serializer;
	}

	public String getCompetitionName() {
		return competitionName;
	}

	public ArenaSerializer getArenaSerializer() {
		return arenaSerializer;
	}

	public void setArenaSerializer(ArenaSerializer arenaSerializer) {
		this.arenaSerializer = arenaSerializer;
	}

	public void reloadConfigType() {
		configSerializer.reloadFile();
		MessageSerializer.reloadConfig(competitionName);
		try {
			configSerializer.loadType();
		} catch (Exception e) {
			e.printStackTrace();
		}
		plugin.reloadConfig();
	}
	public Plugin getPlugin(){
		return plugin;
	}
}
