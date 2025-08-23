// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.adapters.CommandAdapter;
import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.adapters.YamlAdapter;
import net.kyori.adventure.text.Component;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bstats.velocity.Metrics;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PluginVelocity implements PluginAdapter {

    private ProxyServer proxy;
    private Logger logger;
    private Path dataFolder;
    private Metrics.Factory metricsFactory;

    private ChatImage core;
    private Metrics bStats;

    @Inject
    public PluginVelocity(ProxyServer proxy, Logger logger, @DataDirectory Path dataFolder, Metrics.Factory metricsFactory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataFolder = dataFolder;
        this.metricsFactory = metricsFactory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        bStats = metricsFactory.make(this, 22012);
        core = new ChatImage(this);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        core.onDisable();
    }

    @Subscribe
    public void onJoin(ServerConnectedEvent event) {
        core.onJoin(new PlayerVelocity(event.getPlayer()));
    }

    @Subscribe(order = PostOrder.LATE)
    public void onChat(PlayerChatEvent event) {

        if (!event.getResult().isAllowed()) return;

        boolean cancelEvent = core.onChat(new PlayerVelocity(event.getPlayer()), event.getMessage());
        if (cancelEvent) event.setResult(PlayerChatEvent.ChatResult.denied());

    }

    @Override
    public void registerCommand(CommandAdapter command) {
        CommandManager manager = proxy.getCommandManager();
        CommandMeta meta = manager.metaBuilder(command.getName()).aliases(command.getAliases()).plugin(this).build();
        manager.register(meta, new CommandVelocity(command));
    }

    @Override
    public File getDataFolder() {
        return dataFolder.toFile();
    }

    @Override
    public InputStream getResource(String path) {
        return getClass().getClassLoader().getResourceAsStream(path);
    }

    // Adapted from the Spigot API.
    @Override
    public void saveResource(String resourcePath) {

        File dataFolder = getDataFolder();
        Logger logger = getLogger();

        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found");
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
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void sendConsoleMessage(String message) {
        proxy.sendMessage(Component.text(message));
    }

    @Override
    public PlayerAdapter getPlayer(UUID uuid) {
        return proxy.getPlayer(uuid).map(PlayerVelocity::new).orElse(null);
    }

    @Override
    public PlayerAdapter getPlayer(String name) {
        return proxy.getPlayer(name).map(PlayerVelocity::new).orElse(null);
    }

    @Override
    public List<PlayerAdapter> getOnlinePlayers() {
        return proxy.getAllPlayers().stream().map(PlayerVelocity::new).collect(Collectors.toList());
    }

    @Override
    public YamlAdapter loadYaml(File file) {

        try {
            return new YamlVelocity(YamlConfigurationLoader.builder().file(file).build().load());
        } catch (ConfigurateException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public void runAsyncTaskLater(Runnable task, int ticks) {
        proxy.getScheduler().buildTask(this, task).delay(ticks * 50L, TimeUnit.MILLISECONDS).schedule();
    }

    @Override
    public void runTaskLater(Runnable task, int ticks) {
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
    public String setPlaceholders(UUID uuid, String text) {
        return text;
    }
}
