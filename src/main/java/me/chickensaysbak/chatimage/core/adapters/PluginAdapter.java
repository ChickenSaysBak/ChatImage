// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.adapters;

import net.md_5.bungee.api.chat.TextComponent;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

public interface PluginAdapter {

    void registerCommand(CommandAdapter command);
    File getDataFolder();
    InputStream getResource(String name);
    YamlAdapter loadYaml(File file);
    void sendMessage(UUID recipient, String message);
    void sendImage(UUID recipient, TextComponent component);
    boolean isBungee();
    int getPlayerVersion(UUID uuid);

}
