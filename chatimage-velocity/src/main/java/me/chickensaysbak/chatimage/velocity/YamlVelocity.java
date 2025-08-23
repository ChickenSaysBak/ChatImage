package me.chickensaysbak.chatimage.velocity;

import me.chickensaysbak.chatimage.core.adapters.YamlAdapter;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class YamlVelocity implements YamlAdapter {

    private ConfigurationNode config;

    YamlVelocity(ConfigurationNode config) {
        this.config = config;
    }

    @Override
    public void save(File file) throws IOException {
        YamlConfigurationLoader.builder().file(file).build().save(config);
    }

    @Override
    public void set(String path, Object value) {

        try {
            config.node(path).set(value);
        } catch (SerializationException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Collection<String> getKeys() {
        return config.childrenMap().keySet().stream().map(Object::toString).collect(Collectors.toList());
    }

    @Override
    public Collection<String> getKeys(String path) {
        return config.node(path).childrenMap().keySet().stream().map(Object::toString).collect(Collectors.toList());
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return config.node(path).getBoolean(def);
    }

    @Override
    public int getInt(String path, int def) {
        return config.node(path).getInt(def);
    }

    @Override
    public long getLong(String path, long def) {
        return config.node(path).getLong(def);
    }

    @Override
    public double getDouble(String path, double def) {
        return config.node(path).getDouble(def);
    }

    @Override
    public String getString(String path, String def) {
        return config.node(path).getString(def);
    }

    @Override
    public List<String> getStringList(String path) {

        try {
            return config.node(path).getList(String.class);
        } catch (SerializationException e) {
            e.printStackTrace();
            return List.of();
        }

    }

}
