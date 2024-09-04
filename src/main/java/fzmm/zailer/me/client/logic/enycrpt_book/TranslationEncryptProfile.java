package fzmm.zailer.me.client.logic.enycrpt_book;

import com.google.gson.JsonObject;
import fzmm.zailer.me.config.FzmmConfigModel;
import io.wispforest.owo.config.annotation.RangeConstraint;

import java.util.*;

public record TranslationEncryptProfile(int seed, @RangeConstraint(min = 1, max = MAX_LENGTH) int length, String key,
                                        int asymmetricValue, @RangeConstraint(min = 1, max = 2) int algorithmVersion) {

    public static final int ALGORITHM_VERSION = 2;
    public static final int MAX_LENGTH = 512;

    public boolean isAsymmetric() {
        return this.asymmetricValue() != 0;
    }

    public boolean isOldAlgorithm() {
        return this.algorithmVersion() != ALGORITHM_VERSION;
    }

    public String translationKey() {
        return translationKey(this.key(), this.seed());
    }

    public static String translationKey(String key, int seed) {
        return key.replaceFirst("%s", String.valueOf(seed));
    }

    public int asymmetricSeed() {
        int result = this.seed();
        if (this.isAsymmetric()) {
            if (result == 0) {
                result = 1;
            }
            result *= this.asymmetricValue() + 0x19429630;
        }

        return result;
    }

    /**
     * Backwards compatibility with old items
     *
     * @see #shuffleIndex()
     */
    @Deprecated(since = "0.2.14")
    private List<Short> oldShuffleIndex() {
        int messageLength = this.length();
        List<Short> encryptedKey = new ArrayList<>();
        Random number = new Random(this.asymmetricSeed());

        // this is necessary to have backward compatibility with previous functionality
        number.nextInt(messageLength);

        // RIP performance
        // Never - Day that I created this (2021-05-05 13:54)
        while (encryptedKey.size() < messageLength) {
            short nextInt = (short) number.nextInt(messageLength);
            if (!encryptedKey.contains(nextInt)) {
                encryptedKey.add(nextInt);
            }
        }

        return encryptedKey;
    }

    private List<Short> shuffleIndex() {
        int messageLength = this.length();
        List<Short> indexList = new ArrayList<>(messageLength);

        for (short i = 0; i < messageLength; i++) {
            indexList.add(i);
        }

        Random number = new Random(this.asymmetricSeed());
        Collections.shuffle(indexList, number);

        return indexList;
    }

    public List<Short> encryptIndexOrder() {
        return switch (this.algorithmVersion()) {
            case 1 -> this.oldShuffleIndex();
            case 2 -> this.shuffleIndex();
            default -> throw new IllegalArgumentException("Unknown algorithm version: " + this.algorithmVersion());
        };
    }

    public String decryptorValue() {
        StringBuilder result = new StringBuilder();
        Formatter formatter = new Formatter(result);
        int length = this.length();
        List<Short> encryptedIndex = this.encryptIndexOrder();

        for (int i = 0; i < length; i++) {
            formatter.format("%%%1$s$s", encryptedIndex.get(i) + 1);
        }

        return result.toString();
    }

    public JsonObject toJson() {
        JsonObject result = new JsonObject();
        result.addProperty(this.translationKey(), this.decryptorValue());
        return result;
    }

    public static TranslationEncryptProfile of(FzmmConfigModel.TranslationEncryptProfileModel model) {
        return new TranslationEncryptProfile(model.seed, model.length, model.key, model.asymmetricValue, model.algorithmVersion);
    }

    public static List<TranslationEncryptProfile> of(List<FzmmConfigModel.TranslationEncryptProfileModel> models) {
        return models.stream().map(TranslationEncryptProfile::of).toList();
    }

    public FzmmConfigModel.TranslationEncryptProfileModel toModel() {
        FzmmConfigModel.TranslationEncryptProfileModel model = new FzmmConfigModel.TranslationEncryptProfileModel();
        model.seed = this.seed();
        model.length = this.length();
        model.key = this.key();
        model.asymmetricValue = this.asymmetricValue();
        model.algorithmVersion = this.algorithmVersion();
        return model;
    }
}
