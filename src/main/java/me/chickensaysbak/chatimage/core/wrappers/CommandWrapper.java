package me.chickensaysbak.chatimage.core.wrappers;

import java.util.List;
import java.util.UUID;

public abstract class CommandWrapper {

    private PluginWrapper plugin;

    public CommandWrapper(PluginWrapper plugin) {
        this.plugin = plugin;
    }

    public PluginWrapper getPlugin() {
        return plugin;
    }

    public abstract String getName();
    public abstract String getPermission();
    public abstract void onCommand(UUID sender, String[] args);
    public abstract List<String> onTabComplete(UUID sender, String[] args);

}