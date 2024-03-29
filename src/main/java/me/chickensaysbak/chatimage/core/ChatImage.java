// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core;

import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.commands.ChatImageCommand;
import me.chickensaysbak.chatimage.core.commands.HideImagesCommand;
import me.chickensaysbak.chatimage.core.commands.ShowImagesCommand;
import me.chickensaysbak.chatimage.core.loaders.Loadable;
import me.chickensaysbak.chatimage.core.loaders.PlayerPreferences;
import me.chickensaysbak.chatimage.core.loaders.SavedImages;
import me.chickensaysbak.chatimage.core.loaders.Settings;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class ChatImage {

    private static ChatImage instance;
    private PluginAdapter plugin;
    private Filtration filtration;

    private Settings settings;
    private PlayerPreferences playerPreferences;
    private SavedImages savedImages;
    private ArrayList<Loadable> loaders = new ArrayList<>();

    private HashMap<String, Long> lastSent = new HashMap<>(); // Contains UUIDs and Discord IDs.

    public ChatImage(PluginAdapter plugin) {

        instance = this;
        this.plugin = plugin;
        filtration = new Filtration(plugin);

        settings = new Settings(plugin);
        playerPreferences = new PlayerPreferences(plugin);
        savedImages = new SavedImages(plugin);
        loaders.addAll(Arrays.asList(settings, playerPreferences, savedImages));

        plugin.registerCommand(new ChatImageCommand(plugin));
        plugin.registerCommand(new HideImagesCommand(plugin));
        plugin.registerCommand(new ShowImagesCommand(plugin));

    }

    /**
     * Called when the server shuts down and the plugin is disabled.
     */
    public void onDisable() {
        if (playerPreferences.isSaveQueued()) playerPreferences.saveFile();
    }

    /**
     * Called when a player sends a message in chat.
     * @param player the player that chatted
     * @param message the message sent
     * @return true if the event should be canceled and have the message removed
     */
    public boolean onChat(PlayerAdapter player, String message) {
        return processChatLinks(message, player.getUniqueId().toString(), player.hasPermission("chatimage.use"));
    }

    /**
     * Searches for a url in a message and attempts to render an image if applicable.
     * @param message the message to scan
     * @param id the UUID or Discord ID of the user
     * @param hasPermission if the user has permission to render images
     * @return true if the message should be removed
     */
    public boolean processChatLinks(String message, String id, boolean hasPermission) {

        int cooldown = settings.getCooldown();
        boolean strictCooldown = settings.isStrictCooldown();
        boolean filterBadWords = settings.isFilterBadWords(), filterExplicitContent = settings.isFilterExplicitContent();
        boolean removeBadWords = settings.isRemoveBadWords(), removeExplicitContent = settings.isRemoveExplicitContent();

        boolean underCooldown = cooldown > 0 && lastSent.containsKey(id) && (System.currentTimeMillis()-lastSent.get(id))/1000 < cooldown;
        boolean underRegularCooldown = !strictCooldown && underCooldown;
        boolean dontRender = !hasPermission || underRegularCooldown;

        /*
         The following code may be run two different ways:
             Optimal: Stop Check #1, Filtration #2
             Message Removal Enabled: Filtration #1, Stop Check #2
        */

        // Stop Check #1 - Terminates code early if possible; optimal if message removal is disabled.
        if (!removeBadWords && !removeExplicitContent && dontRender) return false;

        String url = findURL(message);
        if (url == null) return false;

        // Filtration #1 - Removes message and prevents rendering.
        if (removeBadWords && filterBadWords && filtration.hasBadWords(url)) return true;
        else if (removeExplicitContent && filterExplicitContent && filtration.hasExplicitContent(url)) return true;

        // Stop Check #2 - Terminates code if necessary AFTER message removal.
        if (dontRender) return false;
        lastSent.put(id, System.currentTimeMillis()); // Cooldown starts over early with Strict Cooldown enabled
        if (strictCooldown && underCooldown) return false;

        // Renders the image AFTER the message has been sent.
        plugin.runAsyncTaskLater(() -> {

            // Filtration #2 - Only prevents rendering; if message removal is disabled, it's ideal to run on a separate thread.
            if (!removeBadWords && filterBadWords && filtration.hasBadWords(url)) return;
            else if (!removeExplicitContent && filterExplicitContent && filtration.hasExplicitContent(url)) return;

            BufferedImage image = loadImage(url);
            if (image == null) return;

            boolean smooth = settings.isSmoothRender(), trim = settings.isTrimTransparency();
            Dimension dim = new Dimension(settings.getMaxWidth(), settings.getMaxHeight());
            Dimension hiddenDim = new Dimension(settings.getMaxHiddenWidth(), settings.getMaxHiddenHeight());

            BaseComponent[] expandedImage = getExpandedImage(url, ImageMaker.createChatImage(image, dim, smooth, trim));
            BaseComponent[] hiddenImage = getHiddenImage(url, ImageMaker.createChatImage(image, hiddenDim, smooth, trim));

            for (PlayerAdapter p : plugin.getOnlinePlayers()) {
                if (!isVersionValid(p.getVersion())) continue;
                p.sendMessage(playerPreferences.isShowingImages(p.getUniqueId()) ? expandedImage : hiddenImage);
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
        sendUIMessage(recipient, key, null);
    }

    /**
     * Sends a UI message with variables if it exists in the settings. If the recipient is null, the message is sent to console.
     * @param recipient the UUID of the recipient or null for console
     * @param key the setting key for the message
     * @param variables variables to be replaced in the message (name followed by value)
     */
    public void sendUIMessage(UUID recipient, String key, Map<String, String> variables) {

        String message = getUIMessage(key, variables);
        if (message == null) return;

        if (recipient != null) {
            PlayerAdapter player = plugin.getPlayer(recipient);
            if (player != null) player.sendMessage(message);
        } else plugin.sendConsoleMessage(message);

    }

    /**
     * Gets a UI message with variables if it exists in the settings.
     * @param key the setting key for the message
     * @return the UI message or null if it doesn't exist
     */
    public String getUIMessage(String key) {
        return getUIMessage(key, null);
    }

    /**
     * Gets a UI message with variables if it exists in the settings.
     * @param key the setting key for the message
     * @param variables variables to be replaced in the message (name followed by value)
     * @return the UI message or null if it doesn't exist
     */
    public String getUIMessage(String key, Map<String, String> variables) {

        String message = settings.getMessage(key);
        if (message == null) return null;

        if (variables != null) for (String name : variables.keySet()) message = message.replace("%" + name + "%", variables.get(name));
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;

    }

    /**
     * Reloads all files.
     */
    public void reload() {
        for (Loadable loadable : loaders) loadable.reload();
    }

    /**
     * Finds the first url in a string of text.
     * @param text the text to search
     * @return the first url found
     */
    public static String findURL(String text) {
        String[] words = ChatColor.stripColor(text).split(" ");
        for (String word : words) if (word.startsWith("http")) return word;
        return null;
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
            BufferedImage img = ImageIO.read(connection.getInputStream());

            if (img == null && settings.isDebug()) {
                plugin.getLogger().warning("ChatImage Debugger - Error loading image: Null BufferedImage");
                plugin.getLogger().warning("URL: " + url);
            }

            return img;

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
     * Gets an expanded image that can be clicked on to open and contains a tip about /hideimages.
     * @param url the url of the image
     * @param imageComponent the chat image
     * @return the expanded image
     */
    public BaseComponent[] getExpandedImage(String url, TextComponent imageComponent) {

        ComponentBuilder builder = new ComponentBuilder(imageComponent);
        String hoverTip = settings.getMessage("hover_tip");

        if (hoverTip != null) {
            Text tip = new Text(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', hoverTip)));
            builder.event(new HoverEvent(tip.requiredAction(), tip));
        }

        return builder.event(new ClickEvent(ClickEvent.Action.OPEN_URL, url)).create();

    }

    /**
     * Gets a hidden image that can be clicked on to open and hovered over to view.
     * @param url the url of the image
     * @param imageComponent the chat image
     * @return the hidden image
     */
    public BaseComponent[] getHiddenImage(String url, TextComponent imageComponent) {

        String showImage = settings.getMessage("show_image");
        showImage = ChatColor.translateAlternateColorCodes('&', showImage != null ? showImage : "&a[Show Image]");

        Text hoverImage = new Text(new ComponentBuilder("\n").append(imageComponent).create()); // New line prevents odd spacing.
        HoverEvent hoverEvent = new HoverEvent(hoverImage.requiredAction(), hoverImage);
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, url);

        return new ComponentBuilder().appendLegacy(showImage).event(hoverEvent).event(clickEvent).create();

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

    public Filtration getFiltration() {
        return filtration;
    }

    public PluginAdapter getPlugin() {
        return plugin;
    }

    public Settings getSettings() {
        return settings;
    }

    public PlayerPreferences getPlayerPreferences() {
        return playerPreferences;
    }

    public SavedImages getSavedImages() {
        return savedImages;
    }

}
