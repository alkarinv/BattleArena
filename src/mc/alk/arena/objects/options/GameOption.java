package mc.alk.arena.objects.options;

import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.serializers.ConfigSerializer;
import mc.alk.arena.util.MinMax;

public enum GameOption{
	NLIVES ("nLives"),
	TEAMSIZE("teamSize"),
	NTEAMS("nTeams"),
	PREFIX("prefix"),
	PRESTARTTIME("secondsTillMatch"),
	MATCHTIME("matchTime"),
	VICTORYTIME("secondsToLoot"),
	VICTORYCONDITION("victoryCondition"),
	COMMAND("command"),
	;

	final String name;
//	final Class<?> clazz;

	GameOption(String name){this.name= name;}

	@Override
	public String toString(){return name;}

	public static GameOption fromString(String str){
		str = str.toUpperCase();
		try {
			return GameOption.valueOf(str);
		} catch (IllegalArgumentException e){
			if (str.equals("secondsTillMatch"))
				return GameOption.PRESTARTTIME;
			if (str.equals("secondsToLoot"))
				return GameOption.VICTORYTIME;
			return null;
		}
	}

	public static Object getValue(GameOption go, String value) {
		switch (go){
		case TEAMSIZE:
		case NTEAMS:
			return MinMax.valueOf(value);
		case MATCHTIME:
		case NLIVES:
			return ConfigSerializer.parseSize(value,-1);
		case PREFIX:
		case COMMAND:
			return value;
		case VICTORYCONDITION:
			return VictoryType.fromString(value);
		default:
			break;
		}
		return null;
	}
};
