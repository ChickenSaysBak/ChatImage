// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package io.github.chickensaysbak.chatimage.core.commands;

import io.github.chickensaysbak.chatimage.core.ChatImage;
import io.github.chickensaysbak.chatimage.core.adapters.CommandAdapter;
import io.github.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import io.github.chickensaysbak.chatimage.core.loaders.PlayerPreferences;

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
        return List.of();
    }

}
