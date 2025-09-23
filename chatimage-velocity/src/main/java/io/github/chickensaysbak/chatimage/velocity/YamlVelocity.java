// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package io.github.chickensaysbak.chatimage.velocity;

import io.github.chickensaysbak.chatimage.core.adapters.YamlAdapter;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class YamlVelocity implements YamlAdapter {

    private PluginVelocity plugin;
    private ConfigurationNode config;

    YamlVelocity(PluginVelocity plugin, ConfigurationNode config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void save(File file) throws IOException {
        YamlConfigurationLoader.builder().file(file).build().save(config);
    }

    @Override
    public void set(String path, Object value) {

        try {
            nodeAtDotPath(config, path).set(value);
        } catch (SerializationException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }

    }

    @Override
    public Collection<String> getKeys() {
        return config.childrenMap().keySet().stream().map(Object::toString).collect(Collectors.toList());
    }

    @Override
    public Collection<String> getKeys(String path) {
        return nodeAtDotPath(config, path).childrenMap().keySet().stream().map(Object::toString).collect(Collectors.toList());
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return nodeAtDotPath(config, path).getBoolean(def);
    }

    @Override
    public int getInt(String path, int def) {
        return nodeAtDotPath(config, path).getInt(def);
    }

    @Override
    public long getLong(String path, long def) {
        return nodeAtDotPath(config, path).getLong(def);
    }

    @Override
    public double getDouble(String path, double def) {
        return nodeAtDotPath(config, path).getDouble(def);
    }

    @Override
    public String getString(String path, String def) {
        return nodeAtDotPath(config, path).getString(def);
    }

    @Override
    public List<String> getStringList(String path) {

        try {
            return nodeAtDotPath(config, path).getList(String.class);
        } catch (SerializationException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return List.of();
        }

    }

    /**
     * Gets the node at the specified dot.separated.path.
     * @param root the node to start from
     * @param path the path separated by dots
     * @return the node at the destination of the path
     */
    private static ConfigurationNode nodeAtDotPath(ConfigurationNode root, String path) {
        String[] parts = path.split("\\.");
        return root.node((Object[]) parts);
    }

}
