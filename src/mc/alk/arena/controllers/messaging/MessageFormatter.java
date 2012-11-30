package mc.alk.arena.controllers.messaging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.messaging.Message;
import mc.alk.arena.objects.messaging.MessageOptions.MessageOption;
import mc.alk.arena.objects.teams.Team;
import mc.alk.arena.serializers.MessageSerializer;
import mc.alk.arena.util.BTInterface;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TimeUtil;
import mc.alk.tracker.objects.Stat;
import mc.alk.tracker.objects.VersusRecords.VersusRecord;

import org.apache.commons.lang3.StringUtils;


/**
 * @author alkarin
 *
 * at the moment I hate this class and how it works, but it works at the moment.
 * need to revisit this later
 */
public class MessageFormatter{
	final String[] searchList;
	final String[] replaceList;
	final BTInterface bti;
	final Set<MessageOption> ops;
	final Message msg;
	HashMap<Integer,Stat> stats = null;
	final HashMap<Integer,TeamNames> tns;
	final MatchParams mp;
	final String typeName;
	final MessageSerializer impl;
	int commonIndex = 0, teamIndex = 0, curIndex = 0;
	//	final String matchOrEvent;

	public MessageFormatter(MessageSerializer impl, MatchParams mp, int size, int nTeams, Message message, Set<MessageOption> ops){
		searchList = new String[size];
		replaceList = new String[size];
		this.msg = message;
		this.ops = ops;
		tns = new HashMap<Integer,TeamNames>(nTeams);
		//		matchOrEvent = isMatch ? "match" : "event";
		this.mp = mp;
		typeName = mp.getType().getName();
		bti = new BTInterface(mp);
		if (bti.isValid()){
			stats = new HashMap<Integer,Stat>();
		}
		this.impl = impl;
	}

	public void formatCommonOptions(Collection<Team> teams){
		formatCommonOptions(teams,null);
	}
	public void formatCommonOptions(Collection<Team> teams, Integer seconds){
		int i = 0;
		Team t1 =null,t2 = null;
		if (teams != null){
			int j=0;
			for (Team t: teams){
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
				case MATCHPREFIX: replaceList[i] = mp.getPrefix(); break;
				case MATCHNAME: replaceList[i] = mp.getName(); break;
				case EVENTPREFIX: replaceList[i] = mp.getPrefix(); break;
				case EVENTNAME: replaceList[i] = mp.getName(); break;
				case SECONDS: replaceList[i] = seconds != null ? seconds.toString(): null; break;
				case TIME: replaceList[i] = seconds != null ? TimeUtil.convertSecondsToString(seconds): null; break;
				case TEAM1:
					replaceList[i] = formatTeamName(impl.getMessage("common.team"),t1);
					break;
				case TEAM2:
					replaceList[i] = formatTeamName(impl.getMessage("common.team"),t2);
					break;
				case TEAMSHORT1:
					replaceList[i] = formatTeamName(impl.getMessage("common.teamshort"),t1);
					break;
				case TEAMSHORT2:
					replaceList[i] = formatTeamName(impl.getMessage("common.teamshort"),t2);
					break;
				case TEAMLONG1:
					replaceList[i] = formatTeamName(impl.getMessage("common.teamlong"),t1);
					break;
				case TEAMLONG2:
					replaceList[i] = formatTeamName(impl.getMessage("common.teamlong"),t2);
					break;
				case NTEAMS: replaceList[i] = teams != null ? teams.size()+"" : "0"; break;
				case PLAYERORTEAM: replaceList[i] = teams!=null? MessageUtil.getTeamsOrPlayers(teams.size()) : "teams"; break;
				case PARTICIPANTS:{
					StringBuilder sb = new StringBuilder();
					boolean first = true;
					for (Team t: teams){
						if (!first) sb.append("&e, ");
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

	@SuppressWarnings("deprecation")
	public void formatTeamOptions(Team team, boolean isWinner){

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
			case WINS: replaceList[i] = bti.isValid() ? getStat(bti,stats,team).getWins()+"" : "0"; break;
			case LOSSES: replaceList[i] = bti.isValid() ? getStat(bti,stats,team).getLosses()+"" : "0"; break;
			case RANKING: replaceList[i] = bti.isValid() ? getStat(bti,stats,team).getRanking()+"" : "0"; break;
			case RATING: replaceList[i] = bti.isValid() ? getStat(bti,stats,team).getRanking()+"" : "0"; break;
			default:
				continue;
			}
			searchList[i] = mop.getReplaceString();
			i++;
		}
		teamIndex = i;
		curIndex = i;
	}

	public void formatTwoTeamsOptions(Team t, Collection<Team> teams){
		Team oteam = null;
		Stat st1 = null;
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
				if (bti.isValid()){
					try{
						st1 = getStat(bti,stats,t);
						oteam = getOtherTeam(t,teams);
						if (oteam != null){
							Stat st2 = getStat(bti,stats,oteam);
							VersusRecord vr = st1.getRecordVersus(st2);
							repl = vr.wins +"";
						} else {
							repl = "0";
						}
					} catch(Exception e){
						e.printStackTrace();
					}
				} else{
					repl = "0";
				}

				break;
			case LOSSESAGAINST:
				if (bti.isValid()){
					try{
						st1 = getStat(bti,stats,t);
						oteam = getOtherTeam(t,teams);
						if (oteam != null){
							Stat st2 = getStat(bti,stats,oteam);
							VersusRecord vr = st1.getRecordVersus(st2);
							repl = vr.losses +"";
						} else {
							repl = "0";
						}
					} catch(Exception e){
						e.printStackTrace();
					}
				} else{
					repl = "0";
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

	public void formatTeams(Collection<Team> teams){
		if (ops.contains(MessageOption.TEAMS)){
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (Team team: teams){
				if (!first) sb.append(", ");
				else first = false;
				sb.append(team.getDisplayName());
			}

			replaceList[curIndex] = sb.toString();
			searchList[curIndex] = MessageOption.TEAMS.getReplaceString();
			curIndex++;
		}
	}

	public void formatWinnerOptions(Team team, boolean isWinner){
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
						if (!first) sb.append("&e, ");
						sb.append("&6" + ap.getDisplayName()+"&e(&4" + ap.getHealth()+"&e)");
						first = false;
					}
					for (ArenaPlayer ap: team.getDeadPlayers()){
						if (!first) sb.append("&e, ");
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


	private TeamNames getTeamNames(Team t) {
		if (tns.containsKey(t.getId()))
			return tns.get(t.getId());
		TeamNames tn = new TeamNames();
		formatTeamNames(ops,t,tn);

		tns.put(t.getId(), tn);
		return tn;
	}


	private Team getOtherTeam(Team t, Collection<Team> teams) {
		for (Team oteam: teams){
			if (oteam.getId() != t.getId()){
				return oteam;
			}
		}
		return null;
	}


	private Stat getStat(BTInterface bti, HashMap<Integer, Stat> stats, Team t) {
		if (stats.containsKey(t.getName()))
			return stats.get(t.getName());
		Stat st = bti.loadRecord(t);
		stats.put(t.getId(), st);
		return st;
	}


	@SuppressWarnings("deprecation")
	private String formatTeamName(Message message, Team t) {
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
			case WINS: repl = bti.isValid() ? getStat(bti,stats,t).getWins()+"" : "0"; break;
			case LOSSES: repl = bti.isValid() ? getStat(bti,stats,t).getLosses()+"" : "0"; break;
			case RANKING: repl = bti.isValid() ? getStat(bti,stats,t).getRanking()+"" : "0"; break;
			default:
				continue;
			}
			searchList[i] = mop.getReplaceString();
			replaceList[i] = repl;
			i++;
		}

		return StringUtils.replaceEach(message.getMessage(), searchList, replaceList);
	}


	public void formatTeamNames(Set<MessageOption> ops, Team team, TeamNames tn){
		if (ops.contains(MessageOption.TEAM) || ops.contains(MessageOption.WINNER) || ops.contains(MessageOption.LOSER)){
			tn.name = formatTeamName(impl.getMessage("common.team"),team);
		}
		if (ops.contains(MessageOption.TEAMSHORT) || ops.contains(MessageOption.WINNERSHORT) || ops.contains(MessageOption.LOSERSHORT)){
			tn.shortName = formatTeamName(impl.getMessage("common.teamshort"),team);
		}
		if (ops.contains(MessageOption.TEAMLONG) || ops.contains(MessageOption.WINNERLONG) || ops.contains(MessageOption.LOSERLONG)){
			tn.longName = formatTeamName(impl.getMessage("common.teamlong"),team);
		}
	}

	public String getFormattedMessage(Message message) {
		return StringUtils.replaceEach(message.getMessage(), searchList, replaceList);
	}

	public void printMap(){
		System.out.println("!!!!!!!!!!!!!! " + commonIndex +"   " + teamIndex);
		for (int i=0;i<searchList.length;i++){
			System.out.println(i +" : " + replaceList[i] +"  ^^^ " + searchList[i]);
		}
	}
}