// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.plugin.spigot;

import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.DiscordSRVHandler;
import me.chickensaysbak.chatimage.core.adapters.CommandAdapter;
import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.adapters.YamlAdapter;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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
    private DiscordSRVHandler discordSRVHandler = null;

    @Override
    public void onEnable() {

        bStats = new Metrics(this, 12672);
        core = new ChatImage(this);
        getServer().getPluginManager().registerEvents(this, this);

        if (getServer().getPluginManager().isPluginEnabled("DiscordSRV")) {
            discordSRVHandler = new DiscordSRVHandler();
            if (core.getSettings().isDebug()) getLogger().info("ChatImage Debugger - DiscordSRV found");
        }

        publishStat("discordsrv", String.valueOf(discordSRVHandler != null));

    }

    @Override
    public void onDisable() {
        if (discordSRVHandler != null) discordSRVHandler.unsubscribe();
        core.onDisable();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        boolean cancelEvent = core.onChat(new PlayerSpigot(event.getPlayer()), event.getMessage());
        if (cancelEvent) event.setCancelled(true);
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

        Player player = getServer().getPlayer(uuid);
        if (player == null) return null;

        return new PlayerSpigot(player);

    }

    @Override
    public PlayerAdapter getPlayer(String name) {

        Player player = getServer().getPlayer(name);
        if (player == null) return null;

        return new PlayerSpigot(player);

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
    public int runAsyncTaskLater(Runnable task, int ticks) {
        return getServer().getScheduler().runTaskLaterAsynchronously(this, task, ticks).getTaskId();
    }

    @Override
    public boolean isBungee() {
        return false;
    }

    @Override
    public void publishStat(String id, String value) {
        bStats.addCustomChart(new SimplePie(id, () -> value));
    }

    @Override
    public void publishStat(String id, int value) {
        bStats.addCustomChart(new SingleLineChart(id, () -> value));
    }

}
