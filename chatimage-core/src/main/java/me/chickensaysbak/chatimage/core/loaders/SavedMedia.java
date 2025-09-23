// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.loaders;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.GifHandler;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.media.Gif;
import me.chickensaysbak.chatimage.core.media.Image;
import me.chickensaysbak.chatimage.core.media.Media;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;

public class SavedMedia implements Loadable {

    private PluginAdapter plugin;
    private File savedDirectory;
    private HashMap<String, Media> savedMedia = new HashMap<>();

    public SavedMedia(PluginAdapter plugin) {

        this.plugin = plugin;
        savedDirectory = new File(plugin.getDataFolder(), "saved");

        reload();

    }

    /**
     * Saves media as a json file.
     * @param name the name of the media
     * @param media the media to save
     * @return true is saved successfully
     */
    public boolean saveMedia(String name, Media media) {

        try {

            File file = new File(savedDirectory, name + ".json");

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            Files.writeString(file.toPath(), media.serialize());

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
            return false;
        }

        savedMedia.put(name, media);
        return true;

    }

    /**
     * Deletes a saved media file and the parent directory if it is empty.
     * @param name the name of the media
     * @return true if deleted successfully
     */
    public boolean deleteMedia(String name) {

        File file = new File(savedDirectory, name + ".json");
        boolean successful = !file.exists() || file.delete();

        if (savedDirectory.listFiles().length == 0) savedDirectory.delete();

        if (successful) savedMedia.remove(name);
        return successful;

    }

    /**
     * Gets saved media if it exists.
     * @param name the name of the media
     * @return the saved media or null if it doesn't exist
     */
    public Media getMedia(String name) {
        return savedMedia.get(name);
    }

    /**
     * Gets all saved media names
     * @return an alphabetized list of media names
     */
    public ArrayList<String> getMediaNames() {
        ArrayList<String> names = new ArrayList<>(savedMedia.keySet());
        Collections.sort(names);
        return names;
    }

    /**
     * Loads or reloads all saved media.
     */
    @Override
    public void reload() {

        savedMedia.clear();

        MiniMessage mm = MiniMessage.miniMessage();
        convertLegacyImages(mm);
        if (!savedDirectory.exists()) return;

        for (File file : savedDirectory.listFiles()) if (file.getName().toLowerCase().endsWith(".json")) {

            String jsonStr;

            try {
                jsonStr = Files.readString(file.toPath());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
                continue;
            }

            String name = file.getName().replace(".json", "");
            JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
            Media media = null;

            if (json.has("image")) {
                Component component = mm.deserialize(json.get("image").getAsString());
                media = new Image(component);
            }

            else if (json.has("frames")) {
                GifHandler.Gif gif = ChatImage.getInstance().getGifHandler().loadSavedGif(name, json);
                if (gif != null) media = new Gif(gif);
            }

            if (media == null) continue;
            savedMedia.put(name, media);

        }

    }

    /**
     * Converts legacy BaseComponent JSON images (from versions 2.8.2 and below) to MiniMessage format.
     * @param mm existing MiniMessage instance
     */
    private void convertLegacyImages(MiniMessage mm) {

        File oldImagesDirectory = new File(plugin.getDataFolder(), "images");
        if (!oldImagesDirectory.exists()) return;

        ArrayList<File> toDelete = new ArrayList<>();

        for (File file : oldImagesDirectory.listFiles()) if (file.getName().toLowerCase().endsWith(".json")) {

            try {

                String jsonString = Files.readString(file.toPath());
                String miniMessage = mm.serialize(GsonComponentSerializer.gson().deserialize(jsonString));
                File newFile = new File(savedDirectory, file.getName());

                if (!newFile.exists()) {
                    newFile.getParentFile().mkdirs();
                    newFile.createNewFile();
                }

                JsonObject newJson = new JsonObject();
                newJson.addProperty("image", miniMessage);

                Files.writeString(newFile.toPath(), newJson.toString());
                toDelete.add(file);

            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, e.getMessage(), e);
            }

        }

        toDelete.forEach(File::delete);
        oldImagesDirectory.delete();

    }

}
