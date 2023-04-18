// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.adapters;

import net.md_5.bungee.api.chat.BaseComponent;

import java.util.UUID;

public interface PlayerAdapter {

    UUID getUniqueId();
    String getName();
    boolean isOnline();
    boolean hasPermission(String node);
    void sendMessage(String message);
    void sendMessage(BaseComponent component);
    int getVersion();

}
