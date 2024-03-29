// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.plugin.bungee;

import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.adapters.CommandAdapter;
import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.adapters.YamlAdapter;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.bstats.bungeecord.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent event) {

        Connection sender = event.getSender();
        if (event.isCancelled() || event.isCommand() || !(sender instanceof ProxiedPlayer)) return;

        ProxiedPlayer player = (ProxiedPlayer) sender;
        boolean cancelEvent = core.onChat(new PlayerBungee(player), event.getMessage());
        if (cancelEvent) event.setCancelled(true);

    }

    @Override
    public void registerCommand(CommandAdapter command) {
        getProxy().getPluginManager().registerCommand(this, new CommandBungee(command));
    }

    @Override
    public InputStream getResource(String name) {
        return getResourceAsStream(name);
    }

    @Override
    public void sendConsoleMessage(String message) {
        getProxy().getConsole().sendMessage(TextComponent.fromLegacyText(message));
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
            return new YamlBungee(ConfigurationProvider.getProvider(YamlConfiguration.class).load(file));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public int runAsyncTaskLater(Runnable task, int ticks) {
        return getProxy().getScheduler().schedule(this, task, ticks * 50L, TimeUnit.MILLISECONDS).getId();
    }

    @Override
    public boolean isBungee() {
        return true;
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
    public String setPlaceholders(UUID uuid, String text) {
        return text;
    }

}
