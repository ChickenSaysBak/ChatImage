// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.media;

import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.GifHandler;
import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import me.chickensaysbak.chatimage.core.loaders.Settings;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.api.BinaryTagHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.codec.binary.Base32;

import static net.kyori.adventure.text.Component.empty;

public class Gif implements Media {

    private GifHandler.Gif gif;

    public Gif(GifHandler.Gif gif) {
        this.gif = gif;
    }

    @Override
    public Component formatFor(PlayerAdapter player, String text, boolean placeholder) {

        if (!ChatImage.getInstance().getPlugin().hasDialogSupport()) return null;
        if (player != null && !isVersionCompatible(player.getVersion())) return null;

        Settings settings = ChatImage.getInstance().getSettings();
        MiniMessage mm = MiniMessage.miniMessage();
        String locale = player != null ? player.getLocale() : ChatImage.getInstance().getSettings().getLanguageDefault();

        String showGifMsg = settings.getMessage("show_gif", locale);
        Component showGif = mm.deserialize(showGifMsg != null ? showGifMsg : "<green>[Show GIF]");

        String playGifMsg = settings.getMessage("play_gif", locale);
        Component playGif = playGifMsg != null ? mm.deserialize(playGifMsg) : empty();

        String formattedText = text != null && !text.isEmpty() && player != null
                ? ChatImage.getInstance().getPlugin().setPlaceholders(player.getUniqueId(), text, placeholder)
                : text;

        return showGif
                .hoverEvent(HoverEvent.showText(playGif))
                .clickEvent(ClickEvent.custom(
                        Key.key("chatimage:open_gif_" + gif.getID() + "_" + encodeText(formattedText)),
                        BinaryTagHolder.binaryTagHolder("{}")
                ));

    }

    @Override
    public String serialize() {
        return gif.toJson();
    }

    /**
     * Checks if the client version is 1.21.6 or higher and can view dialogs.
     * For Spigot servers, this will always be true.
     * @param version the client's protocol version
     * @return true if the client's version is 1.21.6 or higher
     */
    public static boolean isVersionCompatible(int version) {
        return version == -1 || version >= 771;
    }

    /**
     * Encode text to Base32 so it can be passed as a namespaced key.
     * @param text the raw text
     * @return the encoded text
     */
    public static String encodeText(String text) {
        if (text == null) return "";
        Base32 base32 = new Base32(false, (byte) '.');
        return base32.encodeAsString(text.getBytes()).toLowerCase();
    }

    /**
     * Decodes Base32 text.
     * @param encoded the Base32 string
     * @return the decoded text
     */
    public static String decodeText(String encoded) {
        if (encoded.isEmpty()) return encoded;
        Base32 base32 = new Base32(false, (byte) '.');
        return new String(base32.decode(encoded));
    }

}
