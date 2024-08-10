package fzmm.zailer.me.client.logic.head_gallery;

import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record MinecraftHeadsData(String name, UUID uuid, String value, Set<String> tags) {

    public static MinecraftHeadsData parse(JsonObject jsonObject) {
        String name = jsonObject.get("name").getAsString();
        UUID uuid = UUID.fromString(jsonObject.get("uuid").getAsString());
        String value = jsonObject.get("value").getAsString();
        String tags = jsonObject.get("tags").getAsString();
        Set<String> tagsSet = new HashSet<>(Arrays.asList(tags.split(",(?=\\S)")));
        return new MinecraftHeadsData(name, uuid, value, tagsSet);
    }

    public boolean filter(Set<String> tags, String toLowerCaseName) {
        boolean hasTag = tags.isEmpty() || this.tags.containsAll(tags);

        return hasTag && this.name.toLowerCase().contains(toLowerCaseName);
    }
}
