// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.plugin.bungee;

import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.wrappers.CommandAdapter;
import me.chickensaysbak.chatimage.core.wrappers.PluginAdapter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.InputStream;
import java.util.UUID;

public class PluginBungee extends Plugin implements Listener, PluginAdapter {

    private ChatImage core;

    @Override
    public void onEnable() {

        core = new ChatImage(this);
        getProxy().getPluginManager().registerListener(this, this);

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(ChatEvent event) {

        Connection sender = event.getSender();
        if (event.isCancelled() || event.isCommand() || !(sender instanceof ProxiedPlayer)) return;

        ProxiedPlayer player = (ProxiedPlayer) sender;
        boolean cancelEvent = core.onChat(player.getUniqueId(), event.getMessage());
        event.setCancelled(cancelEvent);

    }

    @Override
    public void registerCommand(CommandAdapter command) {
        getProxy().getPluginManager().registerCommand(this, new CommandBungee(command));
    }

    @Override
    public InputStream getResource(String name) {
        return getResourceAsStream(name);
    }

    @Override
    public void sendPlayerMessage(UUID uuid, String message) {
        ProxiedPlayer player = getProxy().getPlayer(uuid);
        if (player != null) player.sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public void sendPlayerComponent(UUID uuid, BaseComponent component) {
        ProxiedPlayer player = getProxy().getPlayer(uuid);
        if (player != null) player.sendMessage(component);
    }

    @Override
    public boolean isBungee() {
        return true;
    }

}