// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package io.github.chickensaysbak.chatimage.core.commands;

import io.github.chickensaysbak.chatimage.core.ChatImage;
import io.github.chickensaysbak.chatimage.core.adapters.CommandAdapter;
import io.github.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import io.github.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import io.github.chickensaysbak.chatimage.core.loaders.SavedMedia;
import io.github.chickensaysbak.chatimage.core.loaders.Settings;
import io.github.chickensaysbak.chatimage.core.media.Media;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
        SavedMedia savedMedia = chatImage.getSavedImages();

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

            String mediaRef = args[2];

            if (!mediaRef.startsWith("http")) {

                Media media = savedMedia.getMedia(mediaRef);

                if (media == null) {
                    chatImage.sendUIMessage(sender, "error_doesnt_exist", Placeholder.unparsed("name", mediaRef));
                    return;
                }

                String text = "";
                for (int i = 3; i < args.length; ++i) text += args[i].replace("\\n", "\n") + " ";
                if (!text.isEmpty()) text = text.substring(0, text.length()-1);

                sendMedia(media, text, recipient, sender);

            }

            else getPlugin().runAsyncTaskLater(() -> {

                Boolean smooth = null, trim = null;
                Integer width = null, height = null;

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

                Media media = chatImage.loadMedia(mediaRef, width, height, smooth, trim, false);

                if (media == null) {
                    chatImage.sendUIMessage(sender, "error_load");
                    return;
                }

                String text = "";
                for (int i = 7; i < args.length; ++i) text += args[i].replace("\\n", "\n") + " ";
                if (!text.isEmpty()) text = text.substring(0, text.length()-1);

                sendMedia(media, text, recipient, sender);

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

            if (savedMedia.getMedia(name) != null) {
                chatImage.sendUIMessage(sender, "error_already_exists", Placeholder.unparsed("name", name));
                return;
            }

            getPlugin().runAsyncTaskLater(() -> {

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

                Media media = chatImage.loadMedia(args[2], width, height, smooth, trim, false);

                if (media == null) {
                    chatImage.sendUIMessage(sender, "error_load");
                    return;
                }

                if (savedMedia.saveMedia(name, media)) chatImage.sendUIMessage(sender, "image_saved", Placeholder.unparsed("name", name));
                else chatImage.sendUIMessage(sender, "error_save");

            }, 0);

        }

        else if (args[0].equalsIgnoreCase("delete")) {

            if (args.length < 2) {
                chatImage.sendUIMessage(sender, "chatimage_delete_usage");
                return;
            }

            String name = args[1];

            if (savedMedia.getMedia(name) == null) {
                chatImage.sendUIMessage(sender, "error_doesnt_exist", Placeholder.unparsed("name", name));
                return;
            }

            if (savedMedia.deleteMedia(name)) chatImage.sendUIMessage(sender, "image_deleted", Placeholder.unparsed("name", name));
            else chatImage.sendUIMessage(sender, "error_delete");

        } else chatImage.sendUIMessage(sender, "chatimage_usage");

    }

    @Override
    public List<String> onTabComplete(UUID sender, String[] args) {

        ChatImage chatImage = ChatImage.getInstance();
        Settings settings = chatImage.getSettings();
        SavedMedia savedMedia = chatImage.getSavedImages();
        List<String> result = new ArrayList<>();
        boolean usingURL = args.length >= 3 && args[2].startsWith("http");

        if (args.length < 1) return result;
        else if (args.length == 1) result.addAll(Arrays.asList("reload", "send", "save", "delete"));

        else if (args[0].equalsIgnoreCase("send")) switch (args.length) {

            case 2 -> {

                List<String> names = getPlugin().getOnlinePlayers().stream().map(PlayerAdapter::getName).collect(Collectors.toList());
                names.add("all");

                names.stream()
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .forEach(result::add);

            }

            case 3 -> {

                savedMedia.getMediaNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .forEach(result::add);

            }

            case 4, 5 -> {
                if (usingURL) result.addAll(Arrays.asList("default", "true", "false"));
            }

            case 6 -> {
                if (usingURL) result.addAll(Arrays.asList("default", settings.getMaxWidth() + ""));
            }

            case 7 -> {
                if (usingURL) result.addAll(Arrays.asList("default", settings.getMaxHeight() + ""));
            }

        }

        else if (args[0].equalsIgnoreCase("save")) switch (args.length) {
            case 4, 5 -> result.addAll(Arrays.asList("default", "true", "false"));
            case 6 -> result.addAll(Arrays.asList("default", settings.getMaxWidth() + ""));
            case 7 -> result.addAll(Arrays.asList("default", settings.getMaxHeight() + ""));
        }

        else if (args[0].equalsIgnoreCase("delete")) switch (args.length) {

            case 2 -> {

                savedMedia.getMediaNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .forEach(result::add);

            }

        }

        return result;

    }

    /**
     * Handles the logic of sending media to one or more players.
     * @param media the media to send as a component
     * @param text optional text to append to the media
     * @param recipient the recipient or null to send to all players
     * @param sender the sender of the send command
     */
    private void sendMedia(Media media, String text, PlayerAdapter recipient, UUID sender) {

        ChatImage chatImage = ChatImage.getInstance();

        // Send all
        if (recipient == null) {
            getPlugin().getOnlinePlayers().forEach(p -> p.sendMessage(media.formatFor(p, text, false)));
            if (sender != null) chatImage.sendUIMessage(sender, "image_sent_all");
        }

        else {

            if (recipient.isOnline()) {

                Component component = media.formatFor(recipient, text, false);

                if (component == null) {
                    chatImage.sendUIMessage(sender, "error_outdated");
                    return;
                }

                recipient.sendMessage(component);

            }

            if (sender != null && !sender.equals(recipient.getUniqueId())) chatImage.sendUIMessage(sender, "image_sent");

        }

    }

}
