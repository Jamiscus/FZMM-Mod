package fzmm.zailer.me.client.logic.headGallery;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fzmm.zailer.me.client.FzmmClient;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.apache.http.client.HttpResponseException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class HeadGalleryResources {
    public static final HashMap<String, ObjectArrayList<MinecraftHeadsData>> cache = new HashMap<>();
    public static final String MINECRAFT_HEADS_URL = "https://minecraft-heads.com";
    public static final String MINECRAFT_HEADS_API = MINECRAFT_HEADS_URL + "/scripts/api.php";
    public static final List<String> CATEGORY_LIST = List.of(
            "alphabet",
            "animals",
            "blocks",
            "decoration",
            "food-drinks",
            "humans",
            "humanoid",
            "miscellaneous",
            "monsters",
            "plants"
    );

    public static CompletableFuture<ObjectArrayList<MinecraftHeadsData>> getCategory(String category) {
        CompletableFuture<ObjectArrayList<MinecraftHeadsData>> future = new CompletableFuture<>();

        if (!CATEGORY_LIST.contains(category)) {
            String categoryList = String.join(", ", CATEGORY_LIST);
            String message = "Invalid category. Please choose from the following list:\n" + categoryList;
            FzmmClient.LOGGER.error(message);
            future.completeExceptionally(new IllegalArgumentException(message));
            return future;
        }

        boolean cacheCategories = FzmmClient.CONFIG.headGallery.cacheCategories();
        if (cacheCategories && cache.containsKey(category)) {
            future.complete(cache.get(category));
            return future;
        }

        String url = getUrl(category);

        try {
            future.complete(fetchUrl(url, category, cacheCategories));
        } catch (Exception e) {
            future.completeExceptionally(e);
        }

        return future;
    }

    private static String getUrl(String category) {
        return MINECRAFT_HEADS_API + "?cat=" + category + "&tags=true";
    }

    private static ObjectArrayList<MinecraftHeadsData> fetchUrl(String url, String category, boolean cacheCategories) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
        conn.setRequestProperty("User-Agent", FzmmClient.HTTP_USER_AGENT);
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();

        if ((responseCode / 100) == 2) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);

            in.close();

            ObjectArrayList<MinecraftHeadsData> headsData = parseJson(response.toString());

            if (cacheCategories)
                cache.put(category, headsData);

            FzmmClient.LOGGER.info("[HeadGalleryResources] category '{}' successfully loaded", category);

            return headsData;
        } else {
            String errorReason = conn.getResponseMessage();
            String message = "HTTP Error " + responseCode + " (" + (errorReason == null ? "Unknown reason" : errorReason) + ")";
            throw new HttpResponseException(responseCode, message);
        }
    }

    private static ObjectArrayList<MinecraftHeadsData> parseJson(String json) {
        JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();
        ObjectArrayList<MinecraftHeadsData> headsData = new ObjectArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            headsData.add(MinecraftHeadsData.parse(jsonObject));
        }

        return headsData;
    }

}
