// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.plugin.bungee;

import me.chickensaysbak.chatimage.core.adapters.YamlAdapter;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class YamlBungee implements YamlAdapter {

    private Configuration config;

    YamlBungee(Configuration config) {
        this.config = config;
    }

    @Override
    public void save(File file) throws IOException {
        ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
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