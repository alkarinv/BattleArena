package mc.alk.arena.listeners.custom;

public class TimingStat {
	public int count = 0;
	public long totalTime = 0;
	public long getAverage() {
		return totalTime/count;
	}
}
