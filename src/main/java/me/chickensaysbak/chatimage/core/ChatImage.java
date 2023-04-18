// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core;

import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.commands.ChatImageCommand;
import me.chickensaysbak.chatimage.core.commands.IgnoreImagesCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.UUID;

public class ChatImage {

    public static final String[] SUPPORTED_EXTENSIONS = new String[] {".png", ".jpg", ".jpeg", ".gif"};

    private static ChatImage instance;
    private PluginAdapter plugin;
    private Settings settings;
    private IgnoringImages ignoringImages;
    private Filtration filtration;

    private HashMap<UUID, Long> lastSent = new HashMap<>();

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
     * @param player the player that chatted
     * @param message the message sent
     * @return true if the event should be cancelled and have the message removed
     */
    public boolean onChat(PlayerAdapter player, String message) {

        UUID uuid = player.getUniqueId();
        int cooldown = settings.getCooldown();

        boolean underCooldown = cooldown > 0 && lastSent.containsKey(uuid) && (System.currentTimeMillis()-lastSent.get(uuid))/1000 < cooldown;
        boolean hasPermission = player.hasPermission("chatimage.use");
        boolean noMessageRemoval = !settings.isRemoveBadWords() && !settings.isRemoveExplicitContent();
        boolean underRegularCooldown = !settings.isStrictCooldown() && underCooldown;
        boolean dontRender = !hasPermission || underRegularCooldown;

        // Optimal if messages don't need to be checked for removal.
        if (noMessageRemoval && dontRender) return false;

        String url = findURL(message);
        if (url == null) return false;

        // ! Filtration Here !

        if (dontRender) return false;
        lastSent.put(uuid, System.currentTimeMillis());
        if (settings.isStrictCooldown() && underCooldown) return false;

        // Renders the image AFTER the message has been sent.
        plugin.runAsyncTaskLater(() -> {

            // ! Filtration Here !

            BufferedImage image = loadImage(url);
            if (image == null) return;

            Dimension dim = new Dimension(settings.getMaxWidth(), settings.getMaxHeight());
            boolean smooth = settings.isSmoothRender(), trim = settings.isTrimTransparency();
            TextComponent component = ImageMaker.createChatImage(image, dim, smooth, trim);

            for (PlayerAdapter p : plugin.getOnlinePlayers()) {
                if (!isVersionValid(p.getVersion()) || ignoringImages.isIgnoring(p.getUniqueId())) continue;
                p.sendMessage(component);
            }

        }, 1);

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

    /**
     * Finds the first url in a string of text.
     * @param text the text to search
     * @return the first url found
     */
    public static String findURL(String text) {
        String[] words = ChatColor.stripColor(text).split(" ");
        for (String word : words) if (word.startsWith("http")) return stripURL(word);
        return null;
    }

    /**
     * Checks if the client version is 1.16 or higher and can see custom colors.
     * For Spigot servers, this will always be true.
     * @param version the client's protocol version
     * @return true if the client's version is 1.16 or higher
     */
    public static boolean isVersionValid(int version) {
        return version == -1 || version > 735;
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
