package mc.alk.arena.controllers;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.events.Event;
import mc.alk.arena.events.events.EventFinishedEvent;
import mc.alk.arena.executors.EventExecutor;
import mc.alk.arena.executors.ReservedArenaEventExecutor;
import mc.alk.arena.executors.TournamentExecutor;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.MatchEventHandler;
import mc.alk.arena.objects.exceptions.InvalidEventException;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.pairs.EventPair;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TimeUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class EventScheduler implements Runnable, ArenaListener{

	int curEvent = 0;
	boolean continuous= false;
	boolean running = false;
	boolean stop = false;
	Integer currentTimer = null;

	final CopyOnWriteArrayList<EventPair> events = new CopyOnWriteArrayList<EventPair>();

	@Override
	public void run() {
		if (events.isEmpty() || stop)
			return;
		running = true;
		int index = curEvent % events.size();
		curEvent++;
		currentTimer = Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new RunEvent(this, events.get(index)));
	}

	public class RunEvent implements Runnable{
		final EventPair eventPair;
		final EventScheduler scheduler;
		public RunEvent(EventScheduler scheduler, EventPair eventPair) {
			this.eventPair = eventPair;
			this.scheduler = scheduler;
		}
		@Override
		public void run() {
			if (stop)
				return;

			EventExecutor ee = EventController.getEventExecutor(eventPair.getEventParams().getName());
			if (ee == null){
				Log.err("executor for " + eventPair.getEventParams() +" was not found");
				return;
			}
			CommandSender sender = Bukkit.getConsoleSender();
			EventParams eventParams = eventPair.getEventParams();
			String args[] = eventPair.getArgs();
			Event event = null;
			try {
				if (ee instanceof ReservedArenaEventExecutor){
					ReservedArenaEventExecutor exe = (ReservedArenaEventExecutor) ee;
					event = exe.openIt(eventParams, args);
				} else if (ee instanceof TournamentExecutor){
					TournamentExecutor exe = (TournamentExecutor) ee;
					event = exe.openIt(sender, eventParams, args);
				}
			} catch (InvalidEventException e) {
				/** do nothing */
			} catch (InvalidOptionException e) {
				/** do nothing */
			} catch (Exception e){
				e.printStackTrace();
			}
			if (Defaults.DEBUG_SCHEDULER) Log.info("[BattleArena debugging] Running event ee=" + ee  +"  event" + event +"  args=" + args);
			if (event != null){
				event.addArenaListener(scheduler);
			} else {  /// wait then start up the scheduler again in x seconds
				currentTimer = Bukkit.getScheduler().scheduleAsyncDelayedTask(BattleArena.getSelf(),
						scheduler, 20L*Defaults.TIME_BETWEEN_SCHEDULED_EVENTS);
			}
		}
	}

	@MatchEventHandler
	public void onEventFinished(EventFinishedEvent event){
		Event e = event.getEvent();
		e.removeArenaListener(this);
		if (continuous){
			if (Defaults.DEBUG_SCHEDULER) Log.info("[BattleArena debugging] finished event "+ e+"  scheduling next event in "+ 20L*Defaults.TIME_BETWEEN_SCHEDULED_EVENTS + " ticks");

			/// Wait x sec then start the next event
			Bukkit.getScheduler().scheduleAsyncDelayedTask(BattleArena.getSelf(), this, 20L*Defaults.TIME_BETWEEN_SCHEDULED_EVENTS);
			if (Defaults.SCHEDULER_ANNOUNCE_TIMETILLNEXT){
				Bukkit.getServer().broadcastMessage(
						MessageUtil.colorChat(
						ChatColor.YELLOW+"Next event will start in "+
						TimeUtil.convertSecondsToString(Defaults.TIME_BETWEEN_SCHEDULED_EVENTS)));}
		} else {
			running = false;
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void stop() {
		stop = true;
		running = false;
		continuous = false;
	}

	public List<EventPair> getEvents() {
		return events;
	}

	public void start() {
		continuous = true;
		stop = false;
		new Thread(this).start();
	}

	public void startNext() {
		continuous = false;
		if (currentTimer != null)
			Bukkit.getScheduler().cancelTask(currentTimer);
		stop = false;
		new Thread(this).start();
	}


	public boolean scheduleEvent(EventParams eventParams, String[] args) {
		events.add(new EventPair(eventParams,args)); /// TODO verify these arguments here instead of waiting until running them
		return true;
	}

	public EventPair deleteEvent(int i) {
		return events.remove(i);
	}

}
