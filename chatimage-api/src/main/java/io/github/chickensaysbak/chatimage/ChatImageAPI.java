// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package io.github.chickensaysbak.chatimage;

import io.github.chickensaysbak.chatimage.core.ChatImage;
import io.github.chickensaysbak.chatimage.core.media.Media;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class ChatImageAPI {

    private ChatImageAPI() {}

    /**
     * Check if a player is hiding images via /hideimages.
     * @param uuid the uuid of the player to check
     * @return true if the player is hiding images
     */
    public static boolean isHidingImages(UUID uuid) {
        return !ChatImage.getInstance().getPlayerPreferences().isShowingImages(uuid);
    }

    /**
     * Creates an image or GIF that can be sent in Minecraft chat.
     * All parameters are optional besides the URL. Any unused parameters will use config defaults.
     * @param url the URL of the media
     * @param text optional text to append to the right of the image or top of the GIF
     * @param smooth whether to use smooth rendering (true) or simple rendering (false)
     * @param trim whether to trim transparent edges or not (ignored for GIFs)
     * @param width the maximum width the content can be
     * @param height the maximum height the content can be
     * @return a Minecraft chat image or clickable GIF text
     */
    public static Component createMediaFromURL(String url, String text, boolean smooth, boolean trim, int width, int height) {

        Media media = ChatImage.getInstance().loadMedia(url, width, height, smooth, trim, false);
        if (media == null) return null;

        return media.formatFor(null, text, false);

    }

    /**
     * @see #createMediaFromURL(String, String, boolean, boolean, int, int)
     */
    public static Component createMediaFromURL(String url, String text, boolean smooth, boolean trim, int width) {
        int height = ChatImage.getInstance().getSettings().getMaxHeight();
        return createMediaFromURL(url, text, smooth, trim, width, height);
    }

    /**
     * @see #createMediaFromURL(String, String, boolean, boolean, int, int)
     */
    public static Component createMediaFromURL(String url, String text, boolean smooth, boolean trim) {
        int width = ChatImage.getInstance().getSettings().getMaxWidth();
        return createMediaFromURL(url, text, smooth, trim, width);
    }

    /**
     * @see #createMediaFromURL(String, String, boolean, boolean, int, int)
     */
    public static Component createMediaFromURL(String url, String text, boolean smooth) {
        boolean trim = ChatImage.getInstance().getSettings().isTrimTransparency();
        return createMediaFromURL(url, text, smooth, trim);
    }

    /**
     * @see #createMediaFromURL(String, String, boolean, boolean, int, int)
     */
    public static Component createMediaFromURL(String url, String text) {
        boolean smooth = ChatImage.getInstance().getSettings().isSmoothRender();
        return createMediaFromURL(url, text, smooth);
    }

    /**
     * @see #createMediaFromURL(String, String, boolean, boolean, int, int)
     */
    public static Component createMediaFromURL(String url) {
        return createMediaFromURL(url, null);
    }

    /**
     * Gets an image or GIF that has been saved in the ChatImage plugin folder.
     * Text is optional.
     * @param name the name of the saved media
     * @param text optional text to append to the right of the image or top of the GIF
     * @return a Minecraft chat image or clickable GIF text
     */
    public static Component getSavedMedia(String name, String text) {

        Media media = ChatImage.getInstance().getSavedImages().getMedia(name);
        if (media == null) return null;

        return media.formatFor(null, text, false);

    }

    /**
     * @see #getSavedMedia(String, String)
     */
    public static Component getSavedMedia(String name) {
        return getSavedMedia(name, null);
    }

}
