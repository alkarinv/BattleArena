package mc.alk.arena.objects;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

import mc.alk.arena.controllers.MethodController;
import mc.alk.arena.objects.arenas.Arena;
import mc.alk.arena.util.CaseInsensitiveMap;

import org.bukkit.plugin.Plugin;

public class ArenaType implements Comparable<ArenaType>{
	static public CaseInsensitiveMap<Class<?>> classes = new CaseInsensitiveMap<Class<?>>();
	static public CaseInsensitiveMap<ArenaType> types = new CaseInsensitiveMap<ArenaType>();

	public static ArenaType ANY = null;
	public static ArenaType VERSUS = null;
	static int count = 0;

	final String name;
	final Plugin ownerPlugin;
	final int id = count++;
	Set<ArenaType> compatibleTypes = null;

	private ArenaType(final String name,Plugin plugin){
		this.name = name;
		this.ownerPlugin = plugin;
		if (!types.containsKey(name))
			types.put(name,this);

		if (name.equalsIgnoreCase("ANY")) ANY = this;
		else if (name.equalsIgnoreCase("VERSUS")) { VERSUS = this;}
	}

	public static ArenaType fromString(final String arenatype) {
		if (arenatype==null)
			return null;
		return types.get(arenatype.toUpperCase());
	}

	public String toString(){
		return name;
	}

	public boolean matches(ArenaType arenaType) {
		if (this == ANY || arenaType == ANY) return true;
		if (this == arenaType)
			return true;
		return (compatibleTypes==null) ? false : compatibleTypes.contains(arenaType); 
	}

	public String toPrettyString(int teamSize) {
		if (this == ArenaType.VERSUS){
			return teamSize +"v" + teamSize;
		} else {
			return toString();
		}
	}

	public String getCompatibleTypes(){
		if (compatibleTypes == null || compatibleTypes.isEmpty())
			return name;
		StringBuilder sb = new StringBuilder(name);
		for (ArenaType at: compatibleTypes){
			sb.append(", " +at.name);}
		return sb.toString();
	}

	public static String getValidList() {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (ArenaType at: types.values()){
			if (!first) sb.append(", ");
			first = false;
			sb.append(at.name);
		}
		return sb.toString();
	}

	public static ArenaType register(String arenaType, Class<? extends Arena> c, Plugin plugin) {
		final String uarenaType = arenaType.toUpperCase();
		if (!classes.containsKey(uarenaType))
			classes.put(uarenaType, c);
		if (!types.containsKey(uarenaType)){
			new ArenaType(arenaType,plugin);
		}
		MethodController.addMethods(c,c.getMethods());
		return types.get(uarenaType);
	}


	public int ordinal() {
		return id;
	}

	public static Arena createArena(Arena arena) {
		ArenaType arenaType = arena.getParameters().getType();
		Class<?> arenaClass = classes.get(arenaType.name);
		if (arenaClass == null)
			return null;
		Class<?>[] args = {Arena.class};
		try {
			Constructor<?> constructor = arenaClass.getConstructor(args);
			Arena newArena = (Arena) constructor.newInstance(arena);
			return newArena;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Arena createArena(String arenaName, ArenaParams q) {
		ArenaType arenaType = q.getType();
		Class<?> arenaClass = classes.get(arenaType.name);
		if (arenaClass == null)
			return null;
		Class<?>[] args = {String.class,ArenaParams.class};
		try {
			Constructor<?> constructor = arenaClass.getConstructor(args);
			Arena newArena = (Arena) constructor.newInstance(arenaName, q);
			return newArena;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public int compareTo(ArenaType arg0) {
		return this.name.compareTo(arg0.name);
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj) 
			return true;
		if((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		return this.name.equals( ((ArenaType)obj).name);
	}
	@Override
	public int hashCode(){
		return name.hashCode();
	}

	public Plugin getPlugin() {
		return ownerPlugin;
	}

	public static void addCompatibleTypes(String type1, String type2) {
		ArenaType at1 = fromString(type1);
		ArenaType at2 = fromString(type2);
		if (at1 == null || at2==null)
			return;
		at1.addCompatibleType(at2);
		at2.addCompatibleType(at1);
	}

	private void addCompatibleType(ArenaType at) {
		if (compatibleTypes == null){
			compatibleTypes = new HashSet<ArenaType>();
		}
		compatibleTypes.add(at);
	}

}
