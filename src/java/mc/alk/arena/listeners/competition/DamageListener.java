package mc.alk.arena.listeners.competition;

import mc.alk.arena.BattleArena;
import mc.alk.arena.listeners.PlayerHolder;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.StateGraph;
import mc.alk.arena.objects.PVPState;
import mc.alk.arena.objects.arenas.ArenaListener;
import mc.alk.arena.objects.events.ArenaEventHandler;
import mc.alk.arena.objects.events.EventPriority;
import mc.alk.arena.objects.options.StateOptions;
import mc.alk.arena.objects.teams.ArenaTeam;
import mc.alk.arena.util.DmgDeathUtil;
import mc.alk.arena.util.Log;
import mc.alk.arena.util.Util;
import mc.alk.arena.util.compat.IEventHelper;
import mc.alk.plugin.updater.Version;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements ArenaListener{
	StateGraph transitionOptions;
	PlayerHolder holder;
    static IEventHelper handler;

    static {
        Class<?>[] args = {};
        try {
            Version version = Util.getCraftBukkitVersion();
            if (version.compareTo("v1_6_1") >= 0){
                final Class<?> clazz = Class.forName("mc.alk.arena.util.compat.v1_6_1.EventHelper");
                handler = (IEventHelper) clazz.getConstructor(args).newInstance((Object[])args);
            } else {
                final Class<?> clazz = Class.forName("mc.alk.arena.util.compat.pre.EventHelper");
                handler = (IEventHelper) clazz.getConstructor(args).newInstance((Object[])args);
            }
        } catch (Exception e) {
            Log.printStackTrace(e);
        }
    }

    public DamageListener(PlayerHolder holder){
		this.transitionOptions = holder.getParams().getStateGraph();
		this.holder = holder;
	}

	@ArenaEventHandler(suppressCastWarnings=true,priority=EventPriority.LOW)
	public void onEntityDamageEvent(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		final StateOptions to = transitionOptions.getOptions(holder.getMatchState());
		if (to == null)
			return;
		final PVPState pvp = to.getPVP();
		if (pvp == null)
			return;
		final ArenaPlayer target = BattleArena.toArenaPlayer((Player) event.getEntity());
		if (pvp == PVPState.INVINCIBLE){
			/// all damage is cancelled
			target.setFireTicks(0);
            handler.setDamage(event,0);
			event.setCancelled(true);
			return;
		}
		if (!(event instanceof EntityDamageByEntityEvent)){
			return;}

		final Entity damagerEntity = ((EntityDamageByEntityEvent)event).getDamager();
		ArenaPlayer damager;
		switch(pvp){
		case ON:
			ArenaTeam targetTeam = holder.getTeam(target);
			if (targetTeam == null || !targetTeam.hasAliveMember(target)) /// We dont care about dead players
				return;
			damager = DmgDeathUtil.getPlayerCause(damagerEntity);
			if (damager == null){ /// damage from some source, its not pvp though. so we dont care
				return;}
			ArenaTeam t = holder.getTeam(damager);
			if (t != null && t.hasMember(target)){ /// attacker is on the same team
				event.setCancelled(true);
			} else {/// different teams... lets make sure they can actually hit
				event.setCancelled(false);
			}
			break;
		case OFF:
			damager = DmgDeathUtil.getPlayerCause(damagerEntity);
			if (damager != null){ /// damage done from a player
                handler.setDamage(event,0);
				event.setCancelled(true);
			}
			break;
		default:
			break;
		}
	}
}
