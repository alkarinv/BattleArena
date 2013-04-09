package mc.alk.arena.controllers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.objects.spawns.SpawnInstance;
import mc.alk.arena.objects.spawns.TimedSpawn;
import mc.alk.arena.util.CaseInsensitiveMap;
import mc.alk.arena.util.TimeUtil;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;



public class SpawnController {
	static final boolean DEBUG_SPAWNS = false;
	static CaseInsensitiveMap<SpawnInstance> allSpawns = new CaseInsensitiveMap<SpawnInstance>();

	PriorityQueue<NextSpawn> spawnQ = null;
	Plugin plugin = null;

	Map<Long, TimedSpawn> timedSpawns= null;
	Integer timerId = null;

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
				} catch (Exception e){}
			}
		}
	}

	public void start(){
		System.out.println("Arena::onStart " + timedSpawns);
		if (timedSpawns != null && !timedSpawns.isEmpty()){
			Plugin plugin = BattleArena.getSelf();
			/// Create our Q, with a descending Comparator
			spawnQ = new PriorityQueue<NextSpawn>(timedSpawns.size(), new Comparator<NextSpawn>(){
				public int compare(NextSpawn o1, NextSpawn o2) {
					return (o1.timeToNext.compareTo(o2.timeToNext));
				}
			});
			/// TeamJoinResult our items into the Q
			ArrayList<NextSpawn> nextspawns = new ArrayList<NextSpawn>();
			for (TimedSpawn is: timedSpawns.values()){
//				System.out.println("itemSpawns = " + timedSpawns.size() + " " + is.getFirstSpawnTime()+ "  ts=" + is);
				long tts = is.getFirstSpawnTime();
				if (tts == 0)
					is.spawn();
				NextSpawn ns = new NextSpawn(is, tts);
				spawnQ.add(ns);
				nextspawns.add(ns);
			}

			timerId = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new SpawnNextEvent(0L));
		}
	}

	public class SpawnNextEvent implements Runnable{
		Long nextTimeToSpawn = null;
		public SpawnNextEvent(Long nextTimeToSpawn){
			this.nextTimeToSpawn = nextTimeToSpawn;
		}

		public void run() {
			if (DEBUG_SPAWNS) System.out.println("SpawnNextEvent::run " + nextTimeToSpawn);
			TimeUtil.testClock();

			/// Subtract the time passed from each element
			for (NextSpawn next : spawnQ){ /// we dont need to resort after this as we are subtracting a constant from all
				next.timeToNext -= nextTimeToSpawn;
				if (DEBUG_SPAWNS) System.out.println("     " + next.timeToNext +"  " + next.is +"   ");
			}
			/// Find all the elements that should spawn at this time
			NextSpawn ns = null;
			boolean stop = false;
			while (!spawnQ.isEmpty() && !stop){ /// Keep iterating until we have times that dont match
				ns = spawnQ.peek();
				stop = spawnQ.peek().timeToNext != 0;
				if (!stop){
					ns = spawnQ.remove();
					ns.is.spawn();
					/// Now we have to add back the items we spawned into the Q with their original time lengths
					ns.timeToNext = ns.is.getRespawnTime();
					/// spawn time!!
					spawnQ.add(ns);
				}
			}

			nextTimeToSpawn = spawnQ.peek().timeToNext;
			if (DEBUG_SPAWNS) System.out.println("run SpawnNextEvent " + spawnQ.size() +"  next=" + nextTimeToSpawn);
			timerId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new SpawnNextEvent(nextTimeToSpawn),
					(long)(Defaults.TICK_MULT*nextTimeToSpawn*20));
		}
	}

	public static void registerSpawn(String s, SpawnInstance sg) {
//		System.out.println("Adding spawn group " + s +"  sg=" + sg);
		allSpawns.put(s,sg);
	}

	public static SpawnInstance getSpawnable(String name) {
		return allSpawns.get(name);
	}

}
