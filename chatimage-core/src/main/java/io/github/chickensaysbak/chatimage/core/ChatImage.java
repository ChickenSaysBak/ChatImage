// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package io.github.chickensaysbak.chatimage.core;

import io.github.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import io.github.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import io.github.chickensaysbak.chatimage.core.commands.ChatImageCommand;
import io.github.chickensaysbak.chatimage.core.commands.HideImagesCommand;
import io.github.chickensaysbak.chatimage.core.commands.ShowImagesCommand;
import io.github.chickensaysbak.chatimage.core.loaders.Loadable;
import io.github.chickensaysbak.chatimage.core.loaders.PlayerPreferences;
import io.github.chickensaysbak.chatimage.core.loaders.SavedMedia;
import io.github.chickensaysbak.chatimage.core.loaders.Settings;
import io.github.chickensaysbak.chatimage.core.media.Gif;
import io.github.chickensaysbak.chatimage.core.media.HidableImage;
import io.github.chickensaysbak.chatimage.core.media.Image;
import io.github.chickensaysbak.chatimage.core.media.Media;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.*;
import java.util.logging.Level;

public class ChatImage {

    private static ChatImage instance;
    private PluginAdapter plugin;
    private GifHandler gifHandler;
    private Filtration filtration;

    private Settings settings;
    private PlayerPreferences playerPreferences;
    private SavedMedia savedMedia;
    private ArrayList<Loadable> loaders = new ArrayList<>();

    private HashMap<String, Long> lastSent = new HashMap<>(); // Contains UUIDs and Discord IDs.
    private ArrayList<UUID> recordedPlayerLocale = new ArrayList<>();

    public static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0";

    public ChatImage(PluginAdapter plugin) {

        instance = this;
        this.plugin = plugin;
        gifHandler = new GifHandler(plugin);
        filtration = new Filtration(plugin);

        settings = new Settings(plugin);
        playerPreferences = new PlayerPreferences(plugin);
        savedMedia = new SavedMedia(plugin);
        loaders.addAll(Arrays.asList(settings, playerPreferences, savedMedia));

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
     * Called when a player joins.
     * @param player the player that joined
     */
    public void onJoin(PlayerAdapter player) {

        UUID uuid = player.getUniqueId();
        if (recordedPlayerLocale.contains(uuid)) return;

        plugin.runTaskLater(() -> {

            Map<String, Map<String, Integer>> map = new HashMap<>();
            Map<String, Integer> entry = new HashMap<>();

            entry.put(player.getLocale().toLowerCase(), 1);
            map.put(settings.getLanguageDefault(), entry);
            plugin.publishStat("locale", map);
            if (!recordedPlayerLocale.contains(uuid)) recordedPlayerLocale.add(uuid);

        }, 20);

    }

    /**
     * Called when a player sends a message in chat.
     * @param player the player that chatted
     * @param message the message sent
     * @return true if the event should be canceled and have the message removed
     */
    public boolean onChat(PlayerAdapter player, Component message) {
        return processChatLinks(message, player.getUniqueId().toString(), player.hasPermission("chatimage.use"));
    }

    /**
     * Called when a player clicks a custom dialog button.
     * @param player the player that clicked
     * @param id the ID of the button
     */
    public void onClick(PlayerAdapter player, String id) {

        if (id.equals("chatimage:close_gif")) gifHandler.closeGif(player);

        else if (id.startsWith("chatimage:open_gif_")) {

            String[] parts = id.split("_");
            int gifID = Integer.parseInt(parts[2]);
            String text = parts.length >= 4 ? Gif.decodeText(parts[3]) : null;

            Component component = text != null && !text.isEmpty() ? MiniMessage.miniMessage().deserialize(text) : null;
            gifHandler.playGif(player, gifHandler.getGif(gifID), component);

        }

    }

    /**
     * Searches for a url in a message and attempts to render an image if applicable.
     * @param message the message to scan
     * @param id the UUID or Discord ID of the user
     * @param hasPermission if the user has permission to render images
     * @return true if the message should be removed
     */
    public boolean processChatLinks(Component message, String id, boolean hasPermission) {

        int cooldown = settings.getCooldown();
        boolean strictCooldown = settings.isStrictCooldown();
        boolean filterExplicitContent = settings.isFilterExplicitContent();
        boolean removeExplicitContent = settings.isRemoveExplicitContent();

        boolean underCooldown = cooldown > 0 && lastSent.containsKey(id) && (System.currentTimeMillis()-lastSent.get(id))/1000 < cooldown;
        boolean underRegularCooldown = !strictCooldown && underCooldown;
        boolean dontRender = !hasPermission || underRegularCooldown;

        /*
         The following code may be run two different ways:
             Optimal: Stop Check #1, Filtration #2
             Message Removal Enabled: Filtration #1, Stop Check #2
        */

        // Stop Check #1 - Terminates code early if possible; optimal if message removal is disabled.
        if (!removeExplicitContent && dontRender) return false;

        String url = findURL(message);
        if (url == null) return false;

        // Filtration #1 - Removes message and prevents rendering.
        if (removeExplicitContent && filterExplicitContent && filtration.hasExplicitContent(url)) return true;

        // Stop Check #2 - Terminates code if necessary AFTER message removal.
        if (dontRender) return false;
        lastSent.put(id, System.currentTimeMillis()); // Cooldown starts over early with Strict Cooldown enabled
        if (strictCooldown && underCooldown) return false;

        // Renders the image AFTER the message has been sent.
        plugin.runAsyncTaskLater(() -> {

            // Filtration #2 - Only prevents rendering; if message removal is disabled, it's ideal to run on a separate thread.
            if (!removeExplicitContent && filterExplicitContent && filtration.hasExplicitContent(url)) return;

            Media media = loadMedia(url, null, null, null, null, true);
            if (media != null) plugin.getOnlinePlayers().forEach(p -> p.sendMessage(media.formatFor(p, null, false)));

        }, 1);

        return false;

    }

    /**
     * Sends a UI message with variables if it exists in the settings. If the recipient is null, the message is sent to console.
     * @param recipient the UUID of the recipient or null for console
     * @param key the setting key for the message
     * @param variables variables to be replaced in the message
     */
    public void sendUIMessage(UUID recipient, String key, TagResolver... variables) {

        PlayerAdapter player = recipient != null ? plugin.getPlayer(recipient) : null;
        String locale = player != null ? player.getLocale() : settings.getLanguageDefault();

        Component message = getUIMessage(key, locale, variables);
        if (message == null) return;

        if (recipient != null) {
            if (player != null) player.sendMessage(message);
        } else plugin.sendConsoleMessage(message);

    }

    /**
     * Gets a UI message with variables if it exists in the settings.
     * @param key the setting key for the message
     * @param variables variables to be replaced in the message
     * @param locale the locale of the recipient
     * @return the UI message or null if it doesn't exist
     */
    public Component getUIMessage(String key, String locale, TagResolver... variables) {

        String message = settings.getMessage(key, locale);
        if (message == null) return null;

        return MiniMessage.miniMessage().deserialize(message, variables);

    }

    /**
     * Reloads all files.
     */
    public void reload() {
        for (Loadable loadable : loaders) loadable.reload();
    }

    /**
     * Finds the first URL in a string of text.
     * @param text the text to search
     * @return the first URL found
     */
    public static String findURL(Component text) {

        String stripped = PlainTextComponentSerializer.plainText().serialize(text);
        String[] words = stripped.split(" ");

        for (String word : words) if (word.startsWith("http")) return word;
        return null;

    }

    /**
     * Loads media from a URL.
     * @param url the URL string
     * @param width the maximum width (null for default)
     * @param height the maximum height (null for default)
     * @param smooth smooth rendering (null for default)
     * @param trim trim transparency (null for default)
     * @param user true if a user sent the URL
     * @return the media
     */
    public Media loadMedia(String url, Integer width, Integer height, Boolean smooth, Boolean trim, boolean user) {

        if (url.toLowerCase().contains("tenor.com/view")) url = extractDirectTenorUrl(url);
        if (smooth == null) smooth = settings.isSmoothRender();
        if (trim == null) trim = settings.isTrimTransparency();

        try {

            URLConnection connection = new URI(url).toURL().openConnection();
            // Prevents 403 Forbidden errors.
            connection.addRequestProperty("User-Agent", USER_AGENT);

            if ("image/gif".equalsIgnoreCase(connection.getContentType())) {

                if (width == null) width = settings.getMaxGifWidth();
                if (height == null) height = settings.getMaxGifHeight();

                GifHandler.Gif gif = gifHandler.loadGif(connection, new Dimension(width, height), smooth);
                if (gif != null) return new Gif(gif);

            }

            try (InputStream in = connection.getInputStream()) {

                BufferedImage img = ImageIO.read(in);

                if (img == null) {

                    if (settings.isDebug()) {
                        plugin.getLogger().warning("ChatImage Debugger - Error loading image: Null BufferedImage");
                        plugin.getLogger().warning("URL: " + url);
                    }

                    return null;

                }

                if (width == null) width = settings.getMaxWidth();
                if (height == null) height = settings.getMaxHeight();

                return user
                        ? new HidableImage(url, img)
                        : new Image(img, new Dimension(width, height), smooth, trim);

            }

        }

        catch (IOException | URISyntaxException e) {

            if (settings.isDebug()) {
                plugin.getLogger().warning("ChatImage Debugger - Error loading media");
                plugin.getLogger().warning("URL: " + url);
                plugin.getLogger().log(Level.WARNING, e.getMessage(), e);
            }

        }

        return null;

    }

    /**
     * Finds the direct GIF URL when provided a tenor.com/view URL.
     * @param url the Tenor URL pointing to a webpage
     * @return the Tenor URL pointing to a GIF
     */
    private String extractDirectTenorUrl(String url) {

        Document doc;

        try {
            doc = Jsoup.connect(url).userAgent(ChatImage.USER_AGENT).get();
        } catch (IOException e) {

            if (settings.isDebug()) {
                plugin.getLogger().warning("ChatImage Debugger - Error extracting Tenor URL");
                plugin.getLogger().warning("URL: " + url);
                plugin.getLogger().log(Level.WARNING, e.getMessage(), e);
            }

            return url;

        }

        // Check the OpenGraph "og:image" meta tag
        Element meta = doc.selectFirst("meta[property=og:image]");
        if (meta != null) return meta.attr("content");

        // As fallback, check Twitter card meta
        meta = doc.selectFirst("meta[property=twitter:image]");
        if (meta != null) return meta.attr("content");

        if (settings.isDebug()) {
            plugin.getLogger().warning("ChatImage Debugger - Error extracting Tenor URL");
            plugin.getLogger().warning("URL: " + url);
        }

        return url;

    }

    public static ChatImage getInstance() {
        return instance;
    }

    public GifHandler getGifHandler() {
        return gifHandler;
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

    public SavedMedia getSavedImages() {
        return savedMedia;
    }

}
