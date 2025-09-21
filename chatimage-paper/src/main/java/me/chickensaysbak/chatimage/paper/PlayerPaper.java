package me.chickensaysbak.chatimage.paper;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.translatable;

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
        if (component == null) return;
        player.sendMessage(component);
    }

    @Override
    public void sendGifFrame(Component frame, Component text) {

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(text != null ? text : empty())
                        .body(List.of(DialogBody.plainMessage(frame)))
                        .build()
                )
                .type(DialogType.notice(
                                ActionButton.builder(translatable("mco.selectServer.close", "Close"))
                                        .action(DialogAction.customClick(Key.key("chatimage:close_gif"), null))
                                        .build()
                        )
                )
        );

        player.showDialog(dialog);

    }

    @Override
    public void closeDialog() {
        player.closeInventory(); // closeDialog() is only available in 1.21.8+
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
