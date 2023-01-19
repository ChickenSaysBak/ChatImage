// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core;

import me.chickensaysbak.chatimage.core.commands.ChatImageCommand;
import me.chickensaysbak.chatimage.core.commands.IgnoreImagesCommand;
import me.chickensaysbak.chatimage.core.wrappers.PluginWrapper;

import java.util.UUID;

public class ChatImage {

    private PluginWrapper plugin;

    public ChatImage(PluginWrapper plugin) {

        this.plugin = plugin;

        plugin.registerCommand(new ChatImageCommand(plugin));
        plugin.registerCommand(new IgnoreImagesCommand(plugin));

    }

    /**
     * Called when a player sends a message in chat.
     * @param uuid refers to the player
     * @param message the message sent
     * @return true if the event should be cancelled and have the message removed
     */
    public boolean onChat(UUID uuid, String message) {

        return false;

    }

}