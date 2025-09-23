// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.media;

import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.ImageMaker;
import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import me.chickensaysbak.chatimage.core.loaders.Settings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.awt.*;
import java.awt.image.BufferedImage;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.textOfChildren;

public class HidableImage extends Image {

    private String url;
    private Component hiddenImage;

    public HidableImage(String url, BufferedImage image) {

        this.url = url;

        Settings settings = ChatImage.getInstance().getSettings();
        boolean smooth = settings.isSmoothRender(), trim = settings.isTrimTransparency();
        Dimension expandedDim = new Dimension(settings.getMaxWidth(), settings.getMaxHeight());
        Dimension hiddenDim = new Dimension(settings.getMaxHiddenWidth(), settings.getMaxHiddenHeight());

        expandedImage = ImageMaker.createChatImage(image, expandedDim, smooth, trim);
        hiddenImage = ImageMaker.createChatImage(image, hiddenDim, smooth, trim);

    }

    @Override
    public Component formatFor(PlayerAdapter player, String text, boolean placeholder) {

        if (player != null && !isVersionCompatible(player.getVersion())) return null;

        String locale = player != null ? player.getLocale() : ChatImage.getInstance().getSettings().getLanguageDefault();
        boolean showing = ChatImage.getInstance().getPlayerPreferences().isShowingImages(player.getUniqueId());
        return showing ? formatExpandedImage(locale) : formatHiddenImage(locale);

    }

    /**
     * Gets the URL string of the original image.
     * @return the URL string
     */
    public String getUrl() {
        return url;
    }

    /**
     * Formats the expanded image so it can be clicked on to open and contains a tip about /hideimages.
     * @param locale the locale of the recipient
     * @return the expanded image
     */
    private Component formatExpandedImage(String locale) {

        MiniMessage mm = MiniMessage.miniMessage();
        String hoverTip = ChatImage.getInstance().getSettings().getMessage("hover_tip", locale);

        Component result = textOfChildren(expandedImage);

        if (hoverTip != null) {
            Component tip = mm.deserialize(hoverTip);
            result = result.hoverEvent(HoverEvent.showText(tip));
        }

        return result.clickEvent(ClickEvent.openUrl(url));

    }

    /**
     * Formats the hidden image so it can be clicked on to open and hovered over to view.
     * @param locale the locale of the recipient
     * @return the hidden image
     */
    private Component formatHiddenImage(String locale) {

        MiniMessage mm = MiniMessage.miniMessage();
        String showImageMsg = ChatImage.getInstance().getSettings().getMessage("show_image", locale);
        Component showImage = mm.deserialize(showImageMsg != null ? showImageMsg : "<green>[Show Image]");
        Component hoverImage = newline().append(hiddenImage); // Newline prevents odd spacing.

        return showImage
                .hoverEvent(HoverEvent.showText(hoverImage))
                .clickEvent(ClickEvent.openUrl(url));

    }

}
