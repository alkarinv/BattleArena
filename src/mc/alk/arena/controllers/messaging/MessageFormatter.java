package mc.alk.arena.controllers.messaging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import mc.alk.arena.controllers.StatController;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.messaging.Message;
import mc.alk.arena.objects.messaging.MessageOptions.MessageOption;
import mc.alk.arena.objects.stats.ArenaStat;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.serializers.MessageSerializer;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TimeUtil;


/**
 * @author alkarin
 *
 * at the moment I hate this class and how it works, but it works at the moment.
 * need to revisit this later
 */
public class MessageFormatter{
	final String[] searchList;
	final String[] replaceList;
	final StatController sc;

	final Set<MessageOption> ops;
	final Message msg;
	HashMap<Integer,ArenaStat> stats = null;
	final HashMap<Integer,TeamNames> tns;
	final MatchParams mp;
	final String typeName;
	final MessageSerializer impl;
	int commonIndex = 0, teamIndex = 0, curIndex = 0;

	public MessageFormatter(MessageSerializer impl, MatchParams mp, int size, int nTeams, Message message, Set<MessageOption> ops){
		searchList = new String[size];
		replaceList = new String[size];
		this.msg = message;
		this.ops = ops;
		tns = new HashMap<Integer,TeamNames>(nTeams);
		this.mp = mp;
		typeName = mp.getType().getName();
		sc = new StatController(mp);
		stats = new HashMap<Integer,ArenaStat>();
		this.impl = impl;
	}

	public void formatCommonOptions(Collection<ArenaTeam> teams){
		formatCommonOptions(teams,null);
	}

	public void formatCommonOptions(Collection<ArenaTeam> teams, Integer seconds){
		int i = 0;
		ArenaTeam t1 =null,t2 = null;
		if (teams != null){
			int j=0;
			for (ArenaTeam t: teams){
				if (j == 0)
					t1 = t;
				else if (j==1)
					t2 = t;
				else
					break;
				j++;
			}
		}

		for (MessageOption mop : ops){
			if (mop == null)
				continue;
			try{
				switch(mop){
				case CMD: replaceList[i] = mp.getCommand(); break;
				case PREFIX:
				case MATCHPREFIX:
				case EVENTPREFIX: replaceList[i] = mp.getPrefix();
					break;
				case COMPNAME:
				case EVENTNAME:
				case MATCHNAME: replaceList[i] = mp.getName();
					break;
				case SECONDS: replaceList[i] = seconds != null ? seconds.toString(): null; break;
				case TIME: replaceList[i] = seconds != null ? TimeUtil.convertSecondsToString(seconds): null; break;
				case TEAM1:
					replaceList[i] = formatTeamName(impl.getNodeMessage("common.team"),t1);
					break;
				case TEAM2:
					replaceList[i] = formatTeamName(impl.getNodeMessage("common.team"),t2);
					break;
				case TEAMSHORT1:
					replaceList[i] = formatTeamName(impl.getNodeMessage("common.teamshort"),t1);
					break;
				case TEAMSHORT2:
					replaceList[i] = formatTeamName(impl.getNodeMessage("common.teamshort"),t2);
					break;
				case TEAMLONG1:
					replaceList[i] = formatTeamName(impl.getNodeMessage("common.teamlong"),t1);
					break;
				case TEAMLONG2:
					replaceList[i] = formatTeamName(impl.getNodeMessage("common.teamlong"),t2);
					break;
				case NTEAMS: replaceList[i] = teams != null ? teams.size()+"" : "0"; break;
				case PLAYERORTEAM: replaceList[i] = teams!=null? MessageUtil.getTeamsOrPlayers(mp.getMaxTeamSize()) : "teams"; break;
				case PARTICIPANTS:{
					StringBuilder sb = new StringBuilder();
					boolean first = true;
					for (ArenaTeam t: teams){
						if (!first) sb.append(", ");
						TeamNames tn = getTeamNames(t);
						if (tn == null){
							sb.append(t.getDisplayName());
						} else if (tn.longName != null){
							sb.append(tn.longName);
						} else if (tn.shortName != null){
							sb.append(tn.shortName);
						} else if (tn.name != null){
							sb.append(tn.name);
						} else {
							sb.append(t.getDisplayName());
						}
						first = false;
					}
					replaceList[i] = sb.toString();
				}
				break;

				default:
					continue;
				}
			} catch (Exception e){
				e.printStackTrace();
			}
			searchList[i] = mop.getReplaceString();
			i++;
		}
		commonIndex = i;
	}

	public void formatPlayerOptions(ArenaPlayer player){
		int i = commonIndex;
		for (MessageOption mop : ops){
			if (mop == null)
				continue;
			switch(mop){
			case PLAYERNAME: replaceList[i] = player.getDisplayName(); break;
			default:
				continue;
			}
			searchList[i] = mop.getReplaceString();
			i++;
		}
		teamIndex = i;
		curIndex = i;
	}

	public void formatTeamOptions(ArenaTeam team, boolean isWinner){

		int i = commonIndex;
		TeamNames tn = getTeamNames(team);
		for (MessageOption mop : ops){
			if (mop == null)
				continue;
			switch(mop){
			case NAME: replaceList[i] = team.getDisplayName(); break;
			case TEAM: replaceList[i] = tn.name;break;
			case TEAMSHORT: replaceList[i] = tn.shortName;break;
			case TEAMLONG: replaceList[i] = tn.longName;break;
			case WINS: replaceList[i] = getStat(team).getWins()+""; break;
			case LOSSES: replaceList[i] = getStat(team).getLosses()+""; break;
			case RANKING: replaceList[i] = getStat(team).getRanking()+"" ; break;
			case RATING: replaceList[i] = getStat(team).getRating()+"" ; break;
			default:
				continue;
			}
			searchList[i] = mop.getReplaceString();
			i++;
		}
		teamIndex = i;
		curIndex = i;
	}

	public void formatTwoTeamsOptions(ArenaTeam t, Collection<ArenaTeam> teams){
		ArenaTeam oteam = null;
		ArenaStat st1 = null;
		int i = teamIndex;
		for (MessageOption mop : ops){
			if (mop == null)
				continue;
			String repl = null;
			switch(mop){
			case OTHERTEAM:
				oteam = getOtherTeam(t,teams);
				if (oteam != null)
					repl = oteam.getDisplayName();
				break;
			case WINSAGAINST:
				try{
					st1 = getStat(t);
					oteam = getOtherTeam(t,teams);
					if (oteam != null){
						ArenaStat st2 = getStat(oteam);
						repl = st1.getWinsVersus(st2)+"";
					} else {
						repl = "0";
					}
				} catch(Exception e){
					e.printStackTrace();
				}

				break;
			case LOSSESAGAINST:
				try{
					st1 = getStat(t);
					oteam = getOtherTeam(t,teams);
					if (oteam != null){
						ArenaStat st2 = getStat(oteam);
						repl = st1.getLossesVersus(st2) +"";
					} else {
						repl = "0";
					}
				} catch(Exception e){
					e.printStackTrace();
				}
				break;
			default:
				continue;
			}

			searchList[i] = mop.getReplaceString();
			replaceList[i] = repl;
			i++;
		}
		curIndex = i;
	}

	public void formatTeams(Collection<ArenaTeam> teams){
		if (ops.contains(MessageOption.TEAMS)){
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (ArenaTeam team: teams){
				if (!first) sb.append(", ");
				else first = false;
				sb.append(team.getDisplayName());
			}

			replaceList[curIndex] = sb.toString();
			searchList[curIndex] = MessageOption.TEAMS.getReplaceString();
			curIndex++;
		}
	}

	public void formatWinnerOptions(ArenaTeam team, boolean isWinner){
		int i = curIndex;
		TeamNames tn = getTeamNames(team);
		for (MessageOption mop : ops){
			if (mop == null)
				continue;
			switch(mop){
			case WINNER:
				if (!isWinner)
					continue;
				replaceList[i] = tn.name;
				break;
			case WINNERSHORT:
				if (!isWinner)
					continue;
				replaceList[i] = tn.shortName;
				break;
			case WINNERLONG:
				if (!isWinner)
					continue;
				replaceList[i] = tn.longName;
				break;
			case LOSER:
				if (isWinner)
					continue;
				replaceList[i] = tn.name;
				break;
			case LOSERSHORT:
				if (isWinner)
					continue;
				replaceList[i] = tn.shortName;
				break;
			case LOSERLONG:
				if (isWinner)
					continue;
				replaceList[i] = tn.longName;
				break;
			case LIFELEFT:
				if (!isWinner)
					continue;
				{
					StringBuilder sb = new StringBuilder();
					boolean first = true;

					for (ArenaPlayer ap: team.getLivingPlayers()){
						if (!first) sb.append(", ");
						sb.append("&6" + ap.getDisplayName()+"&e(&4" + ap.getHealth()+"&e)");
						first = false;
					}
					for (ArenaPlayer ap: team.getDeadPlayers()){
						if (!first) sb.append(", ");
						sb.append("&6" + ap.getDisplayName()+"&e(&8Dead&e)");
						first = false;
					}
					replaceList[i] = sb.toString();
					break;
				}
			default:
				continue;
			}
			searchList[i] = mop.getReplaceString();
			i++;
		}
		curIndex = i;
	}


	public class TeamNames{
		public String longName = null, shortName = null, name = null;
	}


	private TeamNames getTeamNames(ArenaTeam t) {
		if (tns.containsKey(t.getId()))
			return tns.get(t.getId());
		TeamNames tn = new TeamNames();
		formatTeamNames(ops,t,tn);

		tns.put(t.getId(), tn);
		return tn;
	}


	private ArenaTeam getOtherTeam(ArenaTeam t, Collection<ArenaTeam> teams) {
		for (ArenaTeam oteam: teams){
			if (oteam.getId() != t.getId()){
				return oteam;
			}
		}
		return null;
	}


	private ArenaStat getStat(ArenaTeam t) {
		if (stats.containsKey(t.getName()))
			return stats.get(t.getName());
		ArenaStat st = sc.loadRecord(t);
		stats.put(t.getId(), st);
		return st;
	}


	private String formatTeamName(Message message, ArenaTeam t) {
		if (t== null)
			return null;
		Set<MessageOption> ops = message.getOptions();
		String[] searchList = new String[ops.size()];
		String[] replaceList = new String[ops.size()];

		int i=0;

		for (MessageOption mop : ops){
			if (mop == null)
				continue;

			String repl = null;
			switch(mop){
			case NAME: repl = t.getDisplayName(); break;
			case WINS: repl = getStat(t).getWins()+""; break;
			case LOSSES: repl = getStat(t).getLosses()+"" ; break;
			case RANKING: repl = getStat(t).getRanking()+""; break;
			case RATING: repl = getStat(t).getRating()+""; break;
			default:
				continue;
			}
			searchList[i] = mop.getReplaceString();
			replaceList[i] = repl;
			i++;
		}

		return replaceEach(message.getMessage(), searchList, replaceList);
	}

	public static String replaceEach(String text, String[] searchList, String[] replacementList) {
		return replaceEach(text, searchList, replacementList, false, 0);
	}

	public void formatTeamNames(Set<MessageOption> ops, ArenaTeam team, TeamNames tn){
		if (ops.contains(MessageOption.TEAM) || ops.contains(MessageOption.WINNER) || ops.contains(MessageOption.LOSER)){
			tn.name = formatTeamName(impl.getNodeMessage("common.team"),team);
		}
		if (ops.contains(MessageOption.TEAMSHORT) || ops.contains(MessageOption.WINNERSHORT) || ops.contains(MessageOption.LOSERSHORT)){
			tn.shortName = formatTeamName(impl.getNodeMessage("common.teamshort"),team);
		}
		if (ops.contains(MessageOption.TEAMLONG) || ops.contains(MessageOption.WINNERLONG) || ops.contains(MessageOption.LOSERLONG)){
			tn.longName = formatTeamName(impl.getNodeMessage("common.teamlong"),team);
		}
	}

	public String getFormattedMessage(Message message) {
		return replaceEach(message.getMessage(), searchList, replaceList);
	}
	/**
	 * <p>
	 * Replaces all occurrences of Strings within another String.
	 * </p>
	 *
	 * <p>
	 * A <code>null</code> reference passed to this method is a no-op, or if
	 * any "search string" or "string to replace" is null, that replace will be
	 * ignored.
	 * </p>
	 *
	 * <pre>
	 *  StringUtils.replaceEach(null, *, *, *) = null
	 *  StringUtils.replaceEach("", *, *, *) = ""
	 *  StringUtils.replaceEach("aba", null, null, *) = "aba"
	 *  StringUtils.replaceEach("aba", new String[0], null, *) = "aba"
	 *  StringUtils.replaceEach("aba", null, new String[0], *) = "aba"
	 *  StringUtils.replaceEach("aba", new String[]{"a"}, null, *) = "aba"
	 *  StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{""}, *) = "b"
	 *  StringUtils.replaceEach("aba", new String[]{null}, new String[]{"a"}, *) = "aba"
	 *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}, *) = "wcte"
	 *  (example of how it repeats)
	 *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}, false) = "dcte"
	 *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}, true) = "tcte"
	 *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "ab"}, *) = IllegalArgumentException
	 * </pre>
	 *
	 * @param text
	 *            text to search and replace in, no-op if null
	 * @param searchList
	 *            the Strings to search for, no-op if null
	 * @param replacementList
	 *            the Strings to replace them with, no-op if null
	 * @param repeat if true, then replace repeatedly
	 *       until there are no more possible replacements or timeToLive < 0
	 * @param timeToLive
	 *            if less than 0 then there is a circular reference and endless
	 *            loop
	 * @return the text with any replacements processed, <code>null</code> if
	 *         null String input
	 * @throws IllegalArgumentException
	 *             if the search is repeating and there is an endless loop due
	 *             to outputs of one being inputs to another
	 * @throws IndexOutOfBoundsException
	 *             if the lengths of the arrays are not the same (null is ok,
	 *             and/or size 0)
	 * @since 2.4
	 */
	private static String replaceEach(String text, String[] searchList, String[] replacementList,
			boolean repeat, int timeToLive)
	{

		// mchyzer Performance note: This creates very few new objects (one major goal)
		// let me know if there are performance requests, we can create a harness to measure

		if (text == null || text.length() == 0 || searchList == null ||
				searchList.length == 0 || replacementList == null || replacementList.length == 0)
		{
			return text;
		}

		// if recursing, this shouldnt be less than 0
		if (timeToLive < 0) {
			throw new IllegalStateException("TimeToLive of " + timeToLive + " is less than 0: " + text);
		}

		int searchLength = searchList.length;
		int replacementLength = replacementList.length;

		// make sure lengths are ok, these need to be equal
		if (searchLength != replacementLength) {
			throw new IllegalArgumentException("Search and Replace array lengths don't match: "
					+ searchLength
					+ " vs "
					+ replacementLength);
		}

		// keep track of which still have matches
		boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];

		// index on index that the match was found
		int textIndex = -1;
		int replaceIndex = -1;
		int tempIndex = -1;

		// index of replace array that will replace the search string found
		// NOTE: logic duplicated below START
		for (int i = 0; i < searchLength; i++) {
			if (noMoreMatchesForReplIndex[i] || searchList[i] == null ||
					searchList[i].length() == 0 || replacementList[i] == null)
			{
				continue;
			}
			tempIndex = text.indexOf(searchList[i]);

			// see if we need to keep searching for this
			if (tempIndex == -1) {
				noMoreMatchesForReplIndex[i] = true;
			} else {
				if (textIndex == -1 || tempIndex < textIndex) {
					textIndex = tempIndex;
					replaceIndex = i;
				}
			}
		}
		// NOTE: logic mostly below END

		// no search strings found, we are done
		if (textIndex == -1) {
			return text;
		}

		int start = 0;

		// get a good guess on the size of the result buffer so it doesnt have to double if it goes over a bit
		int increase = 0;

		// count the replacement text elements that are larger than their corresponding text being replaced
		for (int i = 0; i < searchList.length; i++) {
			if (searchList[i] == null || replacementList[i] == null) {
				continue;
			}
			int greater = replacementList[i].length() - searchList[i].length();
			if (greater > 0) {
				increase += 3 * greater; // assume 3 matches
			}
		}
		// have upper-bound at 20% increase, then let Java take over
		increase = Math.min(increase, text.length() / 5);

		StringBuffer buf = new StringBuffer(text.length() + increase);

		while (textIndex != -1) {

			for (int i = start; i < textIndex; i++) {
				buf.append(text.charAt(i));
			}
			buf.append(replacementList[replaceIndex]);

			start = textIndex + searchList[replaceIndex].length();

			textIndex = -1;
			replaceIndex = -1;
			tempIndex = -1;
			// find the next earliest match
			// NOTE: logic mostly duplicated above START
			for (int i = 0; i < searchLength; i++) {
				if (noMoreMatchesForReplIndex[i] || searchList[i] == null ||
						searchList[i].length() == 0 || replacementList[i] == null)
				{
					continue;
				}
				tempIndex = text.indexOf(searchList[i], start);

				// see if we need to keep searching for this
				if (tempIndex == -1) {
					noMoreMatchesForReplIndex[i] = true;
				} else {
					if (textIndex == -1 || tempIndex < textIndex) {
						textIndex = tempIndex;
						replaceIndex = i;
					}
				}
			}
			// NOTE: logic duplicated above END

		}
		int textLength = text.length();
		for (int i = start; i < textLength; i++) {
			buf.append(text.charAt(i));
		}
		String result = buf.toString();
		if (!repeat) {
			return result;
		}

		return replaceEach(result, searchList, replacementList, repeat, timeToLive - 1);
	}
	public void printMap(){
		System.out.println("!!!!!!!!!!!!!! " + commonIndex +"   " + teamIndex);
		for (int i=0;i<searchList.length;i++){
			System.out.println(i +" : " + replaceList[i] +"  ^^^ " + searchList[i]);
		}
	}
}