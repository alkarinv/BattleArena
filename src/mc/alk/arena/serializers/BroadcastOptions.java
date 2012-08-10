package mc.alk.arena.serializers;

import java.util.HashMap;
import java.util.Map;

import mc.alk.arena.objects.MatchState;

import com.dthielke.herochat.Channel;
import com.dthielke.herochat.Herochat;

public class BroadcastOptions {

	public enum BroadcastOption{
		ANNOUNCE, HC, PREFIX;

		public static BroadcastOption fromName(String str){
			str = str.toUpperCase();
			return BroadcastOption.valueOf(str);
		}
	}

	static boolean bcOnPrestart = true;
	static boolean bcOnVictory = true;

	public static Herochat hc = null;	
	static Channel onPrestartChannel = null;
	static Channel onVictoryChannel = null;

	Map<MatchState, Map<BroadcastOption,Object>> options = new HashMap<MatchState, Map<BroadcastOption,Object>>();

	//	boolean anStart = true;
	//	boolean anEnd = true;
	public void setMatchStarting(boolean b) {
		//		anStart = b;
		bcOnPrestart = true;
	}
	public void setMatchEnding(boolean b) {
		//		anEnd = b;
		bcOnVictory = b;
	}
	public void setMatchStartingChannel(String ch){
		Channel channel = Herochat.getChannelManager().getChannel(ch);
		onPrestartChannel = channel;
		System.out.println("startchannel = " + channel);
	}
	public void setMatchEndingChannel(String ch){
		Channel channel = Herochat.getChannelManager().getChannel(ch);
		onVictoryChannel = channel;
		System.out.println("endchannel = " + channel);
	}
	public static boolean hasHerochat() {
		return hc != null;
	}
	public static void setHerochat(Herochat hc) {
		BroadcastOptions.hc = hc;
	}
	public boolean broadcastOnPrestart() {
		return bcOnPrestart;
	}
	public boolean broadcastOnVictory() {
		return bcOnVictory;
	}
	public static Channel getOnPrestartChannel() {
		return onPrestartChannel;
	}
	public static void setStartingChannel(Channel startingChannel) {
		BroadcastOptions.onPrestartChannel = startingChannel;
	}
	public static Channel getOnVictoryChannel() {
		return onVictoryChannel;
	}
	public static void setonVictoryChannel(Channel endChannel) {
		BroadcastOptions.onVictoryChannel = endChannel;
	}
	public void setBroadcastOption(MatchState ms, BroadcastOption bo, String value) {
		Map<BroadcastOption,Object> ops = options.get(ms);
		if (ops == null){
			ops = new HashMap<BroadcastOption,Object>();
			options.put(ms, ops);
		}
		switch (bo){
		case HC:
			if (hc != null && value != null){
				Channel channel = Herochat.getChannelManager().getChannel(value);
				ops.put(bo, channel);
				switch(ms){
				case ONPRESTART: onPrestartChannel = channel; break;
				case ONVICTORY: onVictoryChannel = channel;break;
				}
			}
			return;
		case ANNOUNCE:
			switch(ms){
			case ONPRESTART: bcOnPrestart = true;break;
			case ONVICTORY: bcOnVictory = true;break;
			}
			break;
		}
		ops.put(bo,value);
	}
}
