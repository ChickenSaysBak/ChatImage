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
    private IgnoringImages ignoringImages;
    private Filtration filtration;

    public ChatImage(PluginAdapter plugin) {

        instance = this;
        this.plugin = plugin;
        settings = new Settings(plugin);
        ignoringImages = new IgnoringImages(plugin);
        filtration = new Filtration();

        plugin.registerCommand(new ChatImageCommand(plugin));
        plugin.registerCommand(new IgnoreImagesCommand(plugin));

    }

    /**
     * Called when the server shuts down and the plugin is disabled.
     */
    public void onDisable() {
        if (ignoringImages.isSaveQueued()) ignoringImages.saveFile();
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

    /**
     * Sends a UI message if it exists in the settings. If the recipient is null, the message is sent to console.
     * @param recipient the UUID of the recipient or null for console
     * @param key the setting key for the message
     */
    public void sendUIMessage(UUID recipient, String key) {

        String message = settings.getMessage(key);
        if (message == null) return;
        message = ChatColor.translateAlternateColorCodes('&', message);

        if (recipient != null) plugin.getPlayer(recipient).sendMessage(message);
        else plugin.sendConsoleMessage(message);

    }

    /**
     * Reloads all files.
     */
    public void reload() {
        settings.reload();
        ignoringImages.reload();
    }

    public static ChatImage getInstance() {
        return instance;
    }

    public PluginAdapter getPlugin() {
        return plugin;
    }

    public Settings getSettings() {
        return settings;
    }
    public IgnoringImages getIgnoringImages() {
        return ignoringImages;
    }

}
