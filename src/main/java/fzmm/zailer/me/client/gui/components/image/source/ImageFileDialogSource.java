package fzmm.zailer.me.client.gui.components.image.source;

import fzmm.zailer.me.client.FzmmClient;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ImageFileDialogSource implements IInteractiveImageLoader {
    private BufferedImage image;

    @Override
    public void execute(Consumer<BufferedImage> consumer) {
        this.image = null;

        CompletableFuture.runAsync(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer filterPatterns = stack.mallocPointer(4);
                filterPatterns.put(stack.UTF8("*.jpg"))
                        .put(stack.UTF8("*.png"))
                        .put(stack.UTF8("*.gif"))
                        .put(stack.UTF8("*.bmp"));
                filterPatterns.flip();

                String imagePath = TinyFileDialogs.tinyfd_openFileDialog(
                        Text.translatable("fzmm.gui.fileDialog.image.title").getString(),
                        null,
                        filterPatterns,
                        Text.translatable("fzmm.gui.fileDialog.image.filter").getString(),
                        false
                );
                if (imagePath != null) {
                    this.readImage(Path.of(imagePath));
                }
            }
        }, Util.getMainWorkerExecutor()).whenComplete((unused, throwable) -> {
            if (this.image != null) {
                consumer.accept(this.image);
            }
        });
    }

    private void readImage(Path path) {
        File file = path.toFile();
        try {
            this.image = ImageIO.read(file);
        } catch (IOException e) {
            FzmmClient.LOGGER.error("[ImageFileSource] Failed to read image: {}", file.getAbsolutePath(), e);
        }
    }

    @Override
    public Optional<BufferedImage> getImage() {
        return Optional.ofNullable(this.image);
    }

    @Override
    public boolean hasTextField() {
        return false;
    }
}
