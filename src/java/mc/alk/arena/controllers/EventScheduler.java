package mc.alk.arena.controllers;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.competition.events.Event;
import mc.alk.arena.events.events.EventFinishedEvent;
import mc.alk.arena.events.matches.MatchFinishedEvent;
import mc.alk.arena.executors.EventExecutor;
import mc.alk.arena.executors.TournamentExecutor;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.exceptions.InvalidEventException;
import mc.alk.arena.objects.options.EventOpenOptions;
import mc.alk.arena.objects.pairs.EventPair;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

			CommandSender sender = Bukkit.getConsoleSender();
			MatchParams params = eventPair.getEventParams();
			String args[] = eventPair.getArgs();
			boolean success = false;
			try {
				EventExecutor ee = EventController.getEventExecutor(eventPair.getEventParams().getName());
				if (ee != null && ee instanceof TournamentExecutor){
					TournamentExecutor exe = (TournamentExecutor) ee;
					Event event = exe.openIt(sender, (EventParams)params, args);
					if (event != null){
						event.addArenaListener(scheduler);
						success = true;
					}
					if (Defaults.DEBUG_SCHEDULER) Log.info("[BattleArena debugging] Running event ee=" + ee  +
                            "  event" + event +"  args=" + Arrays.toString(args));
				} else { /// normal match
					EventOpenOptions eoo = EventOpenOptions.parseOptions(args, null, params);
					Arena arena = eoo.getArena(params, null);

					BattleArena.getBAController().createAndAutoMatch(arena, eoo);
					arena.addArenaListener(scheduler);
					success = true;
				}
			} catch (InvalidEventException e) {
				/** do nothing */
			} catch (Exception e){
				Log.printStackTrace(e);
			}

			if (!success){ /// wait then start up the scheduler again in x seconds
				currentTimer = Scheduler.scheduleAsynchronousTask(scheduler, 20L * Defaults.TIME_BETWEEN_SCHEDULED_EVENTS);
			}
		}
	}

	@ArenaEventHandler
	public void onEventFinished(EventFinishedEvent event){
		Event e = event.getEvent();
		e.removeArenaListener(this);
		if (continuous){
			if (Defaults.DEBUG_SCHEDULER) Log.info("[BattleArena debugging] finished event "+ e+
                    "  scheduling next event in "+ 20L*Defaults.TIME_BETWEEN_SCHEDULED_EVENTS + " ticks");

			/// Wait x sec then start the next event
			Scheduler.scheduleAsynchronousTask(this, 20L*Defaults.TIME_BETWEEN_SCHEDULED_EVENTS);
			if (Defaults.SCHEDULER_ANNOUNCE_TIMETILLNEXT){
                MessageUtil.broadcastMessage(
						MessageUtil.colorChat(
								ChatColor.YELLOW+"Next event will start in "+
										TimeUtil.convertSecondsToString(Defaults.TIME_BETWEEN_SCHEDULED_EVENTS)));}
		} else {
			running = false;
		}
	}

	@ArenaEventHandler
	public void onMatchFinished(MatchFinishedEvent event){
		if (continuous){
			if (Defaults.DEBUG_SCHEDULER) Log.info("[BattleArena debugging] finished event "+ event.getEventName()+"  scheduling next event in "+ 20L*Defaults.TIME_BETWEEN_SCHEDULED_EVENTS + " ticks");

			/// Wait x sec then start the next event
            Scheduler.scheduleAsynchronousTask(this, 20L*Defaults.TIME_BETWEEN_SCHEDULED_EVENTS);
			if (Defaults.SCHEDULER_ANNOUNCE_TIMETILLNEXT){
                MessageUtil.broadcastMessage(
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


	public boolean scheduleEvent(MatchParams eventParams, String[] args) {
		events.add(new EventPair(eventParams,args));
		return true;
	}

	public EventPair deleteEvent(int i) {
		return events.remove(i);
	}

}
