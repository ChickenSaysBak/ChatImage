// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.adapters;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public interface PluginAdapter {

    /**
     * Registers a command using the proper command executor for Spigot or Bungee.
     * @param command the command to register
     */
    void registerCommand(CommandAdapter command);

    /**
     * Gets the plugin's folder for things like config files.
     * @return the plugin's folder
     */
    File getDataFolder();

    /**
     * Gets resources built into the plugin jar like config defaults.
     * @param name the file name including the extension
     * @return the resource
     */
    InputStream getResource(String name);

    /**
     * Saves a resource into the plugin's data folder.
     * @param path the path of the resource
     * @param replace replaces an existing file if true
     */
    void saveResource(String path, boolean replace);

    /**
     * Gets the plugin's console logger.
     * @return the logger
     */
    Logger getLogger();

    /**
     * Sends a message to console with color support.
     * @param message the message to send
     */
    void sendConsoleMessage(String message);

    /**
     * Gets a player from their uuid.
     * @param uuid the uuid to search for
     * @return the player found
     */
    PlayerAdapter getPlayer(UUID uuid);

    /**
     * Gets a player from their name.
     * @param name the name to search for
     * @return the player found
     */
    PlayerAdapter getPlayer(String name);

    /**
     * Gets all online players.
     * @return all online players
     */
    List<PlayerAdapter> getOnlinePlayers();

    /**
     * Loads a file to be interpreted as YAML.
     * @param file the file to load
     * @return the loaded yaml file that can be used to retrieve values
     */
    YamlAdapter loadYaml(File file);

    /**
     * Runs an asynchronous task after a certain amount of server ticks.
     * @param task the task to run
     * @param ticks the amount of ticks (1 tick = 50 milliseconds)
     * @return the task ID
     */
    int runAsyncTaskLater(Runnable task, int ticks);

    /**
     * Check whether the plugin is Bungee or Spigot.
     * @return true if the plugin is Bungee
     */
    boolean isBungee();

    /**
     * Publishes a pie chart statistic to bStats.
     * @param id the id of the stat
     * @param value the value of the stat
     */
    void publishStat(String id, String value);

    /**
     * Publishes a line chart statistic to bStats.
     * @param id the id of the stat
     * @param value the value of the stat
     */
    void publishStat(String id, int value);

    /**
     * Sets placeholders in a string of text if PlaceholderAPI is enabled.
     * @param uuid the uuid of the player involved
     * @param text the text to set placeholders in
     * @return the text with placeholders set
     */
    String setPlaceholders(UUID uuid, String text);

}
