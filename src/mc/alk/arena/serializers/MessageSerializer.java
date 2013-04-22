package mc.alk.arena.serializers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mc.alk.arena.controllers.messaging.MessageFormatter;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.messaging.Channel;
import mc.alk.arena.objects.messaging.Message;
import mc.alk.arena.objects.messaging.MessageOptions;
import mc.alk.arena.objects.messaging.MessageOptions.MessageOption;
import mc.alk.arena.objects.teams.ArenaTeam;

import org.bukkit.configuration.MemorySection;


public class MessageSerializer extends BaseConfig {
	/// Our default messages
	private static MessageSerializer defaultMessages;

	/// Map of path to options
	private HashMap<String,MessageOptions> msgOptions = new HashMap<String,MessageOptions>();

	private static HashMap<String,MessageSerializer> files = new HashMap<String,MessageSerializer>();

	protected MatchParams mp;

	public MessageSerializer(String name, MatchParams params){
		mp = params;
		if (name == null)
			return;
		MessageSerializer ms = files.get(name);
		if (ms != null){
			this.config = ms.config;
			this.file = ms.file;
			this.msgOptions = ms.msgOptions;
		}
	}

	public static void addMessageSerializer(String name, MessageSerializer ms){
		files.put(name, ms);
	}

	public static MessageSerializer getMessageSerializer(String name){
		return files.get(name);
	}

	public void loadAll(){
		initMessageOptions();
	}

	public static void reloadConfig(String params) {
		MessageSerializer ms = files.get(params);
		if (ms != null){
			ms.reloadFile();
			ms.initMessageOptions();
		}
	}

	public void initMessageOptions(){
		if (config == null)
			return;
		msgOptions.clear();
		Set<String> keys = config.getKeys(true);
		keys.remove("version");
		for (String key: keys){
			Object obj = config.get(key);
			if (obj == null || obj instanceof MemorySection)
				continue;
			msgOptions.put(key, new MessageOptions((String)obj));
		}
	}

	public static Message getDefaultMessage(String path) {
		return defaultMessages != null ? defaultMessages.getNodeMessage(path) : null;
	}

	public Message getNodeMessage(String path) {
		if (config != null && config.contains(path)){
			return new Message(config.getString(path), msgOptions.get(path));
		}
		if (this != defaultMessages){
			return defaultMessages.getNodeMessage(path);
		} else {
			return null;
		}
	}

	public String getNodeText(String path) {
		if (config != null && config.contains(path)){
			return config.getString(path);
		}
		if (this != defaultMessages){
			return defaultMessages.getNodeText(path);
		} else {
			return null;
		}
	}

	private boolean contains(String path) {
		return config.contains(path);
	}

	public static boolean hasMessage(String prefix, String node) {
		return defaultMessages != null ? defaultMessages.contains(prefix+"." + node) : false;
	}

	public static void loadDefaults() {
		if (defaultMessages != null) defaultMessages.reloadFile();
	}

	public static void setDefaultConfig(MessageSerializer messageSerializer) {
		MessageSerializer.defaultMessages = messageSerializer;
	}

	public static String getDefaultMessage(String string, String string2) {
		return null;
	}

	public static String colorChat(String msg) {return msg.replace('&', '\167');}
	public static String decolorChat(String msg) { return msg.replaceAll("&", "§").replaceAll("\\§[0-9a-zA-Z]", "");}

	protected static String getStringPathFromSize(int size) {
		if (size == 1)
			return "oneTeam";
		else if (size == 2){
			return "twoTeams";
		} else {
			return "multipleTeams";
		}
	}

	protected void sendVictory(Channel serverChannel, Collection<ArenaTeam> victors, Collection<ArenaTeam> losers, MatchParams mp, String winnerpath,String loserpath, String serverPath){
		int size = victors != null ? victors.size() : 0;
		size += losers != null ? losers.size() : 0;
		Message winnermessage = getNodeMessage(winnerpath);
		Message losermessage = getNodeMessage(loserpath);
		Message serverMessage = getNodeMessage(serverPath);

		Set<MessageOption> ops = winnermessage.getOptions();
		if (ops == null)
			ops =new HashSet<MessageOption>();
		ops.addAll(losermessage.getOptions());
		if (serverChannel != Channel.NullChannel && serverMessage != null){
			ops.addAll(serverMessage.getOptions());
		}

		String msg = losermessage.getMessage();
		MessageFormatter msgf = new MessageFormatter(this, mp, ops.size(), size, losermessage, ops);
		List<ArenaTeam> teams = new ArrayList<ArenaTeam>(losers);
		if (victors != null){
			teams.addAll(victors);
		}

		msgf.formatCommonOptions(teams, mp.getSecondsToLoot());
		ArenaTeam vic = (victors != null && !victors.isEmpty()) ? victors.iterator().next() : null;
		for (ArenaTeam t: losers){
			msgf.formatTeamOptions(t,false);
			msgf.formatTwoTeamsOptions(t, teams);
			msgf.formatTeams(teams);
			msgf.formatWinnerOptions(t, false);
			/// TODO : I now need to make this work with multiple winners
			if (vic != null)
				msgf.formatWinnerOptions(vic, true);
			String newmsg = msgf.getFormattedMessage(losermessage);
			t.sendMessage(newmsg);
		}

		if (victors != null){
			for (ArenaTeam victor: victors){
				msgf = new MessageFormatter(this, mp, ops.size(), size, winnermessage, ops);
				msgf.formatCommonOptions(teams, mp.getSecondsToLoot());
				msgf.formatTeamOptions(victor,true);
				msgf.formatTwoTeamsOptions(victor, teams);
				msgf.formatTeams(teams);
				if (!losers.isEmpty()){
					msgf.formatWinnerOptions(losers.iterator().next(), false);
				}
				msgf.formatWinnerOptions(victor, true);
				String newmsg = msgf.getFormattedMessage(winnermessage);
				victor.sendMessage(newmsg);
			}
		}

		if (serverChannel != Channel.NullChannel && serverMessage != null){
			msg = msgf.getFormattedMessage(serverMessage);
			serverChannel.broadcast(msg);
		}
	}

	public void sendAddedToTeam(ArenaTeam team, ArenaPlayer player) {
		Message message = getNodeMessage("common.added_to_team");
		Set<MessageOption> ops = message.getOptions();
		MessageFormatter msgf = new MessageFormatter(this, mp, ops.size(), 1, message, ops);
		msgf.formatTeamOptions(team, false);
		msgf.formatPlayerOptions(player);
		team.sendToOtherMembers(player,msgf.getFormattedMessage(message));
	}

	public void sendTeamJoinedEvent(Channel serverChannel, ArenaTeam team) {
		Message message = getNodeMessage("common.onjoin");
		Message serverMessage = getNodeMessage("common.onjoin_server");
		Set<MessageOption> ops = message.getOptions();
		if (serverChannel != Channel.NullChannel){
			ops.addAll(serverMessage.getOptions());
		}

		String msg = message.getMessage();
		List<ArenaTeam> teams = new ArrayList<ArenaTeam>();
		teams.add(team);
		MessageFormatter msgf = new MessageFormatter(this, mp, ops.size(), teams.size(), message, ops);
		msgf.formatCommonOptions(teams);
		for (ArenaTeam t: teams){
			msgf.formatTeamOptions(t,false);
			msgf.formatTeams(teams);
			String newmsg = msgf.getFormattedMessage(message);
			t.sendMessage(newmsg);
		}

		if (serverChannel != Channel.NullChannel){
			msg = msgf.getFormattedMessage(serverMessage);
			serverChannel.broadcast(msg);
		}
	}

}
