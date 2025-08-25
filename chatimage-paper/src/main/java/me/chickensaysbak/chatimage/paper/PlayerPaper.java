package me.chickensaysbak.chatimage.paper;

import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerPaper implements PlayerAdapter {

    private Player player;

    PlayerPaper(Player player) {
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
        player.sendMessage(component);
    }

    @Override
    public int getVersion() {
        return player.getProtocolVersion();
    }

    @Override
    public String getLocale() {
        return player.locale().toString().toLowerCase();
    }

}
