// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.commands;

import me.chickensaysbak.chatimage.core.wrappers.CommandWrapper;
import me.chickensaysbak.chatimage.core.wrappers.PluginWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IgnoreImagesCommand extends CommandWrapper {

    public IgnoreImagesCommand(PluginWrapper plugin) {
        super(plugin);
    }

    @Override
    public String getName() {
        return "ignoreimages";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public void onCommand(UUID sender, String[] args) {

    }

    @Override
    public List<String> onTabComplete(UUID sender, String[] args) {
        List<String> result = new ArrayList<>();
        return result;
    }

}