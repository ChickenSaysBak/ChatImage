// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.adapters;

import net.md_5.bungee.api.chat.TextComponent;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

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
     * Loads a file to be interpreted as YAML.
     * @param file the file to load
     * @return the loaded yaml file that can be used to retrieve values
     */
    YamlAdapter loadYaml(File file);

    /**
     * Gets a player's UUID based on their name.
     * @param name the name of the player
     * @return the UUID of the player
     */
    UUID getUUID(String name);

    /**
     * Runs a synchronous task after a certain amount of server ticks.
     * @param task the task to run
     * @param ticks the amount of ticks (1 tick = 50 milliseconds)
     * @return the task ID
     */
    int runTaskLater(Runnable task, int ticks);

    /**
     * Sends a message to the recipient.
     * @param recipient the uuid of the recipient or null for console
     * @param message the message to send
     */
    void sendMessage(UUID recipient, String message);

    /**
     * Sends an image in the form of a TextComponent to the recipient. Does not send to console.
     * @param recipient the uuid of the recipient
     * @param component the image to send
     */
    void sendImage(UUID recipient, TextComponent component);

    /**
     * Check whether the plugin is Bungee or Spigot.
     * @return true if the plugin is Bungee
     */
    boolean isBungee();

    /**
     * Gets the player's protocol version number if they're on Bungee.
     * @param uuid the player to check
     * @return the player's protocol version or -1 if running Spigot or the player is invalid
     */
    int getPlayerVersion(UUID uuid);

}
