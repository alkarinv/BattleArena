package mc.alk.arena.objects.spawns;

import mc.alk.arena.util.SerializerUtil;


public class TimedSpawn{
	static int count=0;
	SpawnInstance sg;
	final int id = count++;

//	private Long timeToNext;

	Long firstSpawnTime, respawnInterval, timeToDespawn;

	public TimedSpawn(long timeToStart, long respawnInterval, long timeToDespawn, SpawnInstance sg){
		this.firstSpawnTime = timeToStart;
		this.sg = sg;
		this.respawnInterval = respawnInterval;
		this.timeToDespawn = timeToDespawn;

	}

	public Long getTimeToNext() {
		return respawnInterval;
	}

	public void setRespawnInterval(Long timeToNext) {
		this.respawnInterval = timeToNext;
	}

	public Long getTimeToStart() {
		return firstSpawnTime;
	}

	public void setFirstSpawnTime(Long timeToStart) {
		this.firstSpawnTime = timeToStart;
	}

	public void despawn() {
		sg.despawn();
	}

	public int spawn() {
		return sg.spawn();
	}

	public int getId(){
		return id;
	}

	public SpawnInstance getSpawn() {
		return sg;
	}

	public Long getTimeToDespawn() {
		return timeToDespawn;
	}

	public Long getRespawnInterval() {
		return respawnInterval;
	}
	public String toString(){
		return "[TimedSpawn "+id+" loc="+SerializerUtil.getLocString(sg.getLocation()) + " sg=" + sg+"]";
	}
}
