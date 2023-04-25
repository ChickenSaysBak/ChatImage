// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core;

import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.adapters.YamlAdapter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class PlayerPreferences {

    private PluginAdapter plugin;
    private File preferencesFile;
    private HashMap<UUID, Boolean> preferences = new HashMap<>();
    private int saveTimeRemaining = 0;

    PlayerPreferences(PluginAdapter plugin) {

        this.plugin = plugin;
        preferencesFile = new File(plugin.getDataFolder(), "player_preferences.yml");

        reload();
        convertIgnoringFile();

    }

    /**
     * Considers a player showing or hiding images.
     * If changes are successfully made, the file will be queued to save.
     * @param uuid the uuid of the player
     * @param showImages true if showing images; false if hiding images
     */
    public void setShowingImages(UUID uuid, boolean showImages) {

        boolean autoHide = ChatImage.getInstance().getSettings().isAutoHide();

        // Ignores preference if it aligns with the default setting.
        if (showImages == autoHide) {
            preferences.put(uuid, showImages);
        } else preferences.remove(uuid);

        queueSave();

    }

    /**
     * Check whether a player is showing images or not.
     * @param uuid the uuid of the player
     * @return true if the player is showing images
     */
    public boolean isShowingImages(UUID uuid) {
        boolean autoHide = ChatImage.getInstance().getSettings().isAutoHide();
        return preferences.getOrDefault(uuid, !autoHide);
    }

    /**
     * Loads or reloads players' preferences.
     */
    public void reload() {

        preferences.clear();
        if (!preferencesFile.exists()) return;
        YamlAdapter preferencesYaml = plugin.loadYaml(preferencesFile);
        boolean autoHide = ChatImage.getInstance().getSettings().isAutoHide();

        for (String uuidString : preferencesYaml.getKeys("preferences")) {

            try {
                UUID uuid = UUID.fromString(uuidString);
                preferences.put(uuid, preferencesYaml.getBoolean("preferences." + uuidString, !autoHide));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Check if saving is scheduled to occur.
     * @return true if saving is queued
     */
    public boolean isSaveQueued() {
        return saveTimeRemaining > 0;
    }

    /**
     * Saves the preferences file after at least 5 seconds or longer if changes are made before saving.
     * This prevents players from causing potential lag from spamming the hide/show images commands.
     */
    public void queueSave() {
        boolean startTimer = !isSaveQueued();
        saveTimeRemaining = 5;
        if (startTimer) queueSaveTimer();
    }

    /**
     * Decrements the time remaining until save and then saves the file.
     */
    private void queueSaveTimer() {

        plugin.runAsyncTaskLater(() -> {

            if (isSaveQueued()) {
                --saveTimeRemaining;
                queueSaveTimer();
            } else saveFile();

        }, 20);

    }

    /**
     * Updates the preferences file to contain the contents of the preferences map.
     * Deletes the file if the map is empty.
     */
    public void saveFile() {

        if (preferences.isEmpty()) {
            if (preferencesFile.exists()) preferencesFile.delete();
            return;
        }

        try {

            if (!preferencesFile.exists()) {
                preferencesFile.getParentFile().mkdirs();
                preferencesFile.createNewFile();
            }

            YamlAdapter preferencesYaml = plugin.loadYaml(preferencesFile);
            for (UUID uuid : preferences.keySet()) preferencesYaml.set("preferences." + uuid.toString(), preferences.get(uuid));
            preferencesYaml.save(preferencesFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Converts old ignoring.yml into new player_preferences.yml
     */
    private void convertIgnoringFile() {

        File ignoringFile = new File(plugin.getDataFolder(), "ignoring.yml");
        if (!ignoringFile.exists()) return;
        YamlAdapter ignoringYaml = plugin.loadYaml(ignoringFile);

        for (String uuidString : ignoringYaml.getStringList("ignoring")) {

            try {
                UUID uuid = UUID.fromString(uuidString);
                preferences.put(uuid, false);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

        }

        ignoringFile.delete();
        saveFile();

    }

}
