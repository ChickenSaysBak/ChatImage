// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.velocity;

import com.velocitypowered.api.proxy.Player;
import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class PlayerVelocity implements PlayerAdapter {

    private Player player;

    PlayerVelocity(Player player) {
        this.player = player;
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getUsername();
    }

    @Override
    public boolean isOnline() {
        return player != null && player.isActive();
    }

    @Override
    public boolean hasPermission(String node) {
        return player.hasPermission(node);
    }

    @Override
    public void sendMessage(Component component) {
        if (component == null) return;
        player.sendMessage(component);
    }

    @Override
    public void sendGifFrame(Component frame) {
        // Waiting for Adventure to implement full dialog API.
    }

    @Override
    public void closeDialog() {
        player.closeDialog();
    }

    @Override
    public int getVersion() {
        return player.getProtocolVersion().getProtocol();
    }

    @Override
    public String getLocale() {
        return player.getPlayerSettings().getLocale().toString().toLowerCase();
    }

}
