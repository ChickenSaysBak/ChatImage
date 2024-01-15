// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.commands;

import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.ImageMaker;
import me.chickensaysbak.chatimage.core.adapters.CommandAdapter;
import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.loaders.SavedImages;
import me.chickensaysbak.chatimage.core.loaders.Settings;
import net.md_5.bungee.api.chat.TextComponent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ChatImageCommand extends CommandAdapter {

    public ChatImageCommand(PluginAdapter plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "chatimage";
    }

    @Override
    public String getPermission() {
        return "chatimage.admin";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public void onCommand(UUID sender, String[] args) {

        ChatImage chatImage = ChatImage.getInstance();
        SavedImages savedImages = chatImage.getSavedImages();

        if (args.length < 1) {
            chatImage.sendUIMessage(sender, "chatimage_usage");
            return;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            chatImage.reload();
            chatImage.sendUIMessage(sender, "reload_config");
        }

        else if (args[0].equalsIgnoreCase("send")) {

            if (args.length < 3) {
                chatImage.sendUIMessage(sender, "chatimage_send_usage");
                chatImage.sendUIMessage(sender, "chatimage_send_usage_2");
                return;
            }

            PlayerAdapter recipient = getPlugin().getPlayer(args[1]);

            if (recipient == null && !args[1].equalsIgnoreCase("all")) {
                chatImage.sendUIMessage(sender, "error_player_offline");
                return;
            }

            String imageRef = args[2];

            if (!imageRef.startsWith("http")) {

                TextComponent savedImage = savedImages.getImage(imageRef);

                if (savedImage == null) {
                    chatImage.sendUIMessage(sender, "error_doesnt_exist", Collections.singletonMap("name", imageRef));
                    return;
                }

                String text = "";
                for (int i = 3; i < args.length; ++i) text += args[i].replace("\\n", "\n") + " ";
                if (!text.isEmpty()) text = text.substring(0, text.length()-1);

                sendImage(savedImage, text, recipient, sender);

            }

            else getPlugin().runAsyncTaskLater(() -> {

                BufferedImage image = chatImage.loadImage(imageRef);

                if (image == null) {
                    chatImage.sendUIMessage(sender, "error_load");
                    return;
                }

                Settings settings = chatImage.getSettings();
                boolean smooth = settings.isSmoothRender(), trim = settings.isTrimTransparency();
                int width = settings.getMaxWidth(), height = settings.getMaxHeight();

                if (args.length >= 4) {
                    if (args[3].equalsIgnoreCase("true")) smooth = true;
                    else if (args[3].equalsIgnoreCase("false")) smooth = false;
                }

                if (args.length >= 5) {
                    if (args[4].equalsIgnoreCase("true")) trim = true;
                    else if (args[4].equalsIgnoreCase("false")) trim = false;
                }

                if (args.length >= 6) try {width = Integer.parseInt(args[5]);} catch (NumberFormatException ignored) {}
                if (args.length >= 7) try {height = Integer.parseInt(args[6]);} catch (NumberFormatException ignored) {}

                TextComponent component = ImageMaker.createChatImage(image, new Dimension(width, height), smooth, trim);

                String text = "";
                for (int i = 7; i < args.length; ++i) text += args[i].replace("\\n", "\n") + " ";
                if (!text.isEmpty()) text = text.substring(0, text.length()-1);

                sendImage(component, text, recipient, sender);

            }, 0);

        }

        else if (args[0].equalsIgnoreCase("save")) {

            if (args.length < 3) {
                chatImage.sendUIMessage(sender, "chatimage_save_usage");
                return;
            }

            String name = args[1];

            if (name.startsWith("http")) {
                chatImage.sendUIMessage(sender, "error_order");
                return;
            }

            if (savedImages.getImage(name) != null) {
                chatImage.sendUIMessage(sender, "error_already_exists", Collections.singletonMap("name", name));
                return;
            }

            getPlugin().runAsyncTaskLater(() -> {

                BufferedImage image = chatImage.loadImage(args[2]);

                if (image == null) {
                    chatImage.sendUIMessage(sender, "error_load");
                    return;
                }

                Settings settings = chatImage.getSettings();
                boolean smooth = settings.isSmoothRender(), trim = settings.isTrimTransparency();
                int width = settings.getMaxWidth(), height = settings.getMaxHeight();

                if (args.length >= 4) {
                    if (args[3].equalsIgnoreCase("true")) smooth = true;
                    else if (args[3].equalsIgnoreCase("false")) smooth = false;
                }

                if (args.length >= 5) {
                    if (args[4].equalsIgnoreCase("true")) trim = true;
                    else if (args[4].equalsIgnoreCase("false")) trim = false;
                }

                if (args.length >= 6) try {width = Integer.parseInt(args[5]);} catch (NumberFormatException ignored) {}
                if (args.length >= 7) try {height = Integer.parseInt(args[6]);} catch (NumberFormatException ignored) {}

                TextComponent component = ImageMaker.createChatImage(image, new Dimension(width, height), smooth, trim);

                if (savedImages.saveImage(name, component)) chatImage.sendUIMessage(sender, "image_saved", Collections.singletonMap("name", name));
                else chatImage.sendUIMessage(sender, "error_save");

            }, 0);

        }

        else if (args[0].equalsIgnoreCase("delete")) {

            if (args.length < 2) {
                chatImage.sendUIMessage(sender, "chatimage_delete_usage");
                return;
            }

            String name = args[1];

            if (savedImages.getImage(name) == null) {
                chatImage.sendUIMessage(sender, "error_doesnt_exist", Collections.singletonMap("name", name));
                return;
            }

            if (savedImages.deleteImage(name)) chatImage.sendUIMessage(sender, "image_deleted", Collections.singletonMap("name", name));
            else chatImage.sendUIMessage(sender, "error_delete");

        } else chatImage.sendUIMessage(sender, "chatimage_usage");

    }

    @Override
    public List<String> onTabComplete(UUID sender, String[] args) {

        ChatImage chatImage = ChatImage.getInstance();
        Settings settings = chatImage.getSettings();
        SavedImages savedImages = chatImage.getSavedImages();
        List<String> result = new ArrayList<>();
        boolean usingURL = args.length >= 3 && args[2].startsWith("http");

        if (args.length == 1) result.addAll(Arrays.asList("reload", "send", "save", "delete"));

        else if (args[0].equalsIgnoreCase("send")) switch (args.length) {

            case 2:
                List<String> names = getPlugin().getOnlinePlayers().stream().map(PlayerAdapter::getName).collect(Collectors.toList());
                names.add("all");
                for (String name : names) if (name.toLowerCase().startsWith(args[1].toLowerCase())) result.add(name);
                break;

            case 3:
                for (String name : savedImages.getImageNames())
                    if (name.toLowerCase().startsWith(args[2].toLowerCase())) result.add(name);
                break;

            case 4:
            case 5:
                if (usingURL) result.addAll(Arrays.asList("default", "true", "false"));
                break;

            case 6:
                if (usingURL) result.addAll(Arrays.asList("default", settings.getMaxWidth() + ""));
                break;

            case 7:
                if (usingURL) result.addAll(Arrays.asList("default", settings.getMaxHeight() + ""));
                break;

        }

        else if (args[0].equalsIgnoreCase("save")) switch (args.length) {

            case 4:
            case 5:
                result.addAll(Arrays.asList("default", "true", "false"));
                break;

            case 6:
                result.addAll(Arrays.asList("default", settings.getMaxWidth() + ""));
                break;

            case 7:
                result.addAll(Arrays.asList("default", settings.getMaxHeight() + ""));
                break;

        }

        else if (args[0].equalsIgnoreCase("delete")) switch (args.length) {

            case 2:
                for (String name : savedImages.getImageNames())
                    if (name.toLowerCase().startsWith(args[1].toLowerCase())) result.add(name);
                break;

        }

        return result;

    }

    /**
     * Handles the logic of sending an image to one or more players. Sets placeholders if PlaceholderAPI is enabled.
     * @param image the image to send as a component
     * @param text optional text to append to the image
     * @param recipient the recipient or null to send to all players
     * @param sender the sender of the send command
     */
    private void sendImage(TextComponent image, String text, PlayerAdapter recipient, UUID sender) {

        ChatImage chatImage = ChatImage.getInstance();
        boolean hasText = text != null && !text.isEmpty();

        // Send all
        if (recipient == null) {

            for (PlayerAdapter p : getPlugin().getOnlinePlayers()) {
                if (hasText) image = ImageMaker.addText(image, getPlugin().setPlaceholders(p.getUniqueId(), text));
                p.sendMessage(image);
            }

            chatImage.sendUIMessage(sender, "image_sent_all");

        }

        else {

            if (recipient.isOnline()) {
                if (hasText) image = ImageMaker.addText(image, getPlugin().setPlaceholders(recipient.getUniqueId(), text));
                recipient.sendMessage(image);
            }

            if (sender != null && !sender.equals(recipient.getUniqueId())) chatImage.sendUIMessage(sender, "image_sent");

        }

    }

}
