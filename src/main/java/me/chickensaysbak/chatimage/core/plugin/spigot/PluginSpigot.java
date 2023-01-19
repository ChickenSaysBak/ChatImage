// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.plugin.spigot;

import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.wrappers.CommandWrapper;
import me.chickensaysbak.chatimage.core.wrappers.PluginWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class PluginSpigot extends JavaPlugin implements Listener, PluginWrapper {

    private ChatImage core;

    @Override
    public void onEnable() {

        core = new ChatImage(this);
        getServer().getPluginManager().registerEvents(this, this);

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        boolean cancelEvent = core.onChat(event.getPlayer().getUniqueId(), event.getMessage());
        event.setCancelled(cancelEvent);
    }

    @Override
    public void registerCommand(CommandWrapper command) {
        getCommand(command.getName()).setExecutor(new CommandSpigot(command));
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