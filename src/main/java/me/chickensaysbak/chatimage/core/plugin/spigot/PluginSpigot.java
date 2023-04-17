// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.plugin.spigot;

import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.adapters.CommandAdapter;
import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.adapters.YamlAdapter;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PluginSpigot extends JavaPlugin implements Listener, PluginAdapter {

    private ChatImage core;
    private Metrics bStats;

    @Override
    public void onEnable() {
        core = new ChatImage(this);
        bStats = new Metrics(this, 12672);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        core.onDisable();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        boolean cancelEvent = core.onChat(event.getPlayer().getUniqueId(), event.getMessage());
        event.setCancelled(cancelEvent);
    }

    @Override
    public void registerCommand(CommandAdapter command) {
        getCommand(command.getName()).setExecutor(new CommandSpigot(command));
    }

    @Override
    public void sendConsoleMessage(String message) {
        getServer().getConsoleSender().sendMessage(message);
    }

    @Override
    public PlayerAdapter getPlayer(UUID uuid) {
        return new PlayerSpigot(getServer().getPlayer(uuid));
    }

    @Override
    public PlayerAdapter getPlayer(String name) {
        return new PlayerSpigot(getServer().getPlayer(name));
    }

    @Override
    public List<PlayerAdapter> getOnlinePlayers() {
        return getServer().getOnlinePlayers().stream().map(PlayerSpigot::new).collect(Collectors.toList());
    }

    @Override
    public YamlAdapter loadYaml(File file) {
        return new YamlSpigot(YamlConfiguration.loadConfiguration(file));
    }

    @Override
    public int runTaskLater(Runnable task, int ticks) {
        return getServer().getScheduler().runTaskLater(this, task, ticks).getTaskId();
    }

    @Override
    public int runTaskAsync(Runnable task) {
        return getServer().getScheduler().runTaskAsynchronously(this, task).getTaskId();
    }

    @Override
    public boolean isBungee() {
        return false;
    }

    @Override
    public void publishStat(String id, String value) {
        bStats.addCustomChart(new SimplePie(id, () -> value));
    }

}
