package mc.alk.arena.objects.stats;

import mc.alk.tracker.objects.Stat;
import mc.alk.tracker.objects.VersusRecords.VersusRecord;

public class TrackerArenaStat implements ArenaStat{
	final Stat stat;
	public TrackerArenaStat(Stat stat) {
		this.stat = stat;
	}

	@Override
	public int getWinsVersus(ArenaStat ostat) {
		Stat st2 = ((TrackerArenaStat)ostat).getStat();
		VersusRecord vs = stat.getRecordVersus(st2);
		return vs == null ? 0 : vs.wins;
	}

	@Override
	public int getLossesVersus(ArenaStat ostat) {
		Stat st2 = ((TrackerArenaStat)ostat).getStat();
		VersusRecord vs = stat.getRecordVersus(st2);
		return vs == null ? 0 : vs.losses;
	}

	@Override
	public int getWins() {
		return stat.getWins();
	}

	@Override
	public int getLosses() {
		return stat.getLosses();
	}

	@Override
	public int getRanking() {
		return stat.getRating();
	}

	@Override
	public int getRating() {
		return stat.getRating();
	}

	public Stat getStat(){
		return stat;
	}
}
