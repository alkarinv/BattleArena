package mc.alk.arena.objects.signs;

import mc.alk.arena.BattleArena;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import org.bukkit.Location;

/**
 * @author alkarin
 */
class ArenaLeaveSign extends ArenaCommandSign{

    ArenaLeaveSign(Location location, MatchParams mp, String[] op1, String[] op2) throws InvalidOptionException {
        super(location, mp, op1, op2);
    }

    @Override
    public void performAction(ArenaPlayer player) {
        BattleArena.getBAExecutor().leave(player, mp);
    }


    @Override
    public String getCommand() {
        return "Leave";
    }
}
