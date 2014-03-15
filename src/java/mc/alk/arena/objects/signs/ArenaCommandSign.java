package mc.alk.arena.objects.signs;

import mc.alk.arena.BattleArena;
import mc.alk.arena.Defaults;
import mc.alk.arena.controllers.EventController;
import mc.alk.arena.executors.BAExecutor;
import mc.alk.arena.executors.EventExecutor;
import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.EventParams;
import mc.alk.arena.objects.JoinType;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.util.SerializerUtil;
import mc.alk.arena.util.SignUtil;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class ArenaCommandSign implements ConfigurationSerializable{
	public static enum ARENA_COMMAND{
		JOIN, LEAVE, START
	}
	final Location location;
	final MatchParams mp;
    final ARENA_COMMAND command;
    final String options1;
    final String options2;

	public ArenaCommandSign(Location location, MatchParams mp, ARENA_COMMAND cmd, String op1, String op2) {
		this.mp = mp;
		this.command = cmd;
		this.options1 = op1;
		this.options2 = op2;
		this.location = location;
	}

	public void performAction(ArenaPlayer player) {
		if (mp.getJoinType() == JoinType.JOINPHASE){
			performEventAction(player);
		} else {
			performMatchAction(player);
		}
	}

	private void performMatchAction(ArenaPlayer player) {
		BAExecutor executor = BattleArena.getBAExecutor();
		switch (command){
		case JOIN:
			executor.join(player,
                    mp,
                    new String[]{"add", options1},
                    !Defaults.USE_SIGN_PERMS);
			break;
		case LEAVE:
			executor.leave(player,mp, !Defaults.USE_SIGN_PERMS);
			break;
		case START:
			break;
		}
	}

	private void performEventAction(ArenaPlayer player) {
		EventParams ep = (EventParams)mp;
		EventExecutor executor = EventController.getEventExecutor(ep.getType().getName());
		switch (command){
		case JOIN:
			executor.eventJoin(player,
                    ep,
                    new String[]{"add", options1},
                    !Defaults.USE_SIGN_PERMS);
			break;
		case LEAVE:
			executor.leave(player,ep,!Defaults.USE_SIGN_PERMS);
			break;
		case START:
			break;
		}
	}

	public MatchParams getMatchParams() {
		return mp;
	}

	public ARENA_COMMAND getCommand() {
		return command;
	}
	public String getOption1(){
		return options1;
	}

	@Override
	public Map<String, Object> serialize() {
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("location", SerializerUtil.getBlockLocString(location));
		return map;
	}

	public static ArenaCommandSign deserialize(Map<String, Object> map) throws IllegalArgumentException{
		Location location = SerializerUtil.getLocation((String) map.get("location"));
		if (location == null)
			return null;
		Sign s = SignUtil.getSign(location);
		if (s == null)
			return null;
        return SignUtil.getArenaCommandSign(s, s.getLines());
	}

	public Location getLocation() {
		return location;
	}

	public Sign getSign() {
		return SignUtil.getSign(location);
	}

}
