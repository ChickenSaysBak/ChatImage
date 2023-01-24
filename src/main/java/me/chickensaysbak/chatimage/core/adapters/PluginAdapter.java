// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.adapters;

import net.md_5.bungee.api.chat.BaseComponent;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

public interface PluginAdapter {

    void registerCommand(CommandAdapter command);
    File getDataFolder();
    InputStream getResource(String name);
    YamlAdapter loadYaml(File file);
    void sendPlayerMessage(UUID uuid, String message);
    void sendPlayerComponent(UUID uuid, BaseComponent component);
    boolean isBungee();

}
