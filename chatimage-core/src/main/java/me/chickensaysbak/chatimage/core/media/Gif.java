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

import static net.kyori.adventure.text.Component.empty;

public class Gif implements Media {

    private GifHandler.Gif gif;

    public Gif(GifHandler.Gif gif) {
        this.gif = gif;
    }

    @Override
    public Component formatFor(PlayerAdapter player, String text, boolean placeholder) {

        if (!isVersionCompatible(player.getVersion())) return null;

        Settings settings = ChatImage.getInstance().getSettings();
        MiniMessage mm = MiniMessage.miniMessage();
        String locale = player.getLocale();

        String showGifMsg = settings.getMessage("show_gif", locale);
        Component showGif = mm.deserialize(showGifMsg != null ? showGifMsg : "<green>[Show GIF]");

        String playGifMsg = settings.getMessage("play_gif", locale);
        Component playGif = playGifMsg != null ? mm.deserialize(playGifMsg) : empty();

        return showGif
                .hoverEvent(HoverEvent.showText(playGif))
                .clickEvent(ClickEvent.custom(
                        Key.key("chatimage:open_gif_" + gif.getID()),
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

}
