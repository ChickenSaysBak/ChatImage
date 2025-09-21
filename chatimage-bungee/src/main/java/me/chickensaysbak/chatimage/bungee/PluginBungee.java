// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.bungee;

import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.adapters.CommandAdapter;
import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.adapters.YamlAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.CustomClickEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;

public class PluginBungee extends Plugin implements Listener, PluginAdapter {

    private ChatImage core;
    private Metrics bStats;

    @Override
    public void onEnable() {
        bStats = new Metrics(this, 12674);
        core = new ChatImage(this);
        getProxy().getPluginManager().registerListener(this, this);
    }

    @Override
    public void onDisable() {
        core.onDisable();
    }

    @EventHandler
    public void onJoin(PostLoginEvent event) {
        core.onJoin(new PlayerBungee(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(ChatEvent event) {

        if (!event.isCancelled() && !event.isCommand() && event.getSender() instanceof ProxiedPlayer player) {
            boolean cancelEvent = core.onChat(new PlayerBungee(player), text(event.getMessage()));
            if (cancelEvent) event.setCancelled(true);
        }

    }

    @EventHandler
    public void onClick(CustomClickEvent event) {
        core.onClick(new PlayerBungee(event.getPlayer()), event.getId());
    }

    @Override
    public void registerCommand(CommandAdapter command) {
        getProxy().getPluginManager().registerCommand(this, new CommandBungee(command));
    }

    @Override
    public InputStream getResource(String name) {
        return getResourceAsStream(name);
    }

    // Adapted from the Spigot API.
    @Override
    public void saveResource(String resourcePath) {

        File dataFolder = getDataFolder(), file = getFile();
        Logger logger = getLogger();

        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + file);
        }

        File outFile = new File(dataFolder, resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(dataFolder, resourcePath.substring(0, Math.max(lastIndex, 0)));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists()) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
        }

    }

    @Override
    public void sendConsoleMessage(Component message) {
        getProxy().getConsole().sendMessage(BungeeComponentSerializer.get().serialize(message));
    }

    @Override
    public PlayerAdapter getPlayer(UUID uuid) {

        ProxiedPlayer player = getProxy().getPlayer(uuid);
        if (player == null) return null;

        return new PlayerBungee(player);

    }

    @Override
    public PlayerAdapter getPlayer(String name) {

        ProxiedPlayer player = getProxy().getPlayer(name);
        if (player == null) return null;

        return new PlayerBungee(player);

    }

    @Override
    public List<PlayerAdapter> getOnlinePlayers() {
        return getProxy().getPlayers().stream().map(PlayerBungee::new).collect(Collectors.toList());
    }

    @Override
    public YamlAdapter loadYaml(File file) {

        try {
            return new YamlBungee(this, ConfigurationProvider.getProvider(YamlConfiguration.class).load(file));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, e.getMessage(), e);
            return null;
        }

    }

    @Override
    public void runAsyncTaskLater(Runnable task, long ticks) {
        getProxy().getScheduler().schedule(this, task, ticks * 50L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runTaskLater(Runnable task, long ticks) {
        runAsyncTaskLater(task, ticks);
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
    public String setPlaceholders(UUID uuid, String text, boolean brackets) {
        return text;
    }

}
