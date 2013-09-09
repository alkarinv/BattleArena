package mc.alk.arena.controllers.messaging;

import java.util.List;

import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.messaging.Message;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.serializers.MessageSerializer;
import mc.alk.arena.util.Log;

public class MessageHandler extends MessageSerializer {

	public MessageHandler() {
		super(null,null);
	}

	public static String getSystemMessage(List<Object> vars, String string, Object... varArgs) {
		final Message msg = getDefaultMessage("system."+string);
		if (msg != null){
			String stringmsg = msg.getMessage();
			if (vars == null || vars.isEmpty() || !stringmsg.contains("{"))
				return getSystemMessage(string,varArgs);
			for (Object o: vars){
				if (o instanceof MatchParams){
					stringmsg = stringmsg.replaceAll("\\{matchname\\}", ((MatchParams)o).getName());
					stringmsg = stringmsg.replaceAll("\\{cmd\\}", ((MatchParams)o).getCommand());
				} else if (o instanceof ArenaTeam){
					stringmsg = stringmsg.replaceAll("\\{team\\}", ((ArenaTeam)o).getName());
				}
			}
			try{
				return String.format(string,varArgs);
			} catch (Exception e){
				final String err = "&c[BA Message Error] system.+"+string;
				Log.err(err);
				for (Object o: varArgs){
					Log.err("Message Option: " + o);}
				Log.printStackTrace(e);
				return err;
			}
		}
		return null;
	}

	public static String getSystemMessage(String string, Object... varArgs) {
		final Message msg = getDefaultMessage("system."+string);
		if (msg != null){
			try{
				final String message = msg.getMessage();
				return message != null ? String.format(message,varArgs) : null;
			} catch (Exception e){
				final String err = "&c[BA Message Error] system.+"+string;
				Log.err(err);
				for (Object o: varArgs){
					Log.err("Message Option: " + o);}
				Log.printStackTrace(e);
				return err;
			}
		}
		return null;
	}
}
