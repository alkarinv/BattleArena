package mc.alk.arena.objects.messaging;

import mc.alk.arena.util.MessageUtil;

import org.bukkit.Bukkit;

public class ServerChannel implements Channel {
	ServerChannel(){}

	@Override
	public void broadcast(String msg) {
		if (msg == null || msg.trim().isEmpty())
			return;
		Bukkit.getServer().broadcastMessage(MessageUtil.colorChat(msg));
	}
}
