package mc.alk.arena.objects.spawns;

import mc.alk.arena.BattleArena;
import mc.alk.arena.util.SerializerUtil;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;


public class TimedSpawn implements Spawnable{
	static int count=0;
	SpawnInstance sg;
	final int id = count++;

	Long firstSpawnTime, respawnInterval, timeToDespawn;
    BukkitTask despawnTimer;

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
        if (timeToDespawn > 0){
            if (despawnTimer != null)
                Bukkit.getScheduler().cancelTask(despawnTimer.getTaskId());
            despawnTimer = new BukkitRunnable() {
                @Override
                public void run() {
                    despawn();
                }
            }.runTaskLater(BattleArena.getSelf(),timeToDespawn*20L);

        }
    }

    @Override
	public String toString() {
        return "[TimedSpawn " + id + " loc=" + SerializerUtil.getLocString(sg.getLocation()) + " s=" + sg + " fs=" +
                firstSpawnTime + " rs=" + respawnInterval + " ds=" + timeToDespawn+"]";
    }
    public String getDisplayName() {
        return "["+ sg+" loc=" + SerializerUtil.getLocString(sg.getLocation()) + " fs=" +
                firstSpawnTime + " rs=" + respawnInterval + " ds=" + timeToDespawn+"]";
    }
}
