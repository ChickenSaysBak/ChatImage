package me.chickensaysbak.chatimage.core;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class PAPIHandler extends PlaceholderExpansion {

    public PAPIHandler() {
        this.register();
    }

    @Override
    public String getAuthor() {
        return "ChickenSaysBak";
    }

    @Override
    public String getIdentifier() {
        return "chatimage";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    public String setPlaceholders(Player player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

}
