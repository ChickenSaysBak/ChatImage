package me.chickensaysbak.chatimage.core.plugin.bungee;

import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class PlayerBungee implements PlayerAdapter {

    private ProxiedPlayer player;

    PlayerBungee(ProxiedPlayer player) {
        this.player = player;
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public boolean isOnline() {
        return player != null && player.isConnected();
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public void sendMessage(BaseComponent component) {
        player.sendMessage(component);
    }

    @Override
    public int getVersion() {
        return player.getPendingConnection().getVersion();
    }

}
