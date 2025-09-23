// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package io.github.chickensaysbak.chatimage.bungee;

import io.github.chickensaysbak.chatimage.core.adapters.YamlAdapter;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class YamlBungee implements YamlAdapter {

    private PluginBungee plugin;
    private Configuration config;

    YamlBungee(PluginBungee plugin, Configuration config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void save(File file) throws IOException {
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
    }

    @Override
    public void set(String path, Object value) {
        config.set(path, value);
    }

    @Override
    public Collection<String> getKeys() {
        return config.getKeys();
    }

    @Override
    public Collection<String> getKeys(String path) {

        ArrayList<String> keys = new ArrayList<>();
        Configuration section = config.getSection(path);
        if (section != null) keys.addAll(section.getKeys());

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
