package mc.alk.arena.controllers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.spawns.SpawnInstance;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.util.CaseInsensitiveMap;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;



public class SpawnController {
	static final boolean DEBUG_SPAWNS = false;
	static CaseInsensitiveMap<SpawnInstance> allSpawns = new CaseInsensitiveMap<SpawnInstance>();

	PriorityQueue<NextSpawn> spawnQ;
	Plugin plugin;

	Map<Long, TimedSpawn> timedSpawns;
	Integer timerId;

	class NextSpawn{
		TimedSpawn is;
		Long timeToNext;
		/// We are given time in matchEndTime, convert to time in millis
		NextSpawn(TimedSpawn is, Long timeToSpawn){
			this.is = is; this.timeToNext = timeToSpawn;
		}
	}

	public SpawnController(Map<Long, TimedSpawn> spawnGroups) {
		this.timedSpawns = spawnGroups;
		plugin = BattleArena.getSelf();
	}

	public void stop() {
		if (timerId != null){
			Bukkit.getScheduler().cancelTask(timerId);
			timerId = null;
		}
		if (spawnQ != null){
			for (NextSpawn ns: spawnQ){
				try{
					ns.is.despawn();
				} catch (Exception e){
                    Log.printStackTrace(e);
                }
			}
		}
	}

	public void start(){
//		System.out.println("Arena::onStart " + timedSpawns);
		if (timedSpawns != null && !timedSpawns.isEmpty()){
			Plugin plugin = BattleArena.getSelf();
			/// Create our Q, with a descending Comparator
			spawnQ = new PriorityQueue<NextSpawn>(timedSpawns.size(), new Comparator<NextSpawn>(){
				@Override
                public int compare(NextSpawn o1, NextSpawn o2) {
					return (o1.timeToNext.compareTo(o2.timeToNext));
				}
			});
			/// TeamJoinResult our items into the Q
			for (TimedSpawn is: timedSpawns.values()){
//				System.out.println("itemSpawns = " + timedSpawns.size() + " " + is.getFirstSpawnTime()+ "  ts=" + is);
				long tts = is.getFirstSpawnTime();
				if (tts == 0)
					is.spawn();
                if (is.getRespawnTime() <= 0){
                    continue;}
				NextSpawn ns = new NextSpawn(is, tts);
				spawnQ.add(ns);
			}

			timerId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SpawnNextEvent(0L));
		}
	}

	public class SpawnNextEvent implements Runnable{
		Long nextTimeToSpawn = null;
		public SpawnNextEvent(Long nextTimeToSpawn){
			this.nextTimeToSpawn = nextTimeToSpawn;
		}

		@Override
        public void run() {
			if (DEBUG_SPAWNS) Log.info("SpawnNextEvent::run " + nextTimeToSpawn);
			TimeUtil.testClock();

			/// Subtract the time passed from each element
			for (NextSpawn next : spawnQ){ /// we dont need to resort after this as we are subtracting a constant from all
				next.timeToNext -= nextTimeToSpawn;
				if (DEBUG_SPAWNS) Log.info("     " + next.timeToNext +"  " + next.is +"   ");
			}
			/// Find all the elements that should spawn at this time
			NextSpawn ns;
			boolean stop = false;
			while (!spawnQ.isEmpty() && !stop){ /// Keep iterating until we have times that dont match
				stop = spawnQ.peek().timeToNext != 0;
				if (!stop){
					ns = spawnQ.remove();
					ns.is.spawn();
					/// Now we have to add back the items we spawned into the Q with their original time lengths
					ns.timeToNext = ns.is.getRespawnTime();
                    if (ns.timeToNext <= 0) /// don't add back ones that won't respawn
                        continue;
					/// spawn time!!
					spawnQ.add(ns);
				}
			}

			ns = spawnQ.peek();
            if (ns == null){ /// we are out of spawns
                return;}
            nextTimeToSpawn = ns.timeToNext;
			if (DEBUG_SPAWNS) Log.info("run SpawnNextEvent " + spawnQ.size() +"  next=" + nextTimeToSpawn);
			timerId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new SpawnNextEvent(nextTimeToSpawn),
                    nextTimeToSpawn*20);
		}
	}

	public static void registerSpawn(String s, SpawnInstance sg) {
		allSpawns.put(s,sg);
	}

	public static SpawnInstance getSpawnable(String name) {
		return allSpawns.get(name);
	}

}
