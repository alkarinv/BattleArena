package mc.alk.arena.objects.messaging;

import java.util.EnumMap;
import java.util.Map;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.MatchState;
import mc.alk.arena.util.Log;
import net.milkbowl.vault.chat.Chat;

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
	public static Chat chat = null;

	Map<MatchState, Map<AnnouncementOption,Object>> matchOptions =
			new EnumMap<MatchState, Map<AnnouncementOption,Object>>(MatchState.class);
	Map<MatchState, Map<AnnouncementOption,Object>> eventOptions =
			new EnumMap<MatchState, Map<AnnouncementOption,Object>>(MatchState.class);

	public static void setHerochat(Herochat hc) {
		AnnouncementOptions.hc = hc;
	}
	public static void setVaultChat(Chat chat) {
		AnnouncementOptions.chat = chat;
	}

	public void setBroadcastOption(boolean match, MatchState ms, AnnouncementOption bo, String value) {
		Map<MatchState, Map<AnnouncementOption,Object>> options = match ? matchOptions : eventOptions;
		Map<AnnouncementOption,Object> ops = options.get(ms);
		if (ops == null){
			ops = new EnumMap<AnnouncementOption,Object>(AnnouncementOption.class);
			options.put(ms, ops);
		}
		if (bo == AnnouncementOption.HEROCHAT){
			if (hc == null){
				Log.err(BattleArena.getPluginName()+"config.yml Announcement option herochat="+value+
						", will be ignored as HeroChat plugin is not enabled. Defaulting to Server Announcement");
				ops.put(AnnouncementOption.SERVER, null);
				return;
			}

			com.dthielke.herochat.Channel channel = Herochat.getChannelManager().getChannel(value);
			if (channel == null){
				Log.err(BattleArena.getPluginName()+"config.yml Announcement option herochat="+value+
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

	public Channel getChannel(boolean match, MatchState state) {
		Map<MatchState, Map<AnnouncementOption,Object>> options = match ? matchOptions : eventOptions;

		Map<AnnouncementOption,Object> obj = options.get(state);
		/// Dont announce
		if (obj == null || obj.containsKey(AnnouncementOption.DONTANNOUNCE))
			return Channel.NullChannel;

		/// Herochat option enabled
		if (obj.containsKey(AnnouncementOption.HEROCHAT)){
			String hcChannelName = (String) obj.get(AnnouncementOption.HEROCHAT);
			if (hc == null){
				Log.warn(BattleArena.getPluginName()+"Herochat is not enabled, ignoring config.yml announcement option herochat="+hcChannelName);
				return Channel.ServerChannel;
			}
			com.dthielke.herochat.Channel channel = Herochat.getChannelManager().getChannel(hcChannelName);
			if (channel == null){
				Log.warn(BattleArena.getPluginName()+"Herochat channel not found!. ignoring config.yml announcement option herochat="+hcChannelName);
				return Channel.ServerChannel;
			} else {
				return new HerochatChannel(channel);
			}
		}
		return Channel.ServerChannel;
	}

	public static Channel getDefaultChannel(boolean match, MatchState state) {
		return defaultOptions.getChannel(match, state);
	}
	public boolean hasOption(boolean match, MatchState state) {
//		System.out.println("## hasOption = " + matchOptions.containsKey(state) +"  " + state);
		Map<MatchState, Map<AnnouncementOption,Object>> options = match ? matchOptions : eventOptions;
		return options.containsKey(state);
	}


}
