// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.commands;

import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.ImageMaker;
import me.chickensaysbak.chatimage.core.Settings;
import me.chickensaysbak.chatimage.core.adapters.CommandAdapter;
import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import net.md_5.bungee.api.chat.TextComponent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
    public void onCommand(UUID sender, String[] args) {

        ChatImage chatImage = ChatImage.getInstance();

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
                return;
            }

            PlayerAdapter recipient = getPlugin().getPlayer(args[1]);

            if (recipient == null) {
                chatImage.sendUIMessage(sender, "error_player_offline");
                return;
            }

            if (chatImage.getIgnoringImages().isIgnoring(recipient.getUniqueId())) {
                chatImage.sendUIMessage(sender, "error_ignoring");
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

                String text = "";
                for (int i = 7; i < args.length; ++i) text += args[i].replace("\\n", "\n") + " ";
                if (!text.isEmpty()) text = text.substring(0, text.length()-1);

                TextComponent component = ImageMaker.createChatImage(image, new Dimension(width, height), smooth, trim);
                if (!text.isEmpty()) component = ImageMaker.addText(component, text);

                if (recipient.isOnline()) recipient.sendMessage(component);
                if (sender != null && !sender.equals(recipient.getUniqueId())) chatImage.sendUIMessage(sender, "image_sent");

            }, 0);

        } else chatImage.sendUIMessage(sender, "chatimage_usage");

    }

    @Override
    public List<String> onTabComplete(UUID sender, String[] args) {

        ChatImage chatImage = ChatImage.getInstance();
        List<String> result = new ArrayList<>();

        if (args.length == 1) result.addAll(Arrays.asList("reload", "send"));

        else if (args[0].equalsIgnoreCase("send")) switch (args.length) {

            case 2:
                for (PlayerAdapter p : getPlugin().getOnlinePlayers())
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) result.add(p.getName());
                break;

            case 4:
            case 5:
                result.addAll(Arrays.asList("default", "true", "false"));
                break;

            case 6:
                result.addAll(Arrays.asList("default", chatImage.getSettings().getMaxWidth() + ""));
                break;

            case 7:
                result.addAll(Arrays.asList("default", chatImage.getSettings().getMaxHeight() + ""));
                break;

        }

        return result;

    }

}
