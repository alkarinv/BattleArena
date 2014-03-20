package mc.alk.arena.controllers;

import mc.alk.arena.objects.options.StateOptions;
import mc.alk.arena.util.CaseInsensitiveMap;

import java.util.Map;
import java.util.TreeMap;

public class OptionSetController {
	static final Map<String,StateOptions> options = new CaseInsensitiveMap<StateOptions>();

	public static void addOptionSet(String key, StateOptions to) {
		options.put(key, to);
	}

	public static StateOptions getOptionSet(String key) {
		return options.get(key);
	}

	public static Map<String,StateOptions> getOptionSets() {
        return new TreeMap<String,StateOptions>(options);
	}

}
