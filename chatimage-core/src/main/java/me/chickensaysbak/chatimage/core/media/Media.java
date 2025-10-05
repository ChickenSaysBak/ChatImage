package me.chickensaysbak.chatimage.core.media;

import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import net.kyori.adventure.text.Component;

public interface Media {

    /**
     * Formats this media with UI messages translated to the player's locale.
     * @param player the recipient player
     * @param text optional text to be formatted with placeholders
     * @param placeholder true if this media is being formatted inside a PAPI placeholder
     * @return the formatted media component or null if the player's version is incompatible to view it.
     */
    Component formatFor(PlayerAdapter player, String text, boolean placeholder);

    /**
     * Converts this media into a JSON string format.
     * @return the serialized media as a JSON string
     */
    String serialize();

}
