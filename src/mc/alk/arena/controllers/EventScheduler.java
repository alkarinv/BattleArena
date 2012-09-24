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
import mc.alk.arena.listeners.TransitionListener;
import mc.alk.arena.objects.EventPair;
import mc.alk.arena.objects.events.TransitionEventHandler;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.MessageUtil;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.command.ColouredConsoleSender;

public class EventScheduler implements Runnable, TransitionListener{

	int curEvent = 0;
	Long delay = 5L;
	boolean continuous= false;
	boolean running = false;
	boolean stop = false;

	CopyOnWriteArrayList<EventPair> events = new CopyOnWriteArrayList<EventPair>();

	public boolean scheduleEvent(Event event, String[] args) {
		events.add(new EventPair(event,args)); /// TODO verify these arguments here instead of waiting until running them
		return true;
	}
	
	@Override
	public void run() {
		if (events.isEmpty() || stop)
			return;
		running = true;
		int index = curEvent % events.size();
		curEvent++;
		Bukkit.getScheduler().scheduleSyncDelayedTask(BattleArena.getSelf(), new RunEvent(this, events.get(index)));
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
			EventExecutor ee = EventController.getEventExecutor(eventPair.getEvent());
			if (ee == null){
				Log.err("executor for " + eventPair.getEvent() +" was not found");
				return;
			}
			CommandSender sender = ColouredConsoleSender.getInstance();
			Event event = eventPair.getEvent();
			event.addTransitionListener(scheduler);
			String args[] = eventPair.getArgs();
			boolean success = false;
			if (ee instanceof ReservedArenaEventExecutor){
				ReservedArenaEventExecutor exe = (ReservedArenaEventExecutor) ee;
				success = exe.openIt(sender, args);
			} else if (ee instanceof TournamentExecutor){
				TournamentExecutor exe = (TournamentExecutor) ee;
				success = exe.openIt(sender, args);
			}
			
			if (!success){ /// wait then start up the scheduler again in 30 seconds
				MessageUtil.sendMessage(sender, "Event could not be started, continuing in 30 seconds");
				MessageUtil.sendMessage(sender, "Event was " + event.getName()+"  args: " + StringUtils.join(args,","));
				Bukkit.getScheduler().scheduleAsyncDelayedTask(BattleArena.getSelf(), scheduler, 20L*30);
			} 
			/// otherwise we wait for event is finished message
		}
	}
	
	@TransitionEventHandler
	public void onEventFinished(EventFinishedEvent event){
		Event e = event.getEvent();
		e.removeTransitionListener(this);
		if (continuous){
			/// Wait 30 sec then start the next event
			Bukkit.getScheduler().scheduleAsyncDelayedTask(BattleArena.getSelf(), this, (long) (20L*30*Defaults.TICK_MULT));
			Bukkit.getServer().broadcastMessage(ChatColor.GOLD+"Next event will start in 30 seconds");			
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
		if (running)
			return;
		stop = false;
		new Thread(this).start();			
	}

	public void deleteEvent(int i) {
		events.remove(i);
	}

}
