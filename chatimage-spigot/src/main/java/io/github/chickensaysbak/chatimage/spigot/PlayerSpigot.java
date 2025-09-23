// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package io.github.chickensaysbak.chatimage.spigot;

import io.github.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.dialog.Dialog;
import net.md_5.bungee.api.dialog.DialogBase;
import net.md_5.bungee.api.dialog.NoticeDialog;
import net.md_5.bungee.api.dialog.action.ActionButton;
import net.md_5.bungee.api.dialog.action.CustomClickAction;
import net.md_5.bungee.api.dialog.body.PlainMessageBody;
import org.bukkit.entity.Player;

import java.util.List;
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
    public void sendGifFrame(Component frame, Component text) {

        TextComponent frameTC = new TextComponent(BungeeComponentSerializer.get().serialize(frame));
        TextComponent textTC = text != null ? new TextComponent(BungeeComponentSerializer.get().serialize(text)) : new TextComponent();

        TranslatableComponent closeText = new TranslatableComponent("mco.selectServer.close");
        closeText.setFallback("Close");

        Dialog dialog = new NoticeDialog(
                new DialogBase(textTC).body(List.of(new PlainMessageBody(frameTC))),
                new ActionButton(closeText, new CustomClickAction("chatimage:close_gif"))
        );

        player.showDialog(dialog);

    }

    @Override
    public void closeDialog() {
        player.clearDialog();
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
