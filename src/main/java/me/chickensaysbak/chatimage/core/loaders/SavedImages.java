package me.chickensaysbak.chatimage.core.loaders;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.TextComponentSerializer;

import java.io.*;
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

            JsonElement json = new TextComponentSerializer().serialize(image, image.getClass(), null);
            File file = new File(imagesDirectory, name + ".json");

            if (!file.exists()) {
                file.mkdirs();
                file.createNewFile();
            }

            FileWriter writer = new FileWriter(file);
            writer.write(json.toString());
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        savedImages.put(name, image);
        return true;

    }

    /**
     * Deletes a saved chat image.
     * @param name the name of the image
     * @return true if deleted successfully
     */
    public boolean deleteImage(String name) {

        File file = new File(imagesDirectory, name + ".json");
        boolean successful = !file.exists() || file.delete();

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
     * Loads or reloads all saved images.
     */
    @Override
    public void reload() {

        savedImages.clear();
        if (!imagesDirectory.exists()) return;

        for (File file : imagesDirectory.listFiles()) if (file.getName().endsWith(".json")) {

            JsonObject json;

            try {
                FileReader reader = new FileReader(file);
                json = new JsonParser().parse(reader).getAsJsonObject();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            String name = file.getName().replace(".json", "");
            TextComponent image = new TextComponentSerializer().deserialize(json, TextComponent.class, null);
            if (image != null) savedImages.put(name, image);

        }

    }

}
