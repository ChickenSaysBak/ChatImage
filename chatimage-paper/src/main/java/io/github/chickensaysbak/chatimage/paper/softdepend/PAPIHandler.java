// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package io.github.chickensaysbak.chatimage.paper.softdepend;

import io.github.chickensaysbak.chatimage.core.ChatImage;
import io.github.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import io.github.chickensaysbak.chatimage.core.loaders.SavedMedia;
import io.github.chickensaysbak.chatimage.core.loaders.Settings;
import io.github.chickensaysbak.chatimage.core.media.Media;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;

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

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String argString) {

        ChatImage ci = ChatImage.getInstance();
        SavedMedia savedMedia = ci.getSavedImages();
        Settings settings = ci.getSettings();
        MiniMessage mm = MiniMessage.miniMessage();

        PlayerAdapter player = ci.getPlugin().getPlayer(offlinePlayer.getUniqueId());
        String locale = player != null ? player.getLocale() : settings.getLanguageDefault();

        if (argString.startsWith("image")) {

            String[] args = argString.split(" ");
            if (args.length < 2) return null;

            String mediaRef = args[1];

            if (!mediaRef.startsWith("http")) {

                Media media = savedMedia.getMedia(mediaRef);

                if (media == null) {
                    Component errorMsg = ci.getUIMessage("error_doesnt_exist", locale, Placeholder.unparsed("name", mediaRef));
                    return mm.serialize(errorMsg);
                }

                String text = "";
                for (int i = 2; i < args.length; ++i) text += args[i].replace("\\n", "\n") + " ";
                if (!text.isEmpty()) text = text.substring(0, text.length()-1);

                Component component = media.formatFor(player, text, true);
                return mm.serialize(component != null ? component : ci.getUIMessage("error_load", locale));

            }

            else {

                Boolean smooth = null, trim = null;
                Integer width = null, height = null;

                if (args.length >= 3) {
                    if (args[2].equalsIgnoreCase("true")) smooth = true;
                    else if (args[2].equalsIgnoreCase("false")) smooth = false;
                }

                if (args.length >= 4) {
                    if (args[3].equalsIgnoreCase("true")) trim = true;
                    else if (args[3].equalsIgnoreCase("false")) trim = false;
                }

                if (args.length >= 5) try {width = Integer.parseInt(args[4]);} catch (NumberFormatException ignored) {}
                if (args.length >= 6) try {height = Integer.parseInt(args[5]);} catch (NumberFormatException ignored) {}

                Media media = ci.loadMedia(mediaRef, width, height, smooth, trim, false);
                if (media == null) return mm.serialize(ci.getUIMessage("error_load", locale));

                String text = "";
                for (int i = 6; i < args.length; ++i) text += args[i].replace("\\n", "\n") + " ";
                if (!text.isEmpty()) text = text.substring(0, text.length()-1);

                Component component = media.formatFor(player, text, true);
                return mm.serialize(component != null ? component : ci.getUIMessage("error_load", locale));

            }

        }

        return null;

    }

    public String setPlaceholders(OfflinePlayer player, String text, boolean brackets) {
        return brackets ? PlaceholderAPI.setBracketPlaceholders(player, text) : PlaceholderAPI.setPlaceholders(player, text);
    }

}
