// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.adapters;

import net.kyori.adventure.text.Component;

import java.util.UUID;

public interface PlayerAdapter {

    UUID getUniqueId();
    String getName();
    boolean isOnline();
    boolean hasPermission(String node);
    void sendMessage(Component component);
    int getVersion();
    String getLocale();

}
