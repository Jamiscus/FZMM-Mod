package me.zailer.testmod.client.test;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.image.source.ImageFileDialogSource;
import fzmm.zailer.me.client.gui.head_generator.HeadGeneratorScreen;
import fzmm.zailer.me.client.gui.head_generator.components.HeadComponentOverlay;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.client.logic.head_generator.HeadResourcesLoader;
import fzmm.zailer.me.client.logic.head_generator.model.HeadModelEntry;
import fzmm.zailer.me.utils.ImageUtils;
import fzmm.zailer.me.utils.SkinPart;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Predicate;

public class HeadGeneratorTest {
    public static void writeSkins() {
        new ImageFileDialogSource().execute(skin -> {
            for (var model : HeadResourcesLoader.getAllLoaded()) {
                var result = getHead(model, skin);

                var file = new File(HeadGeneratorScreen.SKIN_SAVE_FOLDER_PATH + "/test/" + model.getKey() + ".png");
                if (file.mkdirs()) {
                    FzmmClient.LOGGER.info("[HeadGeneratorTest] Test skin save folder created");
                }

                HeadComponentOverlay.saveSkinExecute(result, file);
            }
        });
    }

    public static void checkFormat(boolean isSlim) {
        checkSkin(bufferedImage -> ImageUtils.isSlimFullCheck(bufferedImage) == isSlim, true);
    }

    public static void checkPixel(int x, int y, boolean subtractEmptyBody) {
        checkSkin(bufferedImage -> ImageUtils.hasPixel(1, x, y, bufferedImage), subtractEmptyBody);
    }

    public static void checkSkin(Predicate<BufferedImage> predicate, boolean subtractEmptyBody) {
        new ImageFileDialogSource().execute(skin -> {
            var entries = HeadResourcesLoader.getLoaded();
            var count = entries.size();
            var correctCount = 0;
            var missingBodyCount = 0;
            for (var model : entries) {
                var headSkin = getHead(model, skin);
                var isEmptyBody = !ImageUtils.hasPixel(1, SkinPart.BODY.x() + 4, SkinPart.BODY.y() + 4, headSkin);

                if (isEmptyBody) {
                    missingBodyCount++;
                    if (subtractEmptyBody) {
                        continue;
                    }
                }

                if (predicate.test(headSkin)) {
                    correctCount++;
                } else {
                    FzmmClient.LOGGER.warn("[HeadGeneratorTest] Pixel not found in model '{}'", model.getKey());
                }
            }

            var totalCount = count;
            if (subtractEmptyBody) {
                totalCount -= missingBodyCount;
            }

            MinecraftClient.getInstance().inGameHud.getChatHud()
                    .addMessage(Text.literal("Correct: " + correctCount + "/" + totalCount
                            + " - Missing body: " + missingBodyCount + "/" + count
                    ));
        });
    }

    private static BufferedImage getHead(AbstractHeadEntry model, BufferedImage skin) {
        var skinCopy = new BufferedImage(skin.getWidth(), skin.getHeight(), BufferedImage.TYPE_INT_ARGB);
        skinCopy.getGraphics().drawImage(skin, 0, 0, null);

        if (model instanceof HeadModelEntry modelEntry) {
            for (var textureParam : modelEntry.getNestedTextureParameters().parameterList()) {
                if (textureParam.isRequested()) {
                    textureParam.setValue(skin);
                }
            }
        }

        return model.getHeadSkin(skinCopy);
    }
}
