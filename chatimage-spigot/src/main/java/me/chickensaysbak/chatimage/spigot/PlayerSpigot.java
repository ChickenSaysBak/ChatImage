// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.spigot;

import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerSpigot implements PlayerAdapter {

    private Player player;

    PlayerSpigot(Player player) {
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
        return player != null && player.isOnline();
    }

    @Override
    public boolean hasPermission(String node) {
        return player.hasPermission(node);
    }

    @Override
    public void sendMessage(Component component) {
        if (component == null) return;
        player.spigot().sendMessage(BungeeComponentSerializer.get().serialize(component));
    }

    @Override
    public int getVersion() {
        return -1;
    }

    @Override
    public String getLocale() {
        return player.getLocale();
    }

}
