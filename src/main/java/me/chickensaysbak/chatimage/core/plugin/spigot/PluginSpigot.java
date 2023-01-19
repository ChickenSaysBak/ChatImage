// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.plugin.spigot;

import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.wrappers.CommandWrapper;
import me.chickensaysbak.chatimage.core.wrappers.PluginWrapper;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class PluginSpigot extends JavaPlugin implements PluginWrapper {

    private ChatImage core;

    @Override
    public void onEnable() {

        core = new ChatImage(this);

    }

    @Override
    public void registerCommand(CommandWrapper command) {
        getCommand(command.getName()).setExecutor(new me.chickensaysbak.chatimage.core.plugin.spigot.CommandSpigot(command));
    }

    @Override
    public void sendPlayerMessage(UUID uuid, String message) {
        Player player = getServer().getPlayer(uuid);
        if (player != null) player.sendMessage(message);
    }

    @Override
    public boolean isBungee() {
        return false;
    }

}