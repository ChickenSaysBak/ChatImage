package me.chickensaysbak.chatimage.core.commands;

import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.adapters.CommandAdapter;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.loaders.PlayerPreferences;

import java.util.List;
import java.util.UUID;

public class HideImagesCommand extends CommandAdapter {

    public HideImagesCommand(PluginAdapter plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "hideimages";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String[] getAliases() {
        return new String[] {"ignoreimages"}; // Legacy support.
    }

    @Override
    public void onCommand(UUID sender, String[] args) {

        if (sender == null) return; // Ignores console.

        ChatImage chatImage = ChatImage.getInstance();
        PlayerPreferences preferences = chatImage.getPlayerPreferences();

        if (preferences.isShowingImages(sender)) {
            preferences.setShowingImages(sender, false);
            chatImage.sendUIMessage(sender, "hiding_images");
        } else chatImage.sendUIMessage(sender, "error_hiding");

    }

    @Override
    public List<String> onTabComplete(UUID sender, String[] args) {
        return null;
    }

}
