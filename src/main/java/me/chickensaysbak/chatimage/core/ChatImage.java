// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core;

import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.commands.ChatImageCommand;
import me.chickensaysbak.chatimage.core.commands.IgnoreImagesCommand;
import net.md_5.bungee.api.ChatColor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

public class ChatImage {

    public static final String[] SUPPORTED_EXTENSIONS = new String[] {".png", ".jpg", ".jpeg", ".gif"};

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

        if (recipient != null) {
            PlayerAdapter player = plugin.getPlayer(recipient);
            if (player != null) player.sendMessage(message);
        } else plugin.sendConsoleMessage(message);

    }

    /**
     * Reloads all files.
     */
    public void reload() {
        settings.reload();
        ignoringImages.reload();
    }

    /**
     * Loads an image.
     * @param url the url string of the image
     * @return the loaded image or null if it failed to load
     */
    public BufferedImage loadImage(String url) {

        url = stripURL(url);

        try {
            URLConnection connection = new URL(url).openConnection();
            // Prevents 403 Forbidden errors.
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0");
            return ImageIO.read(connection.getInputStream());
        }

        catch (IOException e) {

            if (settings.isDebug()) {
                plugin.getLogger().warning("ChatImage Debugger - Error loading image");
                plugin.getLogger().warning("URL: " + url);
                e.printStackTrace();
            }

        }

        return null;

    }

    /**
     * Gets rid of extraneous specifications after the file extension of an image url.
     * Helps to prevent incompatible links in moderatecontent.com
     * @param url the url to strip
     * @return the image url without extra parameters after the extension
     */
    public static String stripURL(String url) {

        String[] split1 = url.split("//", 2);
        if (split1.length < 2) return url;
        String urlMiddle = split1[1]; // Everything after the protocol (http or https).

        String[] split2 = urlMiddle.split("/", 2);
        if (split2.length < 2) return url;
        String urlEnd = split2[1]; // Everything after the domain.

        for (String ext : SUPPORTED_EXTENSIONS) if (urlEnd.contains(ext)) {
            urlEnd = urlEnd.split(ext)[0] + ext;
            break;
        }

        return split1[0] + "//" + split2[0] + "/" + urlEnd;

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
