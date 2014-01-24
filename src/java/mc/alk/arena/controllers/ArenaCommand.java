package mc.alk.arena.controllers;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

public class ArenaCommand extends Command implements PluginIdentifiableCommand{
	final CommandExecutor executor;
	final Plugin plugin;

	public ArenaCommand(String name, String description, String usageMessage, List<String> aliases, Plugin plugin, CommandExecutor executor) {
		super(name, description, usageMessage, aliases);
		this.plugin = plugin;
		this.executor = executor;
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		return executor.onCommand(sender, this, commandLabel, args);
	}

	@Override
	public Plugin getPlugin() {
		return plugin;
	}
}
