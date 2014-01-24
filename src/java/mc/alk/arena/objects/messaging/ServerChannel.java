package mc.alk.arena.objects.messaging;

import mc.alk.arena.Defaults;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;

public class ServerChannel implements Channel {
	ServerChannel(){}

	@Override
	public void broadcast(String msg) {
		if (msg == null || msg.isEmpty())
			return;
		try {
            MessageUtil.broadcastMessage(MessageUtil.colorChat(msg));
		} catch (Throwable e){
			/// getting this a lot of concurrency and null pointer errors from bukkit when stress testing...
			/// so ignore errors from bukkit
			if (!Defaults.DEBUG_STRESS){
				Log.printStackTrace(e);}
		}
	}
}
