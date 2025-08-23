package me.chickensaysbak.chatimage.core.loaders;

import com.google.gson.JsonParseException;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class SavedImages implements Loadable {

    private PluginAdapter plugin;
    private File imagesDirectory;
    private HashMap<String, TextComponent> savedImages = new HashMap<>();

    public SavedImages(PluginAdapter plugin) {

        this.plugin = plugin;
        imagesDirectory = new File(plugin.getDataFolder(), "images");

        reload();

    }

    /**
     * Saves a chat image as a json file.
     * @param name the name of the image
     * @param image the chat image
     * @return true if saved successfully
     */
    public boolean saveImage(String name, TextComponent image) {

        try {

            File file = new File(imagesDirectory, name + ".json");

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            Files.writeString(file.toPath(), ComponentSerializer.toString(image));

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        savedImages.put(name, image);
        return true;

    }

    /**
     * Deletes a saved chat image and the parent directory if it is empty.
     * @param name the name of the image
     * @return true if deleted successfully
     */
    public boolean deleteImage(String name) {

        File file = new File(imagesDirectory, name + ".json");
        boolean successful = !file.exists() || file.delete();

        if (imagesDirectory.listFiles().length == 0) imagesDirectory.delete();

        if (successful) savedImages.remove(name);
        return successful;

    }

    /**
     * Gets a saved image if it exists.
     * @param name the name of the image
     * @return the saved image or null if it doesn't exist
     */
    public TextComponent getImage(String name) {
        return savedImages.getOrDefault(name, null);
    }

    /**
     * Gets all saved image names
     * @return an alphabetized list of image names
     */
    public ArrayList<String> getImageNames() {
        ArrayList<String> names = new ArrayList<>(savedImages.keySet());
        Collections.sort(names);
        return names;
    }

    /**
     * Loads or reloads all saved images.
     */
    @Override
    public void reload() {

        savedImages.clear();
        if (!imagesDirectory.exists()) return;

        for (File file : imagesDirectory.listFiles()) if (file.getName().toLowerCase().endsWith(".json")) {

            String jsonString;

            try {
                jsonString = Files.readString(file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            BaseComponent[] components;

            try {
                components = ComponentSerializer.parse(jsonString);
            } catch (JsonParseException | UnsupportedOperationException e) {
                plugin.getLogger().warning("Could not load '" + file.getName() + "' because the file is corrupt!");
                e.printStackTrace();
                return;
            }

            TextComponent image = null;

            for (BaseComponent bc : components) if (bc instanceof TextComponent tc) {
                image = tc;
                break;
            }

            String name = file.getName().replace(".json", "");
            if (image != null) savedImages.put(name, image);

        }

    }

}
