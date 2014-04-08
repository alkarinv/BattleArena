package mc.alk.arena.objects.stats;



public class BlankArenaStat implements ArenaStat{
	public static final BlankArenaStat BLANK_STAT = new BlankArenaStat();
	private BlankArenaStat(){}

	@Override
    public int getWinsVersus(ArenaStat st2) {
		return 0;
	}

	@Override
    public int getLossesVersus(ArenaStat st2) {
		return 0;
	}

	@Override
    public int getWins() {
		return 0;
	}

	@Override
    public int getLosses() {
		return 0;
	}

	@Override
    public int getRanking() {
		return 1250;
	}

	@Override
    public int getRating() {
		return 1250;
	}

	@Override
	public String getDB() {
		return "";
	}

}
