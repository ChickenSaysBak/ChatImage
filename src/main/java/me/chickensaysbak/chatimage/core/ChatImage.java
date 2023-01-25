// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core;

import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.commands.ChatImageCommand;
import me.chickensaysbak.chatimage.core.commands.IgnoreImagesCommand;
import net.md_5.bungee.api.ChatColor;

import java.util.UUID;

public class ChatImage {

    private static ChatImage instance;
    private PluginAdapter plugin;
    private Settings settings;
    private Filtration filtration;

    public ChatImage(PluginAdapter plugin) {

        instance = this;
        this.plugin = plugin;
        settings = new Settings(plugin);
        filtration = new Filtration();

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

    static ChatImage getInstance() {
        return instance;
    }

    public PluginAdapter getPlugin() {
        return plugin;
    }

    public Settings getSettings() {
        return settings;
    }

}
