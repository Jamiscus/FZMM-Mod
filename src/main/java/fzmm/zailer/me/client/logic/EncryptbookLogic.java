package fzmm.zailer.me.client.logic;

import fzmm.zailer.me.builders.BookBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.*;

import java.util.*;

public class EncryptbookLogic {
    protected static List<Short> encryptKey(long seed, int messageLength) {
        List<Short> encryptedKey = new LinkedList<>();
        Random number = new Random(seed);

        // this is necessary to have backward compatibility with previous functionality
        number.nextInt(messageLength);

        while (encryptedKey.size() < messageLength) {
            short nextInt = (short) number.nextInt(messageLength);
            if (!encryptedKey.contains(nextInt))
                encryptedKey.add(nextInt);
        }

        return encryptedKey;
    }

    public static void give(int seed, String message, String author, String paddingChars, int maxMessageLength, String bookTitle) {
        FzmmConfig.Encryptbook config = FzmmClient.CONFIG.encryptbook;
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;

        List<Short> encryptedKey = encryptKey(getKey(seed), maxMessageLength);
        List<String> encryptMessage = encryptMessage(message, config, paddingChars, maxMessageLength, encryptedKey);
        String encryptMessageString = getEncryptMessageString(encryptMessage);

        ItemStack book = getBook(seed, config, bookTitle, author, encryptMessage, encryptMessageString);

        FzmmUtils.giveItem(book);
    }


    private static List<String> encryptMessage(String message, FzmmConfig.Encryptbook config, String paddingChars, int maxMessageLength, List<Short> encryptedKey) {
        Random random = new Random(new Date().getTime());
        List<String> paddingCharacters = Arrays.asList(paddingChars.split(""));

        message += config.separatorMessage();
        message = message.replaceAll(" ", "_");
        List<String> splitMessage = new ArrayList<>(FzmmUtils.splitMessage(message));
        int messageLength = splitMessage.size();

        while (messageLength < maxMessageLength) {
            String randomCharacter = paddingCharacters.get(random.nextInt(paddingCharacters.size()));
            splitMessage.add(randomCharacter);
            messageLength++;
        }

        List<String> encryptMessage = new ArrayList<>();
        for (int i = 0; i != splitMessage.size(); i++)
            encryptMessage.add("");

        for (int i = 0; i < maxMessageLength; i++)
            encryptMessage.set(encryptedKey.get(i), splitMessage.get(i));

        return encryptMessage;
    }

    private static String getEncryptMessageString(List<String> encryptMessage) {
        StringBuilder encryptMessageString = new StringBuilder();
        for (String s : encryptMessage)
            encryptMessageString.append(s);

        return encryptMessageString.toString();
    }

    private static ItemStack getBook(int seed, FzmmConfig.Encryptbook config, String bookTitle, String author, List<String> encryptMessage, String encryptMessageString) {
        String translationKeyPrefix = config.translationKeyPrefix();
        if (bookTitle.contains("%s")) {
            bookTitle = String.format(bookTitle, translationKeyPrefix + seed);
        }

        BookBuilder bookBuilder = BookBuilder.builder()
                .title(bookTitle)
                .author(author);

        Text encryptMessageTooltip = Text.literal(
                Text.translatable("fzmm.item.encryptbook.encryptMessage.tooltip", translationKeyPrefix + seed, config.asymmetricEncryptKey() != 0).getString()
        );

        bookBuilder.addPage(
                Text.translatableWithFallback(translationKeyPrefix + seed, encryptMessageString, encryptMessage.toArray())
                        .setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, encryptMessageTooltip)))
        );

        return bookBuilder.get();
    }

    public static void showDecryptorInChat(int seed, int maxMessageLength) {
        MinecraftClient mc = MinecraftClient.getInstance();
        String translationKeyPrefix = FzmmClient.CONFIG.encryptbook.translationKeyPrefix();
        StringBuilder decryptorString = new StringBuilder();
        Formatter formatter = new Formatter(decryptorString);
        List<Short> encryptedKey = encryptKey(getKey(seed), maxMessageLength);

        assert mc.player != null;

        for (int i = 0; i < maxMessageLength; i++)
            formatter.format("%%%1$s$s", encryptedKey.get(i) + 1);

        String decryptorTranslationMessage = String.format("\"%s\": \"%s\"", translationKeyPrefix + seed, decryptorString);

        MutableText decryptorMessage = Text.translatable("fzmm.gui.encryptbook.button.copyDecryptor", translationKeyPrefix + seed)
                .setStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, decryptorTranslationMessage))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(decryptorString.toString())))
                        .withColor(FzmmClient.CHAT_BASE_COLOR)
                );

        mc.inGameHud.getChatHud().addMessage(decryptorMessage);
    }

    private static long getKey(long seed) {
        int asymmetricEncryptKey = FzmmClient.CONFIG.encryptbook.asymmetricEncryptKey();
        if (asymmetricEncryptKey != 0) {
            if (seed == 0)
                seed = 1;
            seed *= asymmetricEncryptKey + 0x19429630;
        }

        return seed;
    }
}
