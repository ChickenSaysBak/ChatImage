// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.chickensaysbak.chatimage.core.adapters.PluginAdapter;
import me.chickensaysbak.chatimage.core.loaders.Settings;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Filtration {

    private PluginAdapter plugin;
    private HashMap<String, JsonObject> explicitCache = new HashMap<>();

    Filtration(PluginAdapter plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if image contains explicit content.
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

                HttpClient client = HttpClient.newHttpClient();

                String body = "--$bound%\r\n" +
                        "Content-Disposition: form-data; name=\"url\"\r\n\r\n" +
                        url + "\r\n" +
                        "--$bound%--\r\n";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://demo.api4ai.cloud/nsfw/v1/results"))
                        .header("Content-Type", "multipart/form-data; boundary=$bound%")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                int httpCode = response.statusCode();

                if (httpCode == HttpURLConnection.HTTP_OK) {

                    JsonObject newJson = new JsonParser().parse(response.body()).getAsJsonObject();

                    if (newJson != null) {

                        newJson = newJson.getAsJsonArray("results").get(0).getAsJsonObject();
                        JsonObject status = newJson.getAsJsonObject("status");
                        String statusCode = status.get("code").getAsString();

                        if (statusCode.equalsIgnoreCase("ok")) {
                            json = newJson;
                            explicitCache.put(url, newJson);
                        }

                        else if (debug) {
                            logger.warning("ChatImage Debugger - API error");
                            logger.warning("URL: " + url);
                            logger.warning("ERROR: " + statusCode + " - " + status.get("message").getAsString());
                            return false;
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
                    logger.warning("HTTP CODE: " + httpCode);
                }

            }

            catch (IOException | InterruptedException e) {

                if (debug) {
                    logger.warning("ChatImage Debugger - Attempted explicit content filtration");
                    logger.warning("URL: " + url);
                    logger.log(Level.WARNING, e.getMessage(), e);
                }

            }

        }

        if (json != null) {

            double nsfwConfidence = json.getAsJsonArray("entities").get(0).getAsJsonObject()
                    .getAsJsonObject("classes")
                    .get("nsfw").getAsDouble();

            if (debug) {
                logger.info("ChatImage Debugger - Explicit content filtration");
                logger.info("URL: " + url);
                logger.info("RESPONSE: " + json);
                logger.info("CACHED: " + cached);
            }

            return nsfwConfidence > 0.5;

        }

        return false;

    }

}
