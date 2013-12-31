package mc.alk.arena.objects.options;

import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.serializers.ConfigSerializer;
import mc.alk.arena.util.MinMax;

public enum GameOption{
	NLIVES ("nLives", true, false),
	TEAMSIZE("teamSize",true, false),
	NTEAMS("nTeams",true, false),
	PREFIX("prefix",true, false),
	PRESTARTTIME("secondsTillMatch",true, false),
	FORCESTARTTIME("forceStartTime",true, false),
	MATCHTIME("matchTime",true, false),
	VICTORYTIME("secondsToLoot",true, false),
	VICTORYCONDITION("victoryCondition",true, false),
	COMMAND("command",true, false),
	RATED("rated",true, false),
	GIVEITEMS("giveItems",true, false),
	ALLOWEDTEAMSIZEDIFFERENCE("allowedTeamSizeDifference",true,false),
	CLOSEWAITROOMWHILERUNNING("closeWaitroomWhileRunning", true,false),
	CANCELIFNOTENOUGHPLAYERS("cancelIfNotEnoughPlayers", true,false)
	;

	final String name;

	final boolean needsValue; /// whether the transition needs a value

	final boolean needsPlayer; /// whether we need a player

	GameOption(String name, Boolean hasValue, Boolean needsPlayer){
		this.name = name;
		this.needsValue = hasValue;
		this.needsPlayer = needsPlayer;
	}

	public boolean hasValue(){return needsValue;}

	public boolean needsPlayer(){return needsPlayer;}

	@Override
	public String toString(){return name;}

	public static GameOption fromString(String str){
		str = str.toUpperCase();
		try {
			return GameOption.valueOf(str);
		} catch (IllegalArgumentException e){
			if (str.equalsIgnoreCase("secondsTillMatch"))
				return GameOption.PRESTARTTIME;
			if (str.equalsIgnoreCase("secondsToLoot"))
				return GameOption.VICTORYTIME;
			if (str.equalsIgnoreCase("items"))
				return GameOption.GIVEITEMS;
			if (str.equalsIgnoreCase("waitroomClosedWhileRunning"))
				return GameOption.CLOSEWAITROOMWHILERUNNING;
			return null;
		}
	}

	public static Object getValue(GameOption go, String value) {
		switch (go){
		case TEAMSIZE:
		case NTEAMS:
			return MinMax.valueOf(value);
		case VICTORYTIME:
		case PRESTARTTIME:
		case MATCHTIME:
		case FORCESTARTTIME:
		case NLIVES:
		case ALLOWEDTEAMSIZEDIFFERENCE:
			return ConfigSerializer.toPositiveSize(value, -1);
		case PREFIX:
		case COMMAND:
			return value;
		case VICTORYCONDITION:
			return VictoryType.fromString(value);
		case CANCELIFNOTENOUGHPLAYERS:
		case CLOSEWAITROOMWHILERUNNING:
		case RATED:
			return Boolean.valueOf(value);
		default:
			break;
		}
		return null;
	}
};
