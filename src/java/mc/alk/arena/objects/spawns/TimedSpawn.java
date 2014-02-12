package mc.alk.arena.objects.spawns;

import mc.alk.arena.util.SerializerUtil;


public class TimedSpawn implements Spawnable{
	static int count=0;
	SpawnInstance sg;
	final int id = count++;

	Long firstSpawnTime, respawnInterval, timeToDespawn;

	public TimedSpawn(long firstSpawnTime, long respawnTime, long timeToDespawn, SpawnInstance sg){
		this.firstSpawnTime = firstSpawnTime;
		this.sg = sg;
		this.respawnInterval = respawnTime;
		this.timeToDespawn = timeToDespawn;

	}

	public Long getRespawnTime() {
		return respawnInterval;
	}

	public void setRespawnTime(Long timeToNext) {
		this.respawnInterval = timeToNext;
	}

	public Long getFirstSpawnTime() {
		return firstSpawnTime;
	}

	public void setFirstSpawnTime(Long timeToStart) {
		this.firstSpawnTime = timeToStart;
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

    @Override
    public void despawn() {
        sg.despawn();
    }

    @Override
    public void spawn() {
        sg.spawn();
    }

    @Override
	public String toString(){
		return "[TimedSpawn "+id+" loc="+SerializerUtil.getLocString(sg.getLocation()) + " sg=" + sg+"]";
	}
}
