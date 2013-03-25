package mc.alk.arena.objects.stats;


public class BlankArenaStat implements ArenaStat{
	public static final BlankArenaStat BLANK_STAT = new BlankArenaStat();

	private BlankArenaStat(){}

	public int getWinsVersus(ArenaStat st2) {
		return 0;
	}

	public int getLossesVersus(ArenaStat st2) {
		return 0;
	}

	public int getWins() {
		return 0;
	}

	public int getLosses() {
		return 0;
	}

	public int getRanking() {
		return 1250;
	}

	public int getRating() {
		return 1250;
	}

}
