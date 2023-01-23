// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.wrappers;

import net.md_5.bungee.api.chat.BaseComponent;

import java.util.UUID;

public interface PluginWrapper {

    void registerCommand(CommandWrapper command);
    void sendPlayerMessage(UUID uuid, String message);
    void sendPlayerComponent(UUID uuid, BaseComponent component);
    boolean isBungee();

}