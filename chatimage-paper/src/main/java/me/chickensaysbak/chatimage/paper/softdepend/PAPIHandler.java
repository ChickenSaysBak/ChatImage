// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.paper.softdepend;

import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.ImageMaker;
import me.chickensaysbak.chatimage.core.loaders.SavedImages;
import me.chickensaysbak.chatimage.core.loaders.Settings;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PAPIHandler extends PlaceholderExpansion {

    public PAPIHandler() {
        this.register();
    }

    @Override
    public String getAuthor() {
        return "ChickenSaysBak";
    }

    @Override
    public String getIdentifier() {
        return "chatimage";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String argString) {

        ChatImage ci = ChatImage.getInstance();
        SavedImages savedImages = ci.getSavedImages();
        Settings settings = ci.getSettings();
        MiniMessage mm = MiniMessage.miniMessage();

        String locale = player.isOnline()
                ? player.getPlayer().locale().toString().toLowerCase()
                : settings.getLanguageDefault();

        if (argString.startsWith("image")) {

            String[] args = argString.split(" ");
            if (args.length < 2) return null;

            String imageRef = args[1];

            if (!imageRef.startsWith("http")) {

                Component savedImage = savedImages.getImage(imageRef);

                if (savedImage == null) {
                    Component errorMsg = ci.getUIMessage("error_doesnt_exist", locale, Placeholder.unparsed("name", imageRef));
                    return mm.serialize(errorMsg);
                }

                String text = "";
                for (int i = 2; i < args.length; ++i) text += args[i].replace("\\n", "\n") + " ";

                if (!text.isEmpty()) {
                    text = text.substring(0, text.length()-1);
                    savedImage = ImageMaker.addText(savedImage, PlaceholderAPI.setBracketPlaceholders(player, text));
                }

                return mm.serialize(savedImage);

            }

            else {

                BufferedImage image = ci.loadImage(imageRef);
                if (image == null) return mm.serialize(ci.getUIMessage("error_load", locale));

                boolean smooth = settings.isSmoothRender(), trim = settings.isTrimTransparency();
                int width = settings.getMaxWidth(), height = settings.getMaxHeight();

                if (args.length >= 3) {
                    if (args[2].equalsIgnoreCase("true")) smooth = true;
                    else if (args[2].equalsIgnoreCase("false")) smooth = false;
                }

                if (args.length >= 4) {
                    if (args[3].equalsIgnoreCase("true")) trim = true;
                    else if (args[3].equalsIgnoreCase("false")) trim = false;
                }

                if (args.length >= 5) try {width = Integer.parseInt(args[4]);} catch (NumberFormatException ignored) {}
                if (args.length >= 6) try {height = Integer.parseInt(args[5]);} catch (NumberFormatException ignored) {}

                Component chatImage = ImageMaker.createChatImage(image, new Dimension(width, height), smooth, trim);

                String text = "";
                for (int i = 6; i < args.length; ++i) text += args[i].replace("\\n", "\n") + " ";

                if (!text.isEmpty()) {
                    text = text.substring(0, text.length()-1);
                    chatImage = ImageMaker.addText(chatImage, PlaceholderAPI.setBracketPlaceholders(player, text));
                }

                return mm.serialize(chatImage);

            }

        }

        return null;

    }

    public String setPlaceholders(OfflinePlayer player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

}
