package me.chickensaysbak.chatimage.core.plugin.spigot;

import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import net.md_5.bungee.api.chat.BaseComponent;
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
    public void sendMessage(String message) {
        player.sendMessage(message);
    }

    @Override
    public void sendMessage(BaseComponent component) {
        player.spigot().sendMessage(component);
    }

    @Override
    public int getVersion() {
        return -1;
    }

}
