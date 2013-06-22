package mc.alk.arena.objects.messaging.plugins;

import mc.alk.arena.objects.messaging.Channel;
import mc.alk.arena.objects.messaging.ChatPlugin;

import com.dthielke.herochat.Herochat;

public class HerochatPlugin implements ChatPlugin{

	@Override
	public Channel getChannel(String value) {
		com.dthielke.herochat.Channel channel = Herochat.getChannelManager().getChannel(value);
		return new HerochatChannel(channel);
	}

}
