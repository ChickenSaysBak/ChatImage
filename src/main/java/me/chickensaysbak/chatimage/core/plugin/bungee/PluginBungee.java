// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.plugin.bungee;

import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.wrappers.CommandWrapper;
import me.chickensaysbak.chatimage.core.wrappers.PluginWrapper;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.UUID;

public class PluginBungee extends Plugin implements PluginWrapper {

    private ChatImage core;

    @Override
    public void onEnable() {

        core = new ChatImage(this);

    }

    @Override
    public void registerCommand(CommandWrapper command) {
        getProxy().getPluginManager().registerCommand(this, new CommandBungee(command));
    }

    @Override
    public void sendPlayerMessage(UUID uuid, String message) {
        ProxiedPlayer player = getProxy().getPlayer(uuid);
        if (player != null) player.sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public boolean isBungee() {
        return true;
    }

}