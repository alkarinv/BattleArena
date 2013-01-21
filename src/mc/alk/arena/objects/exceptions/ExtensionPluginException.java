package mc.alk.arena.objects.exceptions;

import org.bukkit.plugin.Plugin;


public class ExtensionPluginException extends Exception {
	private static final long serialVersionUID = 1L;

	public ExtensionPluginException(Plugin plugin, String string) {
		super(plugin == null ? "[Extension Error]" : "[" + plugin.getName() +" Error] " + string);
	}
}
