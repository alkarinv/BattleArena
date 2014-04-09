package mc.alk.arena.objects.options;

import mc.alk.arena.objects.ArenaSize;
import mc.alk.arena.objects.arenas.ArenaType;
import mc.alk.arena.objects.victoryconditions.VictoryType;
import mc.alk.arena.serializers.ConfigSerializer;
import mc.alk.arena.util.MinMax;

public enum AlterParamOption {
    NLIVES ("nLives", true, false),
    TEAMSIZE("teamSize",true, false),
    NTEAMS("nTeams",true, false),
    PREFIX("prefix",true, false),
    PRESTARTTIME("secondsTillMatch",true, false),
    FORCESTARTTIME("forceStartTime",true, false),
    MATCHTIME("matchTime",true, false),
    GAMETYPE("gameType",true, false),
    VICTORYTIME("secondsToLoot",true, false),
    VICTORYCONDITION("victoryCondition",true, false),
    NCUMONCURRENTCOMPETITIONS("numConcurrentCompetitions", true, false),
    COMMAND("command",true, false),
    SIGNDISPLAYNAME("signDisplayName",true, false),
    DISPLAYNAME("displayName",true, false),
    DATABASE("db",true, false),
    RATED("rated",true, false),
    USETRACKERMESSAGES("useTrackerMessages",true, false),
    GIVEITEMS("giveItems",true, false),
    NEEDITEMS("needItems",true, false),
    TAKEITEMS("takeItems",true, false),
    ALLOWEDTEAMSIZEDIFFERENCE("allowedTeamSizeDifference",true,false),
    CLOSEWAITROOMWHILERUNNING("closeWaitroomWhileRunning", true,false),
    CANCELIFNOTENOUGHPLAYERS("cancelIfNotEnoughPlayers", true,false)
    ;

    final String name;

    final boolean needsValue; /// whether the transition needs a value

    final boolean needsPlayer; /// whether we need a player

    AlterParamOption(String name, Boolean hasValue, Boolean needsPlayer){
        this.name = name;
        this.needsValue = hasValue;
        this.needsPlayer = needsPlayer;
    }

    public boolean hasValue(){return needsValue;}

    public boolean needsPlayer(){return needsPlayer;}

    @Override
    public String toString(){return name;}

    public static AlterParamOption fromString(String str){
        str = str.toUpperCase();
        try {
            return AlterParamOption.valueOf(str);
        } catch (IllegalArgumentException e){
            if (str.equalsIgnoreCase("secondsTillMatch") || str.equalsIgnoreCase("secondsUntilMatch"))
                return AlterParamOption.PRESTARTTIME;
            if (str.equalsIgnoreCase("gameTime"))
                return AlterParamOption.MATCHTIME;
            if (str.equalsIgnoreCase("numTeams"))
                return AlterParamOption.NTEAMS;
            if (str.equalsIgnoreCase("secondsToLoot"))
                return AlterParamOption.VICTORYTIME;
            if (str.equalsIgnoreCase("victoryTime"))
                return AlterParamOption.VICTORYTIME;
            if (str.equalsIgnoreCase("items"))
                return AlterParamOption.GIVEITEMS;
            if (str.equalsIgnoreCase("db") || str.equalsIgnoreCase("dbTableName"))
                return AlterParamOption.DATABASE;
            if (str.equalsIgnoreCase("waitroomClosedWhileRunning"))
                return AlterParamOption.CLOSEWAITROOMWHILERUNNING;
            if (str.equalsIgnoreCase("nConcurrentCompetitions"))
                return AlterParamOption.NCUMONCURRENTCOMPETITIONS;
            return null;
        }
    }

    public static Object getValue(AlterParamOption go, String value) {
        switch (go){
            case TEAMSIZE:
            case NTEAMS:
                return MinMax.valueOf(value);
            case MATCHTIME:
                return ConfigSerializer.toPositiveSize(value, 30);
            case VICTORYTIME:
            case PRESTARTTIME:
            case FORCESTARTTIME:
                return ConfigSerializer.toNonNegativeSize(value, 1);
            case NLIVES:
                return ConfigSerializer.toPositiveSize(value, ArenaSize.MAX);
            case NCUMONCURRENTCOMPETITIONS:
                return ConfigSerializer.toPositiveSize(value, ArenaSize.MAX);
            case ALLOWEDTEAMSIZEDIFFERENCE:
                return ConfigSerializer.toNonNegativeSize(value, 1);
            case PREFIX:
            case COMMAND:
            case DATABASE:
            case DISPLAYNAME:
            case SIGNDISPLAYNAME:
                return value;
            case VICTORYCONDITION:
                return VictoryType.fromString(value);
            case GAMETYPE:
                return ArenaType.getType(value);
            case CANCELIFNOTENOUGHPLAYERS:
            case CLOSEWAITROOMWHILERUNNING:
            case RATED:
            case USETRACKERMESSAGES:
                return Boolean.valueOf(value);
            default:
                break;
        }
        return null;
    }

}
