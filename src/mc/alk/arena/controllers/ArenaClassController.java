package mc.alk.arena.controllers;

import java.util.HashMap;

import mc.alk.arena.objects.ArenaClass;

public class ArenaClassController {
	static HashMap<String,ArenaClass> classes = new HashMap<String,ArenaClass>();
	public static void addClass(ArenaClass ac){
		classes.put(ac.getName(), ac);
	}
	
	public static ArenaClass getClass(String name){
		return classes.get(name);
	}
}
