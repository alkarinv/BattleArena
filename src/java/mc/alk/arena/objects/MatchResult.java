package mc.alk.arena.objects;




public class MatchResult extends CompetitionResult{
	/**
	 * and this is why maybe I shouldn't use nested enums
	 * This is now more appropriate inside of CompetitionResult
	 * but a lot of plugins now rely on it from here
	 */
	public static enum WinLossDraw{
		UNKNOWN, LOSS, DRAW, WIN
	}
}
