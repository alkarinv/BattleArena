package mc.alk.arena.objects.signs;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.objects.exceptions.InvalidOptionException;
import mc.alk.arena.objects.options.JoinOptions;
import mc.alk.arena.util.MessageUtil;
import mc.alk.arena.util.SerializerUtil;
import mc.alk.arena.util.SignUtil;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class ArenaCommandSign implements ConfigurationSerializable{

	final Location location;
	final MatchParams mp;
    final String[] options1;
    final String[] options2;
    Arena arena;

    ArenaCommandSign(Location location, MatchParams mp, String[] op1, String[] op2) {
		this.mp = mp;
        this.options1 = Arrays.copyOf(op1, op1.length);
		this.options2 = Arrays.copyOf(op2, op2.length);
		this.location = location;
        try {
            JoinOptions joinOptions = JoinOptions.parseOptions(mp, null, op1);
            arena = joinOptions.getArena();
        } catch (Exception e) {
            /* do nothing */
        }
    }

	public abstract void performAction(ArenaPlayer player);

	public MatchParams getMatchParams() {
		return mp;
	}

    public String[] getOption1(){
        return options1;
    }

    public String[] getOption2(){
        return options2;
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

    public static ArenaCommandSign create(Location location, MatchParams mp, String[] lines) throws InvalidOptionException {
        String type = MessageUtil.decolorChat(lines[1]).toUpperCase().trim();
        String[] ops1 = lines.length > 2 ? lines[2].split(" ") : null;
        String[] ops2 = lines.length > 3 ? lines[3].split(" ") : null;
        if (ops1 != null){
            for (int i=0;i<ops1.length;i++){
                ops1[i] = MessageUtil.decolorChat(ops1[i]).toUpperCase().trim();}
        }
        if (ops2 != null){
            for (int i=0;i<ops2.length;i++){
                ops2[i] = MessageUtil.decolorChat(ops2[i]).toUpperCase().trim();}
        }
        if (type.equalsIgnoreCase("join")) {
            return new ArenaJoinSign(location, mp, ops1, ops2);
        } else if (type.equalsIgnoreCase("leave")){
            return new ArenaLeaveSign(location, mp, ops1, ops2);
        } else {
            throw new InvalidOptionException(type + " is not a known sign type");
        }
    }

    public abstract String getCommand();

    public Arena getArena(){
        return arena;
    }
}
