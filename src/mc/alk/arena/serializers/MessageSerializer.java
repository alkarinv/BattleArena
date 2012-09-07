package mc.alk.arena.serializers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import mc.alk.arena.controllers.messaging.MessageFormatter;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.messaging.Channel;
import mc.alk.arena.objects.messaging.Message;
import mc.alk.arena.objects.messaging.MessageOptions;
import mc.alk.arena.objects.messaging.MessageOptions.MessageOption;
import mc.alk.arena.objects.teams.Team;

import org.bukkit.configuration.MemorySection;


public class MessageSerializer extends BaseSerializer {
	/// Our default messages
	private static MessageSerializer defaultMessages;

	/// Map of path to options
	private HashMap<String,MessageOptions> msgOptions = new HashMap<String,MessageOptions>();
	
	private static HashMap<String,MessageSerializer> files = new HashMap<String,MessageSerializer>();
	
	public MessageSerializer(String name){
		MessageSerializer ms = files.get(name);
		if (ms != null){
			this.config = ms.config;
			this.f = ms.f;
			this.msgOptions = ms.msgOptions;
		}
	}
	public static void addMessageSerializer(String name, MessageSerializer ms){
		files.put(name, ms);
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
		Set<String> keys = config.getKeys(true);
		keys.remove("version");
		for (String key: keys){
			Object obj = config.get(key);
			if (obj instanceof MemorySection)
				continue;
			msgOptions.put(key, new MessageOptions((String)obj));
		}
	}


	public Message getMessage(String path) {
		if (config != null && config.contains(path)){
			return new Message(config.getString(path), msgOptions.get(path));
		}
		if (this != defaultMessages){
			return defaultMessages.getMessage(path);		
		} else {
			return null;
		}
	}

	private boolean contains(String path) {
		return config.contains(path);
	}

	public static boolean hasMessage(String prefix, String node) {
		return defaultMessages.contains(prefix+"." + node);
	}    

	public static void loadDefaults() {
		defaultMessages.reloadFile();
	}

	public static void setDefaultConfig(MessageSerializer messageSerializer) {
		MessageSerializer.defaultMessages = messageSerializer;
	}

	public static String getDefaultMessage(String string, String string2) {
		return null;
	}
	
	public static String colorChat(String msg) {return msg.replaceAll("&", Character.toString((char) 167));}
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
	
	protected void sendVictory(Channel serverChannel, Team victor, Collection<Team> losers, MatchParams mp, String path, String serverPath){
		Message message = getMessage(path);
		Message serverMessage = getMessage(serverPath);
		Set<MessageOption> ops = message.getOptions();
		if (serverChannel != Channel.NullChannel){
			ops.addAll(serverMessage.getOptions());			
		}

		String msg = message.getMessage();
		MessageFormatter msgf = new MessageFormatter(this, mp, ops.size(), losers.size()+1, message, ops);
		List<Team> teams = new ArrayList<Team>(losers);
		teams.add(victor);
		msgf.formatCommonOptions(teams, mp.getSecondsToLoot());
		for (Team t: losers){
			msgf.formatTeamOptions(t,false);
			msgf.formatTwoTeamsOptions(t, teams);
			msgf.formatTeams(teams);
			msgf.formatWinnerOptions(t, false);			
			msgf.formatWinnerOptions(victor, true);
			String newmsg = msgf.getFormattedMessage(message);
			t.sendMessage(newmsg);
		}
		msgf.formatTeamOptions(victor,true);
		msgf.formatTwoTeamsOptions(victor, teams);
		msgf.formatTeams(teams);
		if (!losers.isEmpty()){
			msgf.formatWinnerOptions(losers.iterator().next(), false);			
		}
		msgf.formatWinnerOptions(victor, true);
		String newmsg = msgf.getFormattedMessage(message);
		victor.sendMessage(newmsg);

		if (serverChannel != Channel.NullChannel){
			msg = msgf.getFormattedMessage(serverMessage);
			serverChannel.broadcast(msg);
		}
	}

}
