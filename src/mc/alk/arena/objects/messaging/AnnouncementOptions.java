package mc.alk.arena.objects.messaging;

import java.util.HashMap;
import java.util.Map;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.MatchState;

import org.bukkit.craftbukkit.libs.jline.internal.Log;

import com.dthielke.herochat.Herochat;

public class AnnouncementOptions {

	public enum AnnouncementOption{
		ANNOUNCE, DONTANNOUNCE, SERVER, HEROCHAT;

		public static AnnouncementOption fromName(String str){
			str = str.toUpperCase();
			AnnouncementOption ao = null;
			try{
				ao = AnnouncementOption.valueOf(str);
			} catch (Exception e){}
			if (ao != null)
				return ao;
			if (str.toLowerCase().contains("hc"))
				return AnnouncementOption.HEROCHAT;
			return null;
		}
	}
	
	static AnnouncementOptions defaultOptions;
	public static Herochat hc = null;	
	Map<MatchState, Map<AnnouncementOption,Object>> options = new HashMap<MatchState, Map<AnnouncementOption,Object>>();

	public static void setHerochat(Herochat hc) {
		AnnouncementOptions.hc = hc;
	}
	public void setBroadcastOption(MatchState ms, AnnouncementOption bo, String value) {
		Map<AnnouncementOption,Object> ops = options.get(ms);
		if (ops == null){
			ops = new HashMap<AnnouncementOption,Object>();
			options.put(ms, ops);
		}
		if (bo == AnnouncementOption.HEROCHAT){
			if (hc == null){
				Log.error(BattleArena.getPName()+"config.yml Announcement option herochat="+value+
						", will be ignored as HeroChat plugin is not enabled. Defaulting to Server Announcement");
				ops.put(AnnouncementOption.SERVER, null);
				return;
			}
			
			com.dthielke.herochat.Channel channel = Herochat.getChannelManager().getChannel(value);
			if (channel == null){
				Log.error(BattleArena.getPName()+"config.yml Announcement option herochat="+value+
						", will be ignored as HeroChat channel " + value +" can not be found. Defaulting to Server Announcement");
				ops.put(AnnouncementOption.SERVER, null);
				return;
			}
		}
		ops.put(bo, value);
	}

	public static void setDefaultOptions(AnnouncementOptions bo) {
		defaultOptions = bo;
	}
	
	public Channel getChannel(MatchState state) {
		Map<AnnouncementOption,Object> obj = options.get(state);
		/// Dont announce
		if (obj == null || obj.containsKey(AnnouncementOption.DONTANNOUNCE))
			return Channel.NullChannel;

		/// Herochat option enabled
		if (obj.containsKey(AnnouncementOption.HEROCHAT)){
			String hcChannelName = (String) obj.get(AnnouncementOption.HEROCHAT);
			if (hc == null){
				Log.warn(BattleArena.getPName()+"Herochat is not enabled, ignoring config.yml announcement option herochat="+hcChannelName);
				return Channel.ServerChannel;
			}
			com.dthielke.herochat.Channel channel = Herochat.getChannelManager().getChannel(hcChannelName);
			if (channel == null){
				Log.warn(BattleArena.getPName()+"Herochat channel not found!. ignoring config.yml announcement option herochat="+hcChannelName);
				return Channel.ServerChannel;
			} else {
				return new HerochatChannel(channel);	
			}			
		}
		return Channel.ServerChannel;
	}
	
	public static Channel getDefaultChannel(MatchState state) {
		return defaultOptions.getChannel(state);
	}
	public boolean hasOption(MatchState state) {
//		System.out.println("## hasOption = " + options.containsKey(state) +"  " + state);
		return options.containsKey(state);
	}
}
