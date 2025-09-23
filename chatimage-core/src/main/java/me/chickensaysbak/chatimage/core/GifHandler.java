// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.chickensaysbak.chatimage.core.adapters.PlayerAdapter;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.lib.com.madgag.gif.fmsware.GifDecoder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;
import java.util.*;
import java.util.logging.Level;

public class GifHandler {

    private PluginAdapter plugin;
    private LinkedHashMap<String, Gif> gifs = new LinkedHashMap<>();
    private HashMap<UUID, Gif> playersViewing = new HashMap<>();

    GifHandler(PluginAdapter plugin) {
        this.plugin = plugin;
    }

    public Gif getGif(int id) {
        if (gifs.size() <= id) return null;
        return new ArrayList<>(gifs.values()).get(id);
    }

    public Gif loadGif(URLConnection connection, Dimension dim, boolean smooth) {

        String url = connection.getURL().toString();
        String key = url + " " + smooth + " " + dim.getWidth() + " " + dim.getHeight();

        if (gifs.containsKey(key)) return gifs.get(key);
        boolean debug = ChatImage.getInstance().getSettings().isDebug();

        try {

            GifDecoder decoder = new GifDecoder();

            try (InputStream in = connection.getInputStream()) {

                int status = decoder.read(in);

                if (status != GifDecoder.STATUS_OK) {

                    if (debug) {
                        plugin.getLogger().warning("ChatImage Debugger - Error parsing GIF");
                        plugin.getLogger().warning("URL: " + url);
                        plugin.getLogger().warning("Status: " + status);
                    }

                    return null;

                }

            }

            ArrayList<Component> frames = new ArrayList<>();
            ArrayList<Long> tickDelays = new ArrayList<>();
            long gifDuration = 0, tickDuration = 0;

            for (int i = 0; i < decoder.getFrameCount() && i < 2000; ++i) {

                gifDuration += decoder.getDelay(i);
                long targetTickDuration = Math.round(gifDuration / 50.0);
                long tickDelay = targetTickDuration - tickDuration;
                tickDuration = targetTickDuration;
                if (tickDelay <= 0) continue; // Skips frames if they're shown faster than a 50ms tick.

                frames.add(ImageMaker.createChatImage(decoder.getFrame(i), dim, smooth, false));
                tickDelays.add(tickDelay);

            }

            if (frames.isEmpty()) return null;
            Gif gif = new Gif(gifs.size(), frames, tickDelays);
            gifs.put(key, gif);
            return gif;

        } catch (IOException | NullPointerException e) {

            if (debug) {
                plugin.getLogger().warning("ChatImage Debugger - Error loading GIF");
                plugin.getLogger().warning("URL: " + url);
                plugin.getLogger().log(Level.WARNING, e.getMessage(), e);
            }

        }

        return null;

    }

    public Gif loadSavedGif(String name, JsonObject json) {

        MiniMessage mm = MiniMessage.miniMessage();
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        GifHandler.SerializableGif sGif = gson.fromJson(json, GifHandler.SerializableGif.class);

        List<Component> frames = sGif.frames.stream().map(mm::deserialize).toList();
        if (frames.isEmpty()) return null;

        Gif gif = new Gif(gifs.size(), frames, sGif.tickDelays);
        gifs.put(name, gif);
        return gif;

    }

    public void playGif(PlayerAdapter player, Gif gif, Component text) {
        if (gif == null) return;
        playersViewing.put(player.getUniqueId(), gif);
        gif.play(player, 0, text);
    }

    public void closeGif(PlayerAdapter player) {
        playersViewing.remove(player.getUniqueId());
        plugin.runTaskLater(player::closeDialog, 1);
    }

    public class Gif {

        private int id;
        private List<Component> frames;
        private List<Long> tickDelays;

        Gif(int id, List<Component> frames, List<Long> tickDelays) {
            this.id = id;
            this.frames = frames;
            this.tickDelays = tickDelays;
        }

        public int getID() {
            return id;
        }

        void play(PlayerAdapter player, int frameNum, Component text) {

            if (!this.equals(playersViewing.get(player.getUniqueId()))) return;
            player.sendGifFrame(frames.get(frameNum), text);

            int nextFrameNum = frameNum < frames.size() - 1 ? frameNum + 1 : 0;
            plugin.runAsyncTaskLater(() -> play(player, nextFrameNum, text), tickDelays.get(frameNum));

        }

        public String toJson() {
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            return gson.toJson(new SerializableGif(frames, tickDelays));
        }

    }

    public class SerializableGif {

        private List<String> frames;
        private List<Long> tickDelays;

        SerializableGif(List<Component> frames, List<Long> tickDelays) {
            MiniMessage mm = MiniMessage.miniMessage();
            this.frames = frames.stream().map(mm::serialize).toList();
            this.tickDelays = tickDelays;
        }

    }

}
