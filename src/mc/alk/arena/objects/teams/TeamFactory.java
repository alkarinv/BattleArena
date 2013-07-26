package mc.alk.arena.objects.teams;

import java.lang.reflect.Constructor;
import java.util.Set;

import mc.alk.arena.objects.ArenaPlayer;
import mc.alk.arena.objects.MatchParams;

public class TeamFactory {

	public static ArenaTeam createTeam(MatchParams params, ArenaPlayer p){
		return new CompositeTeam(p);
	}

	//	public static Team createTeam(Set<ArenaPlayer> players){
	//		return new CompositeTeam(players);
	//	}

	public static CompositeTeam createCompositeTeam(MatchParams params, Set<ArenaPlayer> players) {
		CompositeTeam ct =  new CompositeTeam(players);
		ct.setCurrentParams(params);
		return ct;
	}

	public static CompositeTeam createCompositeTeam(MatchParams params) {
		CompositeTeam ct =  new CompositeTeam();
		ct.setCurrentParams(params);
		return ct;
	}

	public static CompositeTeam createCompositeTeam() {
		return new CompositeTeam();
	}

	//	public static CompositeTeam createCompositeTeam(ArenaTeam team) {
	//		return new CompositeTeam(team);
	//	}

	public static ArenaTeam createTeam(Class<? extends ArenaTeam> clazz) {
		Class<?>[] args = {};
		try {
			Constructor<?> constructor = clazz.getConstructor(args);
			return (ArenaTeam) constructor.newInstance((Object[])args);
		} catch (NoSuchMethodException e){
			System.err.println("If you have custom constructors for your Team you must also have a public default constructor");
			System.err.println("Add the following line to your Team Class '" + clazz.getSimpleName()+".java'");
			System.err.println("public " + clazz.getSimpleName()+"(){}");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
