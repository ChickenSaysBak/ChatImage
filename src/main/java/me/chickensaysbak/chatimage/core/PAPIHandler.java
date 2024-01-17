package me.chickensaysbak.chatimage.core;

import me.chickensaysbak.chatimage.core.loaders.SavedImages;
import me.chickensaysbak.chatimage.core.loaders.Settings;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Collections;

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

        if (argString.startsWith("image")) {

            ChatImage chatImage = ChatImage.getInstance();
            SavedImages savedImages = chatImage.getSavedImages();

            String[] args = argString.split(" ");
            if (args.length < 2) return null;

            String imageRef = args[1];

            if (!imageRef.startsWith("http")) {

                TextComponent savedImage = savedImages.getImage(imageRef);
                if (savedImage == null) return chatImage.getUIMessage("error_doesnt_exist", Collections.singletonMap("name", imageRef));

                String text = "";
                for (int i = 2; i < args.length; ++i) text += args[i].replace("\\n", "\n") + " ";

                if (!text.isEmpty()) {
                    text = text.substring(0, text.length()-1);
                    savedImage = ImageMaker.addText(savedImage, PlaceholderAPI.setBracketPlaceholders(player, text));
                }

                return TextComponent.toLegacyText(savedImage);

            }

            else {

                BufferedImage image = chatImage.loadImage(imageRef);
                if (image == null) return chatImage.getUIMessage("error_load");

                Settings settings = chatImage.getSettings();
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

                TextComponent component = ImageMaker.createChatImage(image, new Dimension(width, height), smooth, trim);

                String text = "";
                for (int i = 6; i < args.length; ++i) text += args[i].replace("\\n", "\n") + " ";

                if (!text.isEmpty()) {
                    text = text.substring(0, text.length()-1);
                    component = ImageMaker.addText(component, PlaceholderAPI.setBracketPlaceholders(player, text));
                }

                return TextComponent.toLegacyText(component);

            }

        }

        return null;

    }

    public String setPlaceholders(OfflinePlayer player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

}
