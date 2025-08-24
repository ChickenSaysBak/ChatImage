// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core.loaders;

import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.adapters.YamlAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;

public class Settings implements Loadable {

    private PluginAdapter plugin;

    // Config fields
    private File configFile;
    private boolean smoothRender;
    private boolean trimTransparency;
    private int maxWidth;
    private int maxHeight;
    private int cooldown;
    private boolean strictCooldown;
    private boolean autoHide;
    private int hiddenWidth;
    private int hiddenHeight;
    private boolean filterExplicitContent;
    private boolean removeExplicitContent;
    private String languageDefault;
    private boolean debug;

    // Message fields
    private File messagesDirectory;
    private HashMap<String, HashMap<String, String>> multilingualMsgs = new HashMap<>();
    private String[] suppliedLangs = {
            "de_de", "en_us", "es_es", "fi_fi", "fr_fr", "hi_in", "id_id", "ja_jp", "ko_kr", "nl_nl", "pl_pl", "pt_pt", "zh_cn", "zh_hk"
    };

    public Settings(PluginAdapter plugin) {

        this.plugin = plugin;
        configFile = new File(plugin.getDataFolder(), "config.yml");
        messagesDirectory = new File(plugin.getDataFolder(), "messages");

        reload();

    }

    /**
     * Loads or reloads all settings from their files and creates the files if they do not exist.
     * If any setting is not found, a default value is used.
     */
    @Override
    public void reload() {

        plugin.saveResource("config.yml");
        convertLegacyMessages();
        Arrays.stream(suppliedLangs).forEach(lang -> plugin.saveResource("messages/" + lang + ".yml"));

        YamlAdapter config = plugin.loadYaml(configFile);
        smoothRender = config.getBoolean("smooth_render", config.getBoolean("fancy_render", true)); // Legacy support.
        trimTransparency = config.getBoolean("trim_transparency", true);
        maxWidth = config.getInt("max_width", 35);
        maxHeight = config.getInt("max_height", 20);
        cooldown = config.getInt("cooldown", 3);
        strictCooldown = config.getBoolean("strict_cooldown", false);
        autoHide = config.getBoolean("hidden_images.auto_hide", false);
        hiddenWidth = config.getInt("hidden_images.max_width", 23);
        hiddenHeight = config.getInt("hidden_images.max_height", 24);
        filterExplicitContent = config.getBoolean("explicit_content.enabled", false);
        removeExplicitContent = config.getBoolean("explicit_content.remove_message", false);
        languageDefault = config.getString("language_default", "en_us");
        debug = config.getBoolean("debug", false);

        multilingualMsgs.clear();

        for (File f : messagesDirectory.listFiles()) if (f != null && f.getName().endsWith(".yml")) {

            YamlAdapter messages = plugin.loadYaml(f);
            HashMap<String, String> msgs = new HashMap<>();

            for (String key : messages.getKeys()) msgs.put(key, messages.getString(key, ""));
            multilingualMsgs.put(f.getName().split("\\.")[0], msgs);

        }

        plugin.publishStat("smooth_render", String.valueOf(smoothRender));
        plugin.publishStat("trim_transparency", String.valueOf(trimTransparency));
        plugin.publishStat("max_width", String.valueOf(maxWidth));
        plugin.publishStat("max_height", String.valueOf(maxHeight));
        plugin.publishStat("cooldowns", String.valueOf(cooldown));
        plugin.publishStat("strict_cooldown", String.valueOf(strictCooldown));
        plugin.publishStat("auto_hide", String.valueOf(autoHide));
        plugin.publishStat("max_hidden_width", String.valueOf(hiddenWidth));
        plugin.publishStat("max_hidden_height", String.valueOf(hiddenHeight));
        plugin.publishStat("explicit_content_filtration2", String.valueOf(filterExplicitContent));
        plugin.publishStat("explicit_content_message_removal2", String.valueOf(removeExplicitContent));

    }

    public boolean isSmoothRender() {return smoothRender;}
    public boolean isTrimTransparency() {return trimTransparency;}
    public int getMaxWidth() {return maxWidth;}
    public int getMaxHeight() {return maxHeight;}
    public boolean isAutoHide() {return autoHide;}
    public int getMaxHiddenWidth() {return hiddenWidth;}
    public int getMaxHiddenHeight() {return hiddenHeight;}
    public int getCooldown() {return cooldown;}
    public boolean isStrictCooldown() {return strictCooldown;}
    public boolean isFilterExplicitContent() {return filterExplicitContent;}
    public boolean isRemoveExplicitContent() {return removeExplicitContent;}
    public String getLanguageDefault() {return languageDefault;}
    public boolean isDebug() {return debug;}

    /**
     * Gets a message from the messages folder based on the recipient's locale.
     * If a locale doesn't exist, a variant will be used if one is available, otherwise the default is used.
     * @param name the name of the message key
     * @param locale the locale of the recipient
     * @return the corresponding translated message
     */
    public String getMessage(String name, String locale) {

        locale = locale.toLowerCase();

        if (!multilingualMsgs.containsKey(locale)) {

            String baseLang = locale.split("_")[0] + "_";

            locale = multilingualMsgs.keySet().stream()
                    .filter(l -> l.startsWith(baseLang))
                    .findFirst()
                    .orElseGet(() -> multilingualMsgs.containsKey(languageDefault) ? languageDefault : "en_us");

        }

        return multilingualMsgs.get(locale).get(name);

    }

    /**
     * Converts messages.yml from versions 2.5.0 and below to en_us.yml within the messages folder.
     */
    private void convertLegacyMessages() {

        File legacyMessages = new File(plugin.getDataFolder(), "messages.yml");
        if (!legacyMessages.exists()) return;

        File enUS = new File(messagesDirectory, "en_us.yml");
        if (enUS.exists()) return;

        try {
            Files.copy(legacyMessages.toPath(), enUS.toPath());
            legacyMessages.delete();
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, e.getMessage(), e);
        }

    }

}
