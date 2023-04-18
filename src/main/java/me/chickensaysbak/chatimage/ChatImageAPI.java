// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage;

import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.ImageMaker;
import net.md_5.bungee.api.chat.TextComponent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.UUID;
public class ChatImageAPI {

    private ChatImageAPI() {}

    /**
     * Check if a player is ignoring images via /ignoreimages.
     * @param uuid the uuid of the player to check
     * @return true if the player is ignoring images
     */
    public static boolean isIgnoringImages(UUID uuid) {
        return ChatImage.getInstance().getIgnoringImages().isIgnoring(uuid);
    }

    /**
     * Creates an image that can be sent in Minecraft chat.
     * All parameters are optional besides the url. Any unused parameters will use config defaults.
     * @param url the url of the image
     * @param text optional text to append to the right of the image
     * @param smooth whether to use smooth rendering (true) or simple rendering (false)
     * @param trim whether to trim transparent edges or not
     * @param width the maximum width an image can be
     * @param height the maximum height an image can be
     * @return a Minecraft chat image
     */
    public static TextComponent createChatImage(String url, String text, boolean smooth, boolean trim, int width, int height) {

        BufferedImage image = ChatImage.getInstance().loadImage(url);
        if (image == null) return null;

        Dimension dim = new Dimension(width, height);
        TextComponent component = ImageMaker.createChatImage(image, dim, smooth, trim);
        if (text != null && !text.isEmpty()) component = ImageMaker.addText(component, text);

        return component;

    }

    public static TextComponent createChatImage(String url, String text, boolean smooth, boolean trim, int width) {
        int height = ChatImage.getInstance().getSettings().getMaxHeight();
        return createChatImage(url, text, smooth, trim, width, height);
    }

    public static TextComponent createChatImage(String url, String text, boolean smooth, boolean trim) {
        int width = ChatImage.getInstance().getSettings().getMaxWidth();
        return createChatImage(url, text, smooth, trim, width);
    }

    public static TextComponent createChatImage(String url, String text, boolean smooth) {
        boolean trim = ChatImage.getInstance().getSettings().isTrimTransparency();
        return createChatImage(url, text, smooth, trim);
    }

    public static TextComponent createChatImage(String url, String text) {
        boolean smooth = ChatImage.getInstance().getSettings().isSmoothRender();
        return createChatImage(url, text, smooth);
    }

    public static TextComponent createChatImage(String url) {
        return createChatImage(url, null);
    }

}
