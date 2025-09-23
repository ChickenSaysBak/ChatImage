// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.media;

import com.google.gson.JsonObject;
import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.ImageMaker;
import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Image implements Media {

    Component expandedImage;

    public Image(BufferedImage image, Dimension dim, boolean smooth, boolean trim) {
        expandedImage = ImageMaker.createChatImage(image, dim, smooth, trim);
    }

    public Image(Component image) {
        expandedImage = image;
    }

    Image() {}

    @Override
    public Component formatFor(PlayerAdapter player, String text, boolean placeholder) {

        if (player != null && !isVersionCompatible(player.getVersion())) return null;

        if (text != null && !text.isEmpty()) {

            String formattedText = player != null
                    ? ChatImage.getInstance().getPlugin().setPlaceholders(player.getUniqueId(), text, placeholder)
                    : text;

            return ImageMaker.addText(expandedImage, formattedText);

        }

        return expandedImage;

    }

    @Override
    public String serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("image", MiniMessage.miniMessage().serialize(expandedImage));
        return json.toString();
    }

    /**
     * Checks if the client version is 1.16 or higher and can see custom colors.
     * For Spigot servers, this will always be true.
     * @param version the client's protocol version
     * @return true if the client's version is 1.16 or higher
     */
    public static boolean isVersionCompatible(int version) {
        return version == -1 || version >= 735;
    }

}
