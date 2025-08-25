// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.paper.softdepend;

import me.chickensaysbak.chatimage.core.ChatImage;
import net.essentialsx.api.v2.events.discord.DiscordRelayEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static net.kyori.adventure.text.Component.text;

public class EssXDiscordHandler implements Listener {

    @EventHandler
    public void onDiscordRelay(DiscordRelayEvent event) {

        String message = event.getFormattedMessage(); // Formatted message includes file upload links.

        boolean cancelEvent = ChatImage.getInstance().processChatLinks(text(message), event.getMember().getId(), true);
        if (cancelEvent) event.setCancelled(true);

    }

}
