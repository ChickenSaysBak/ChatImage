// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package io.github.chickensaysbak.chatimage.spigot;

import io.github.chickensaysbak.chatimage.core.adapters.YamlAdapter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class YamlSpigot implements YamlAdapter {

    private PluginSpigot plugin;
    private YamlConfiguration config;

    YamlSpigot(PluginSpigot plugin, YamlConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void save(File file) throws IOException {
        config.save(file);
    }

    @Override
    public void set(String path, Object value) {
        config.set(path, value);
    }

    @Override
    public Collection<String> getKeys() {
        return config.getKeys(false);
    }

    @Override
    public Collection<String> getKeys(String path) {

        ArrayList<String> keys = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section != null) keys.addAll(section.getKeys(false));

        return keys;

    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    @Override
    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    @Override
    public long getLong(String path, long def) {
        return config.getLong(path, def);
    }

    @Override
    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    @Override
    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    @Override
    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

}
