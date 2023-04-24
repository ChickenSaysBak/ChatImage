// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core;

import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.adapters.YamlAdapter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Settings {

    private PluginAdapter plugin;

    // Config fields
    private File configFile;
    private boolean smoothRender;
    private boolean trimTransparency;
    private int maxWidth;
    private int maxHeight;
    private int cooldown;
    private boolean strictCooldown;
    private boolean hoverMode;
    private int hoverWidth;
    private int hoverHeight;
    private boolean filterBadWords;
    private boolean removeBadWords;
    private List<String> exclusions;
    private String apiKey;
    private boolean filterExplicitContent;
    private boolean removeExplicitContent;
    private boolean debug;

    // Message fields
    private File messagesFile;
    private HashMap<String, String> msgs = new HashMap<>();

    Settings(PluginAdapter plugin) {

        this.plugin = plugin;
        configFile = new File(plugin.getDataFolder(), "config.yml");
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        reload();

    }

    /**
     * Loads or reloads all settings from their files and creates the files if they do not exist.
     * If any setting is not found, a default value is used.
     */
    public void reload() {

        createFile(configFile);
        createFile(messagesFile);

        List<String> oldExclusions = new ArrayList<>();
        if (exclusions != null) oldExclusions.addAll(exclusions);

        YamlAdapter config = plugin.loadYaml(configFile);
        smoothRender = config.getBoolean("smooth_render", config.getBoolean("fancy_render", true)); // Legacy support.
        trimTransparency = config.getBoolean("trim_transparency", true);
        maxWidth = config.getInt("max_width", 35);
        maxHeight = config.getInt("max_height", 20);
        cooldown = config.getInt("cooldown", 3);
        strictCooldown = config.getBoolean("strict_cooldown", false);
        hoverMode = config.getBoolean("hover_mode.enabled", false);
        hoverWidth = config.getInt("hover_mode.max_width", 23);
        hoverHeight = config.getInt("hover_mode.max_height", 24);
        filterBadWords = config.getBoolean("bad_words.enabled", false);
        removeBadWords = config.getBoolean("bad_words.remove_message", false);
        exclusions = config.getStringList("bad_words.exclusions");
        apiKey = config.getString("explicit_content.api_key", "");
        filterExplicitContent = config.getBoolean("explicit_content.enabled", false);
        removeExplicitContent = config.getBoolean("explicit_content.remove_message", false);
        debug = config.getBoolean("debug", false);

        // Clears cache to disregard old exclusions if they have changed.
        if (!exclusions.equals(oldExclusions)) ChatImage.getInstance().getFiltration().clearBadWordsCache();

        YamlAdapter messages = plugin.loadYaml(messagesFile);
        msgs.clear();
        for (String key : messages.getKeys()) msgs.put(key, messages.getString(key, ""));

        plugin.publishStat("smooth_render", String.valueOf(smoothRender));
        plugin.publishStat("trim_transparency", String.valueOf(trimTransparency));
        plugin.publishStat("max_width", String.valueOf(maxWidth));
        plugin.publishStat("max_height", String.valueOf(maxHeight));
        plugin.publishStat("cooldowns", String.valueOf(cooldown));
        plugin.publishStat("strict_cooldown", String.valueOf(strictCooldown));
        plugin.publishStat("hover_mode", String.valueOf(hoverMode));
        plugin.publishStat("hover_max_width", String.valueOf(hoverWidth));
        plugin.publishStat("hover_max_width", String.valueOf(hoverWidth));
        plugin.publishStat("bad_word_filtration", String.valueOf(filterBadWords));
        plugin.publishStat("bad_word_message_removal", String.valueOf(removeBadWords));
        plugin.publishStat("explicit_content_filtration", String.valueOf(filterExplicitContent));
        plugin.publishStat("explicit_content_message_removal", String.valueOf(removeExplicitContent));

    }

    public boolean isSmoothRender() {return smoothRender;}
    public boolean isTrimTransparency() {return trimTransparency;}
    public int getMaxWidth() {return maxWidth;}
    public int getMaxHeight() {return maxHeight;}
    public boolean isHoverMode() {return hoverMode;}
    public int getHoverMaxWidth() {return hoverWidth;}
    public int getHoverMaxHeight() {return hoverHeight;}
    public int getCooldown() {return cooldown;}
    public boolean isStrictCooldown() {return strictCooldown;}
    public boolean isFilterBadWords() {return filterBadWords;}
    public boolean isRemoveBadWords() {return removeBadWords;}
    public List<String> getExclusions() {return new ArrayList<>(exclusions);}
    public String getApiKey() {return apiKey;}
    public boolean isFilterExplicitContent() {return filterExplicitContent;}
    public boolean isRemoveExplicitContent() {return removeExplicitContent;}
    public boolean isDebug() {return debug;}

    public String getMessage(String name) {return msgs.getOrDefault(name, null);}

    /**
     * If the file does not exist, it is created and written based on the corresponding resource file.
     * @param file the file to create
     */
    private void createFile(File file) {

        if (file.exists()) return;

        try {

            file.getParentFile().mkdirs();
            file.createNewFile();

            String content = convertStreamToString(plugin.getResource(file.getName()));
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Converts InputStream to String
     * @param in the InputStream to convert
     * @return a String containing the content of the InputStream
     */
    private String convertStreamToString(InputStream in) {

        String result = null;

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int length; (length = in.read(buffer)) != -1;) output.write(buffer, 0, length);
            result = output.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }

}
