package mc.alk.arena.objects.stats;


public interface ArenaStat {
	public int getWinsVersus(ArenaStat stat);
	public int getLossesVersus(ArenaStat stat);

	public int getWins();
	public int getLosses();
	public int getRanking();
	public int getRating();

}
