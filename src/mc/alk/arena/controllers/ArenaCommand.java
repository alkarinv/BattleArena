package mc.alk.arena.controllers;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

public class ArenaCommand extends Command implements PluginIdentifiableCommand{
	CommandExecutor executor;
	Plugin plugin;

	public ArenaCommand(String name, String description, String usageMessage, List<String> aliases, Plugin plugin) {
		super(name, description, usageMessage, aliases);
		this.plugin = plugin;
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		return executor.onCommand(sender, this, commandLabel, args);
	}

	@Override
	public Plugin getPlugin() {
		return plugin;
	}

	public void setExecutor(CommandExecutor executor){
		this.executor = executor;
	}
}
