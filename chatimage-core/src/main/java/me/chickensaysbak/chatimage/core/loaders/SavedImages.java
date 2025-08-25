package me.chickensaysbak.chatimage.core.loaders;

import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
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

public class SavedImages implements Loadable {

    private PluginAdapter plugin;
    private File imagesDirectory;
    private HashMap<String, Component> savedImages = new HashMap<>();

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
    public boolean saveImage(String name, Component image) {

        try {

            File file = new File(imagesDirectory, name + ".txt");

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            Files.writeString(file.toPath(), MiniMessage.miniMessage().serialize(image));

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
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

        File file = new File(imagesDirectory, name + ".txt");
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
    public Component getImage(String name) {
        return savedImages.get(name);
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

        MiniMessage mm = MiniMessage.miniMessage();
        convertLegacyImages(mm);

        for (File file : imagesDirectory.listFiles()) if (file.getName().toLowerCase().endsWith(".txt")) {

            String mmString;

            try {
                mmString = Files.readString(file.toPath());
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
                continue;
            }

            Component image = mm.deserialize(mmString);
            String name = file.getName().replace(".txt", "");
            savedImages.put(name, image);

        }

    }

    /**
     * Converts legacy BaseComponent JSON images (from versions 2.8.2 and below) to MiniMessage format.
     * @param mm existing MiniMessage instance
     */
    private void convertLegacyImages(MiniMessage mm) {

        ArrayList<File> toDelete = new ArrayList<>();

        for (File file : imagesDirectory.listFiles()) if (file.getName().toLowerCase().endsWith(".json")) {

            try {

                String jsonString = Files.readString(file.toPath());
                String miniMessage = mm.serialize(GsonComponentSerializer.gson().deserialize(jsonString));
                File newFile = new File(imagesDirectory, file.getName().replace(".json", ".txt"));

                if (!newFile.exists()) {
                    newFile.getParentFile().mkdirs();
                    newFile.createNewFile();
                }

                Files.writeString(newFile.toPath(), miniMessage);
                toDelete.add(file);

            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, e.getMessage(), e);
                continue;
            }

        }

        toDelete.forEach(File::delete);

    }

}
