// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.loaders.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class Filtration {

    private PluginAdapter plugin;

    private HashMap<String, JsonObject> badWordsCache = new HashMap<>();
    private HashMap<String, JsonObject> explicitCache = new HashMap<>();

    Filtration(PluginAdapter plugin) {
        this.plugin = plugin;
    }

    /**
     * Clears the cache of images that contain bad words.
     */
    public void clearBadWordsCache() {
        badWordsCache.clear();
    }

    /**
     * Checks if image contains bad words that aren't excluded in the config.yml
     * Provided by https://moderatecontent.com/
     * @param url the url of the image
     * @return true if bad words were detected in the image
     */
    public boolean hasBadWords(String url) {

        ChatImage chatImage = ChatImage.getInstance();
        Settings settings = chatImage.getSettings();
        boolean debug = settings.isDebug();
        Logger logger = plugin.getLogger();

        JsonObject json = badWordsCache.getOrDefault(url, null);
        boolean cached = true;

        if (json == null) {

            cached = false;
            String exclude = "";
            List<String> exclusions = settings.getExclusions();

            if (!exclusions.isEmpty()) {
                exclude = "exclude=";
                for (String exclusion : exclusions) exclude += exclusion + ",";
                exclude = exclude.substring(0, exclude.length()-1) + "&";
            }

            try {

                URL apiURL = new URL("https://api.moderatecontent.com/ocr/?" + exclude + "url=" + url);
                HttpURLConnection con = (HttpURLConnection) apiURL.openConnection();
                con.setRequestMethod("GET");

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine, response = "";
                    while ((inputLine = in.readLine()) != null) response += inputLine;
                    in.close();

                    JsonObject newJson = new JsonParser().parse(response).getAsJsonObject();

                    if (newJson != null) {

                        int errorCode = newJson.get("error_code").getAsInt();

                        if (errorCode == 0) {
                            json = newJson;
                            badWordsCache.put(url, newJson);
                        }

                        else if (debug) {
                            logger.warning("ChatImage Debugger - API error");
                            logger.warning("URL: " + url);
                            logger.warning("ERROR: " + errorCode + " - " + newJson.get("error").getAsString());
                        }

                    }

                    else if (debug) {
                        logger.warning("ChatImage Debugger - Null json");
                        logger.warning("URL: " + url);
                        logger.warning("RESPONSE: " + response);
                    }

                }

                else if (debug) {
                    logger.warning("ChatImage Debugger - Bad http response");
                    logger.warning("URL: " + url);
                    logger.warning("HTTP RESPONSE: " + con.getResponseMessage());
                }

            }

            catch (IOException e) {

                if (debug) {
                    logger.warning("ChatImage Debugger - Attempted bad word filtration");
                    logger.warning("URL: " + url);
                    e.printStackTrace();
                }

            }

        }

        if (json != null) {

            if (!json.get("bad_words").isJsonObject()) return false;

            else if (debug) {
                logger.info("ChatImage Debugger - Bad word filtration");
                logger.info("URL: " + url);
                logger.info("RESPONSE: " + json);
                logger.info("CACHED: " + cached);
            }

        }

        return true;

    }

    /**
     * Checks if image contains explicit content.
     * API key from https://moderatecontent.com/ is required; can be obtained for free.
     * @param url the url of the image
     * @return true if explicit content was detected in the image
     */
    public boolean hasExplicitContent(String url) {

        ChatImage chatImage = ChatImage.getInstance();
        Settings settings = chatImage.getSettings();
        boolean debug = settings.isDebug();
        Logger logger = plugin.getLogger();

        JsonObject json = explicitCache.getOrDefault(url, null);
        boolean cached = true;

        if (json == null) {

            cached = false;

            try {

                URL apiURL = new URL("https://api.moderatecontent.com/moderate/?key=" + settings.getApiKey() + "&url=" + url);
                HttpURLConnection con = (HttpURLConnection) apiURL.openConnection();
                con.setRequestMethod("GET");

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine, response = "";
                    while ((inputLine = in.readLine()) != null) response += inputLine;
                    in.close();

                    JsonObject newJson = new JsonParser().parse(response).getAsJsonObject();

                    if (newJson != null) {

                        int errorCode = newJson.get("error_code").getAsInt();

                        if (errorCode == 0) {
                            json = newJson;
                            explicitCache.put(url, newJson);
                        }

                        else if (debug) {
                            logger.warning("ChatImage Debugger - API error");
                            logger.warning("URL: " + url);
                            logger.warning("ERROR: " + errorCode + " - " + newJson.get("error").getAsString());
                        }

                    }

                    else if (debug) {
                        logger.warning("ChatImage Debugger - Null json");
                        logger.warning("URL: " + url);
                        logger.warning("RESPONSE: " + response);
                    }

                }

                else if (debug) {
                    logger.warning("ChatImage Debugger - Bad http response");
                    logger.warning("URL: " + url);
                    logger.warning("HTTP RESPONSE: " + con.getResponseMessage());
                }

            }

            catch (IOException e) {

                if (debug) {
                    logger.warning("ChatImage Debugger - Attempted explicit content filtration");
                    logger.warning("URL: " + url);
                    e.printStackTrace();
                }

            }

        }

        if (json != null) {

            if (json.get("rating_index").getAsInt() == 1) return false;

            else if (debug) {
                logger.info("ChatImage Debugger - Explicit content filtration");
                logger.info("URL: " + url);
                logger.info("RESPONSE: " + json);
                logger.info("CACHED: " + cached);
            }

        }

        return true;

    }

}
