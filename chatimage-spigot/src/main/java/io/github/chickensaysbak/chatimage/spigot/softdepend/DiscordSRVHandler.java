// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package io.github.chickensaysbak.chatimage.spigot.softdepend;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import io.github.chickensaysbak.chatimage.core.ChatImage;

import static net.kyori.adventure.text.Component.text;

public class DiscordSRVHandler {

    public DiscordSRVHandler() {
        DiscordSRV.api.subscribe(this);
    }

    public void unsubscribe() {
        DiscordSRV.api.unsubscribe(this);
    }

    @Subscribe
    public void discordMessageProcessed(DiscordGuildMessagePostProcessEvent event) {

        String message = event.getMessage().getContentStripped();

        // Supports file uploads.
        for (Message.Attachment att : event.getMessage().getAttachments()) if (att.isImage()) {
            message = att.getUrl();
            break;
        }

        boolean cancelEvent = ChatImage.getInstance().processChatLinks(text(message), event.getAuthor().getId(), true);
        if (cancelEvent) event.setCancelled(true);

    }

}
