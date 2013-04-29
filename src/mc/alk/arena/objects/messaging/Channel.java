package mc.alk.arena.objects.messaging;

import mc.alk.arena.util.MessageUtil;

import org.bukkit.Bukkit;


public interface Channel {
	public void broadcast(String msg);
	public static final Channel NullChannel = new NullChannel();
	public static final Channel ServerChannel = new ServerChannel();

	public class NullChannel implements Channel {
		private NullChannel(){}
		@Override
		public void broadcast(String msg) {
			/** Literally do nothing */
		}
		@Override
		public String toString(){
			return "[NullChannel]";
		}
	}

	public class ServerChannel implements Channel {
		private ServerChannel(){}

		@Override
		public void broadcast(String msg) {
			if (msg == null || msg.trim().isEmpty())
				return;
			Bukkit.getServer().broadcastMessage(MessageUtil.colorChat(msg));
		}
	}

}
