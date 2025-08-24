// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.loaders;

import me.chickensaysbak.chatimage.core.ChatImage;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.adapters.YamlAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerPreferences implements Loadable {

    private PluginAdapter plugin;
    private File preferencesFile;
    private HashMap<UUID, Boolean> preferences = new HashMap<>();
    private int saveTimeRemaining = 0;
    private ArrayList<UUID> recordedPlayerPreference = new ArrayList<>();

    public PlayerPreferences(PluginAdapter plugin) {

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

        updateStats();
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
     * Updates bStats on how many players are individually hiding images without Auto Hide enabled.
     */
    public void updateStats() {

        int hiding = 0;

        for (UUID uuid : preferences.keySet()) if (!preferences.get(uuid) && !recordedPlayerPreference.contains(uuid)) {
            ++hiding;
            recordedPlayerPreference.add(uuid);
        }

        if (hiding > 0) plugin.publishStat("players_hiding_images2", hiding);

    }

    /**
     * Loads or reloads players' preferences.
     */
    @Override
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
                plugin.getLogger().log(Level.WARNING, e.getMessage(), e);
            }

        }

        updateStats();

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
            preferencesYaml.set("preferences", null); // Clears outdated preferences.
            for (UUID uuid : preferences.keySet()) preferencesYaml.set("preferences." + uuid.toString(), preferences.get(uuid));
            preferencesYaml.save(preferencesFile);

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
        }

    }

    /**
     * Converts old ignoring.yml (from versions 2.0.0 and below) into new player_preferences.yml
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
                plugin.getLogger().log(Level.WARNING, e.getMessage(), e);
            }

        }

        ignoringFile.delete();
        saveFile();

    }

}
