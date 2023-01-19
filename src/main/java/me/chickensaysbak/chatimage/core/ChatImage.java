// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core;

import me.chickensaysbak.chatimage.core.commands.ChatImageCommand;
import me.chickensaysbak.chatimage.core.commands.IgnoreImagesCommand;
import me.chickensaysbak.chatimage.core.wrappers.PluginWrapper;

public class ChatImage {

    private PluginWrapper plugin;

    public ChatImage(PluginWrapper plugin) {
        this.plugin = plugin;
        plugin.registerCommand(new ChatImageCommand(plugin));
        plugin.registerCommand(new IgnoreImagesCommand(plugin));
    }

}