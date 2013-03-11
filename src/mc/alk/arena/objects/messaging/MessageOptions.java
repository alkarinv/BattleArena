package mc.alk.arena.objects.messaging;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class MessageOptions {
	public static enum MessageOption{
		TEAM("{team}"), TEAMSHORT("{teamshort}"), TEAMLONG("{teamlong}"),
		TEAM1("{team1}"), TEAMSHORT1("{teamshort1}"), TEAMLONG1("{teamlong1}"),
		TEAM2("{team2}"), TEAMSHORT2("{teamshort2}"), TEAMLONG2("{teamlong2}"),
		NAME("{name}"), NAME1("{name1}"),NAME2("{name2}"),
		OTHERTEAM("{otherteam}"),
		WINS("{wins}"), LOSSES("{losses}"),
		WINSAGAINST("{winsagainst}"), LOSSESAGAINST("{lossesagainst}"),
		WINNER("{winner}"), WINNERSHORT("{winnershort}"),WINNERLONG("{winnerlong}"),
		LOSER("{loser}"), LOSERSHORT("{losershort}"),LOSERLONG("{loserlong}"),
		MATCHNAME("{matchname}"), MATCHPREFIX("{matchprefix}"),
		EVENTNAME("{eventname}"), EVENTPREFIX("{eventprefix}"),
		CMD("{cmd}"),
		RANKING("{ranking}"),
		RATING("{rating}"),
		SECONDS("{seconds}"), TIME("{time}"),
		PARTICIPANTS("{participants}"),
		NTEAMS("{nteams}"),
		PLAYERORTEAM("{playerorteam}"),
		LIFELEFT("{lifeleft}"),
		TEAMS("{teams}");
		private String replaceString;

		private MessageOption(String replaceString){
			this.replaceString = replaceString;
		}
		public String getReplaceString(){
			return replaceString;
		}
	}

	final Set<MessageOption> options = new HashSet<MessageOption>();

	public MessageOptions(String msg) {
		for (MessageOption mop: MessageOption.values()){
			if (StringUtils.indexOf(msg, mop.getReplaceString()) != -1){
//				System.out.println("Message " + msg + "   contains " + mop);
				options.add(mop);
			}
		}
	}

	public Set<MessageOption> getOptions() {
		return options;
	}

}
