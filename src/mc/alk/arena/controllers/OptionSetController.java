package mc.alk.arena.controllers;

import java.util.Map;

import mc.alk.arena.objects.options.TransitionOptions;
import mc.alk.arena.util.CaseInsensitiveMap;

public class OptionSetController {
	static final Map<String,TransitionOptions> options = new CaseInsensitiveMap<TransitionOptions>();

	public static void addOptionSet(String key, TransitionOptions to) {
		options.put(key, to);
	}

	public static TransitionOptions getOptionSet(String key) {
		return options.get(key);
	}
	public static Map<String,TransitionOptions> getOptionSets() {
		return options;
	}

}
