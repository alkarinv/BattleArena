package mc.alk.arena.listeners.competition;

import java.util.Collection;

import mc.alk.arena.controllers.SignController;
import mc.alk.arena.events.teams.TeamJoinedQueueEvent;
import mc.alk.arena.objects.ArenaParams;
import mc.alk.arena.objects.signs.ArenaStatusSign;
import mc.alk.arena.util.TimeUtil;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MatchListener implements Listener{
	SignController signController;

	public MatchListener(SignController signController){
		this.signController = signController;
	}

	@EventHandler
	public void onTeamJoinedQueueEvent(TeamJoinedQueueEvent event){
		ArenaParams params = event.getParams();
		if (params == null)
			return;
		Collection<ArenaStatusSign> signs = signController.getSigns(params.getType().getName());
		if (signs == null)
			return;

		for (ArenaStatusSign sign: signs){
			Location l = sign.getLocation();
			final Material type = l.getBlock().getState().getType();
			if (type != Material.SIGN_POST && type != Material.SIGN){
				continue;}
			Sign s = (Sign) l.getBlock().getState();
			if (event.getTimeToStart() != null)
				s.setLine(1, TimeUtil.convertSecondsToString(event.getTimeToStart() ));
			s.setLine(2, event.getPos() +"/" + params.getMinTeams());
//			s.setLine(3, "In Progress:"+);
			s.update();
		}
	}
}
