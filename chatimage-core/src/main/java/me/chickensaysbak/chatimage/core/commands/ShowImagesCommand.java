package me.chickensaysbak.chatimage.core.commands;

import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.loaders.PlayerPreferences;
import me.chickensaysbak.chatimage.core.adapters.CommandAdapter;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;

import java.util.List;
import java.util.UUID;

public class ShowImagesCommand extends CommandAdapter {

    public ShowImagesCommand(PluginAdapter plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "showimages";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public void onCommand(UUID sender, String[] args) {

        if (sender == null) return; // Ignores console.

        ChatImage chatImage = ChatImage.getInstance();
        PlayerPreferences preferences = chatImage.getPlayerPreferences();

        if (!preferences.isShowingImages(sender)) {
            preferences.setShowingImages(sender, true);
            chatImage.sendUIMessage(sender, "showing_images");
        } else chatImage.sendUIMessage(sender, "error_showing");

    }

    @Override
    public List<String> onTabComplete(UUID sender, String[] args) {
        return List.of();
    }

}
