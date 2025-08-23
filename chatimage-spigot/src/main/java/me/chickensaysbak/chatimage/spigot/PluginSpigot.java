// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.spigot;

import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.adapters.CommandAdapter;
import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.adapters.YamlAdapter;
import me.chickensaysbak.chatimage.core.softdepend.DiscordSRVHandler;
import me.chickensaysbak.chatimage.core.softdepend.EssXDiscordHandler;
import me.chickensaysbak.chatimage.spigot.softdepend.PAPIHandler;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class PluginSpigot extends JavaPlugin implements Listener, PluginAdapter {

    private ChatImage core;
    private Metrics bStats;
    private DiscordSRVHandler discordSRVHandler = null;
    private EssXDiscordHandler essXDiscordHandler = null;
    private PAPIHandler papiHandler = null;

    @Override
    public void onEnable() {

        bStats = new Metrics(this, 12672);
        core = new ChatImage(this);
        boolean debug = core.getSettings().isDebug();

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(this, this);

        if (pluginManager.isPluginEnabled("DiscordSRV")) {
            discordSRVHandler = new DiscordSRVHandler();
            if (debug) getLogger().info("ChatImage Debugger - DiscordSRV found");
        }

        if (pluginManager.isPluginEnabled("EssentialsDiscord")) {
            essXDiscordHandler = new EssXDiscordHandler();
            pluginManager.registerEvents(essXDiscordHandler, this);
            if (debug) getLogger().info("ChatImage Debugger - EssentialsDiscord found");
        }

        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            papiHandler = new PAPIHandler();
            if (debug) getLogger().info("ChatImage Debugger - PlaceholderAPI found");
        }

        publishStat("discordsrv", String.valueOf(discordSRVHandler != null));
        publishStat("essxdiscord", String.valueOf(essXDiscordHandler != null));
        publishStat("papi", String.valueOf(papiHandler != null));

    }

    @Override
    public void onDisable() {
        if (discordSRVHandler != null) discordSRVHandler.unsubscribe();
        core.onDisable();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        core.onJoin(new PlayerSpigot(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        boolean cancelEvent = core.onChat(new PlayerSpigot(event.getPlayer()), event.getMessage());
        if (cancelEvent) event.setCancelled(true);
    }

    @Override
    public void registerCommand(CommandAdapter command) {
        getCommand(command.getName()).setExecutor(new CommandSpigot(command));
    }

    @Override
    public void saveResource(String path) {
        File target = new File(getDataFolder(), path);
        if (!target.exists()) saveResource(path, false);
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
    public void runAsyncTaskLater(Runnable task, int ticks) {
        getServer().getScheduler().runTaskLaterAsynchronously(this, task, ticks).getTaskId();
    }

    @Override
    public void runTaskLater(Runnable task, int ticks) {
        getServer().getScheduler().runTaskLater(this, task, ticks).getTaskId();
    }

    @Override
    public void publishStat(String id, String value) {
        bStats.addCustomChart(new SimplePie(id, () -> value));
    }

    @Override
    public void publishStat(String id, int value) {
        bStats.addCustomChart(new SingleLineChart(id, () -> value));
    }

    @Override
    public void publishStat(String id, Map<String, Map<String, Integer>> value) {
        bStats.addCustomChart(new DrilldownPie(id, () -> value));
    }

    @Override
    public String setPlaceholders(UUID uuid, String text) {
        if (papiHandler != null) text = papiHandler.setPlaceholders(getServer().getPlayer(uuid), text);
        return text;
    }

}
