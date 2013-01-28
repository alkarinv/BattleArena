package mc.alk.arena.executors;

import java.util.Arrays;
import java.util.List;

import mc.alk.arena.controllers.EventScheduler;
import mc.alk.arena.controllers.ParamController;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.pairs.EventPair;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

public class BattleArenaSchedulerExecutor extends CustomCommandExecutor{
	EventScheduler es;
	public BattleArenaSchedulerExecutor(EventScheduler es){
		this.es = es;
	}

	@MCCommand(cmds={"add"}, admin=true)
	public boolean schedule(CommandSender sender, String eventType, String[] args) {
		EventParams ep = ParamController.getEventParamCopy(eventType);
		if (ep == null){
			return sendMessage(sender, "&cEvent type " + eventType+ " not found!");
		}
		if (es.scheduleEvent(ep, Arrays.copyOfRange(args, 2, args.length))){
			sendMessage(sender, "&2Event scheduled!. &6/bas list&2 to see a list of scheduled events");
		} else {
			sendMessage(sender, "&cEvent not scheduled!. There was some error scheduling this events");
		}
		return true;
	}

	@MCCommand(cmds={"delete","del"}, admin=true)
	public boolean delete(CommandSender sender, Integer index) {
		List<EventPair> events = es.getEvents();
		if (events == null || events.isEmpty()){
			return sendMessage(sender, "&cNo &4BattleArena&c events have been scheduled");}

		if (events.size() < index || index <= 0){
			return sendMessage(sender, "&cIndex is out of range.  Valid Range: &61-"+events.size());}
		es.deleteEvent(index-1);
		return sendMessage(sender, "&2Event &6"+index+"&2 deleted");
	}

	@MCCommand(cmds={"list"}, admin=true)
	public boolean list(CommandSender sender) {
		List<EventPair> events = es.getEvents();
		if (events == null || events.isEmpty()){
			return sendMessage(sender, "&cNo &4BattleArena&c events have been scheduled");}
		for (int i=0;i<events.size();i++){
			EventPair ep = events.get(i);
			String[] args = ep.getArgs();
			String strargs = args == null ? "[]" : StringUtils.join(ep.getArgs(), " ");
			sendMessage(sender, "&2"+(i+1)+"&e:&6"+ep.getEventParams().getName() +"&e args: &6" + strargs);
		}
		return sendMessage(sender, "&6/bas delete <number>:&e to delete an event");
	}

	@MCCommand(cmds={"start"}, admin=true)
	public boolean start(CommandSender sender) {
		List<EventPair> events = es.getEvents();
		if (events == null || events.isEmpty()){
			return sendMessage(sender, "&cNo &4BattleArena&c events have been scheduled");}

		if (es.isRunning()){
			return sendMessage(sender, "&cScheduled events are already running!");
		} else {
			es.start();
		}
		return sendMessage(sender, "&2Scheduled events are now &astarted");
	}

	@MCCommand(cmds={"stop"}, admin=true)
	public boolean stop(CommandSender sender) {
		if (!es.isRunning()){
			return sendMessage(sender, "&cScheduled events are already stopped!");
		} else {
			es.stop();
		}
		return sendMessage(sender, "&2Scheduled events are now &4stopped!");
	}

	@MCCommand(cmds={"startNext"}, admin=true)
	public boolean startNext(CommandSender sender) {
		List<EventPair> events = es.getEvents();
		if (events == null || events.isEmpty()){
			return sendMessage(sender, "&cNo &4BattleArena&c events have been scheduled");}

		es.startNext();
		return sendMessage(sender, "&2Next Scheduled event is now starting");
	}
}