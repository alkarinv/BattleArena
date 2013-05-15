package mc.alk.arena.objects;

import mc.alk.arena.BattleArena;
import mc.alk.arena.controllers.BattleArenaController;
import mc.alk.arena.executors.CustomCommandExecutor;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.serializers.ArenaSerializer;
import mc.alk.arena.serializers.ConfigSerializer;
import mc.alk.arena.serializers.MessageSerializer;
import mc.alk.arena.util.Log;

import org.bukkit.plugin.Plugin;

public class RegisteredCompetition {
	final Plugin plugin;
	final String competitionName;
	ConfigSerializer configSerializer;
	ArenaSerializer arenaSerializer;
	CustomCommandExecutor customExecutor;

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

	public void reload(){
		reloadConfigType();
		reloadExecutors();
		reloadArenas();
		reloadMessages();
	}

	private void reloadMessages(){
		/// Reload messages
		MessageSerializer.reloadConfig(competitionName);
	}

	public MessageSerializer getMessageSerializer(){
		return MessageSerializer.getMessageSerializer(competitionName);
	}

	private void reloadExecutors(){
		/* TODO allow them to switch from duel, to JoinPhase, Queue without a restart */
	}

	private void reloadArenas(){
		BattleArenaController ac = BattleArena.getBAController();
		for (ArenaType type : ArenaType.getTypes(plugin)){
			ac.removeAllArenas(type);
		}
		for (ArenaType type : ArenaType.getTypes(plugin)){
			ArenaSerializer.loadAllArenas(plugin,type);
		}
	}

	private void reloadConfigType() {
		configSerializer.reloadFile();
		try {
			/// The config serializer will also deal with MatchParams registration and aliases
			configSerializer.loadMatchParams();
		} catch (Exception e) {
			Log.printStackTrace(e);
		}
		if (plugin != BattleArena.getSelf())
			plugin.reloadConfig();
	}

	public void saveParams(MatchParams params){
		configSerializer.save(params);
	}

	public Plugin getPlugin(){
		return plugin;
	}

	public CustomCommandExecutor getCustomExecutor() {
		return customExecutor;
	}
	public void setCustomExeuctor(CustomCommandExecutor customExecutor){
		this.customExecutor = customExecutor;
	}
}
