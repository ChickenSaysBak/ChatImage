// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core;

import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.adapters.YamlAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class IgnoringImages {

    private PluginAdapter plugin;
    private File ignoringFile;
    private ArrayList<UUID> ignoring = new ArrayList<>();
    private int saveTimeRemaining = 0;

    IgnoringImages(PluginAdapter plugin) {

        this.plugin = plugin;
        ignoringFile = new File(plugin.getDataFolder(), "ignoring.yml");

        reload();

    }

    /**
     * Considers a player ignoring or not ignoring images.
     * If changes are successfully made, the file will be queued to save.
     * @param uuid the uuid of the player
     * @param ignore whether to ignore images or stop ignoring
     */
    public void setIgnoring(UUID uuid, boolean ignore) {

        boolean save = false;

        if (ignore) {
            if (!ignoring.contains(uuid)) save = ignoring.add(uuid);
        } else save = ignoring.remove(uuid);

        if (save) queueSave();

    }

    /**
     * Check whether a player is ignoring images or not.
     * @param uuid the uuid of the player
     * @return true if the player is ignoring images
     */
    public boolean isIgnoring(UUID uuid) {
        return ignoring.contains(uuid);
    }

    /**
     * Loads or reloads players ignoring images.
     */
    public void reload() {

        ignoring.clear();
        if (!ignoringFile.exists()) return;
        YamlAdapter ignoringYaml = plugin.loadYaml(ignoringFile);

        for (String uuidString : ignoringYaml.getStringList("ignoring")) {

            try {
                UUID uuid = UUID.fromString(uuidString);
                ignoring.add(uuid);
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
     * Saves the ignoring file after at least 5 seconds or longer if changes are made before saving.
     * This prevents players from causing potential lag from spamming the /ignoreimages command.
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
     * Updates the ignoring file to contain the contents of the ignoring list.
     * Deletes the file if the list is empty.
     */
    public void saveFile() {

        if (ignoring.isEmpty()) {
            if (ignoringFile.exists()) ignoringFile.delete();
            return;
        }

        try {

            if (!ignoringFile.exists()) {
                ignoringFile.getParentFile().mkdirs();
                ignoringFile.createNewFile();
            }

            YamlAdapter ignoringYaml = plugin.loadYaml(ignoringFile);
            List<String> uuidStrings = ignoring.stream().map(UUID::toString).collect(Collectors.toList());

            ignoringYaml.set("ignoring", uuidStrings);
            ignoringYaml.save(ignoringFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
