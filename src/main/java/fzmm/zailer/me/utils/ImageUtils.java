package fzmm.zailer.me.utils;

import com.google.gson.JsonIOException;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.utils.skin.GetSkinDecorator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

public class ImageUtils {

    public static Optional<BufferedImage> getBufferedImgFromIdentifier(Identifier identifier) {
        try {
            Optional<Resource> imageResource = MinecraftClient.getInstance().getResourceManager().getResource(identifier);
            return imageResource.isEmpty() ? Optional.empty() : Optional.of(ImageIO.read(imageResource.get().getInputStream()));
        } catch (IOException ignored) {
            return Optional.empty();
        }
    }

    public static BufferedImage getBufferedImgFromNativeImg(NativeImage nativeImage) {
        int width = nativeImage.getWidth();
        int height = nativeImage.getHeight();
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = nativeImage.getColor(x, y);//ABGR

                bufferedImage.setRGB(x, y, ((color >> 16) & 0xFF) | ((color & 0xFF) << 16) | (color & 0xFF00FF00));//ARGB
            }
        }

        return bufferedImage;
    }


    public static Optional<BufferedImage> getPlayerSkin(String name, GetSkinDecorator getSkinDecorator) throws NullPointerException, JsonIOException, IOException {
        Optional<BufferedImage> skin = getSkinDecorator.getSkin(name);

        if (skin.isEmpty()) {
            FzmmClient.LOGGER.warn("[ImageUtils] skin of '{}' was not found", name);
        }

        return skin;
    }

    public static BufferedImage getImageFromPath(String path) throws IOException {
        File imgFile = new File(path);
        return ImageIO.read(imgFile);
    }

    public static Optional<BufferedImage> getImageFromUrl(String urlLocation) throws IOException {
        URL url = URI.create(urlLocation).toURL();
        return Optional.ofNullable(ImageIO.read(url));
    }

    public static NativeImage toNativeImage(BufferedImage image) {

        // """NativeImage.Format.RGBA""" = ABGR
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, image.getWidth(), image.getHeight(), false);
        ColorModel colorModel = image.getColorModel();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {

                // avoid using image.getRGB(x, y) to pack in argb, to unpack it, to pack it in abgr
                Object elements = image.getRaster().getDataElements(x, y, null);

                int abgr = (colorModel.getAlpha(elements) << 24) |
                        (colorModel.getBlue(elements) << 16) |
                        (colorModel.getGreen(elements) << 8) |
                        colorModel.getRed(elements);

                nativeImage.setColor(x, y, abgr);
            }
        }
        return nativeImage;
    }

    public static boolean isEquals(BufferedImage image1, BufferedImage image2) {
        if (image1.getWidth() != image2.getWidth() || image1.getHeight() != image2.getHeight())
            return false;

        for (int y = 0; y < image1.getHeight(); y++) {
            for (int x = 0; x < image1.getWidth(); x++) {
                if (image1.getRGB(x, y) != image2.getRGB(x, y))
                    return false;
            }
        }
        return true;
    }

    public static boolean isAlexModel(int scale, BufferedImage skin) {
        int color = skin.getRGB((SkinPart.LEFT_ARM.x() + 15) * scale, (SkinPart.LEFT_ARM.y() + 15) * scale);
        int alpha = new Color(color, true).getAlpha();
        return alpha == 0;
    }

    // TODO:
    //  add scale in head models and replace this with InternalModels.SLIM_TO_WIDE
    //  scale is necessary in player statue (because there are 128x128 skins)
    public static BufferedImage convertInSteveModel(BufferedImage skin, int scale) {
        BufferedImage modifiedSkin = convertInSteveModel(skin, SkinPart.LEFT_ARM, scale);
        return convertInSteveModel(modifiedSkin, SkinPart.RIGHT_ARM, scale);
    }

    private static BufferedImage convertInSteveModel(BufferedImage playerSkin, SkinPart skinPart, int scale) {
        BufferedImage modifiedSkin = convertInSteveModel(playerSkin, skinPart.x(), skinPart.y(), scale);
        return convertInSteveModel(modifiedSkin, skinPart.hatX(), skinPart.hatY(), scale);
    }

    private static BufferedImage convertInSteveModel(BufferedImage skin, int x, int y, int scale) {
        x *= scale;
        y *= scale;
        int imageSize = 64 * scale;
        int space = 4 * scale;
        int steveArmWidth = 4 * scale;
        int alexArmWidth = 3 * scale;
        int skinPartSize = 16 * scale;
        BufferedImage bufferedImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = bufferedImage.createGraphics();

        // copy skin
        g2d.drawImage(skin, 0, 0, imageSize, imageSize, 0, 0, imageSize, imageSize, null);
        // clear skin part
        g2d.setBackground(new Color(0, 0, 0, 0));
        g2d.clearRect(x, y, skinPartSize, skinPartSize);
        // copy side 1
        g2d.drawImage(skin, x, y + space, x + steveArmWidth, y + skinPartSize, x, y + space, x + steveArmWidth, y + skinPartSize, null);
        // stretching face 2
        g2d.drawImage(skin, x + steveArmWidth, y + space, x + steveArmWidth * 2, y + skinPartSize, x + steveArmWidth, y + space, x + steveArmWidth + alexArmWidth, y + skinPartSize, null);
        // moving face 3
        g2d.drawImage(skin, x + steveArmWidth * 2, y + space, x + steveArmWidth * 3, y + skinPartSize, x + steveArmWidth + alexArmWidth, y + space, x + steveArmWidth * 2 + alexArmWidth, y + skinPartSize, null);
        // stretching and moving face 4
        g2d.drawImage(skin, x + steveArmWidth * 3, y + space, x + steveArmWidth * 4, y + skinPartSize, x + steveArmWidth * 2 + alexArmWidth, y + space, x + steveArmWidth * 2 + alexArmWidth * 2, y + skinPartSize, null);
        // stretching top/down face 1
        g2d.drawImage(skin, x + space, y, x + steveArmWidth + space, y + space, x + space, y, x + alexArmWidth + space, y + space, null);
        // stretching and moving top/down face 2
        g2d.drawImage(skin, x + space + steveArmWidth, y, x + steveArmWidth * 2 + space, y + space, x + space + alexArmWidth, y, x + alexArmWidth * 2 + space, y + space, null);

        g2d.dispose();
        return bufferedImage;
    }

    // === BEGIN OF 'FILTHY RICH CLIENT' CODE ===

    /*
     * https://github.com/romainguy/filthy-rich-clients/blob/master/Images/PictureScaler/src/PictureScaler.java
     *
     * Created on May 1, 2007, 5:03 PM
     *
     * Copyright (c) 2007, Sun Microsystems, Inc
     * All rights reserved.
     *
     * Redistribution and use in source and binary forms, with or without
     * modification, are permitted provided that the following conditions
     * are met:
     *
     *   * Redistributions of source code must retain the above copyright
     *     notice, this list of conditions and the following disclaimer.
     *   * Redistributions in binary form must reproduce the above
     *     copyright notice, this list of conditions and the following
     *     disclaimer in the documentation and/or other materials provided
     *     with the distribution.
     *   * Neither the name of the TimingFramework project nor the names of its
     *     contributors may be used to endorse or promote products derived
     *     from this software without specific prior written permission.
     *
     * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
     * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
     * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
     * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
     * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
     * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
     * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
     * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
     * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
     * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
     * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
     */

    /**
     * Convenience method that returns a scaled instance of the
     * provided BufferedImage.
     *
     *
     * @param image the original image to be scaled
     * @param targetWidth the desired width of the scaled instance,
     *    in pixels
     * @param targetHeight the desired height of the scaled instance,
     *    in pixels
     * @param progressiveBilinear if true, this method will use a multi-step
     *    scaling technique that provides higher quality than the usual
     *    one-step technique (only useful in down-scaling cases, where
     *    targetWidth or targetHeight is
     *    smaller than the original dimensions)
     * @return a scaled version of the original BufferedImage
     * @author Chet
     */
    public static BufferedImage fastResizeImage(BufferedImage image, int targetWidth,
                                                int targetHeight, boolean progressiveBilinear) {
        boolean isTranslucent = image.getTransparency() != Transparency.OPAQUE;
        int type = isTranslucent ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage resizedImage = image;
        BufferedImage scratchImage = null;
        Graphics2D g2d = null;
        int prevWidth = resizedImage.getWidth();
        int prevHeight = resizedImage.getHeight();
        boolean isUpscale = prevWidth < targetWidth || prevHeight < targetHeight;

        do {
            int[] dimensions = calculateDimensions(prevWidth, prevHeight, targetWidth, targetHeight, progressiveBilinear, isUpscale);
            int width = dimensions[0];
            int height = dimensions[1];

            if (scratchImage == null || isTranslucent) {
                scratchImage = new BufferedImage(width, height, type);

                if (g2d != null) {
                    g2d.dispose();
                }

                g2d = scratchImage.createGraphics();
            }

            drawResizedImage(g2d, resizedImage, prevWidth, prevHeight, width, height);
            prevWidth = width;
            prevHeight = height;
            resizedImage = scratchImage;
        } while (prevWidth != targetWidth || prevHeight != targetHeight);

        g2d.dispose();

        return resizeFinalImageIfNeeded(resizedImage, targetWidth, targetHeight, type);
    }

    private static int[] calculateDimensions(int width, int height, int targetWidth, int targetHeight,
                                             boolean progressiveBilinear, boolean isUpscale) {

        if (progressiveBilinear) {
            width = isUpscale ? Math.max(width / 2, targetWidth) : Math.min(width * 2, targetWidth);
            height = isUpscale ? Math.max(height / 2, targetHeight) : Math.min(height * 2, targetHeight);
        } else {
            width = targetWidth;
            height = targetHeight;
        }

        return new int[]{width, height};
    }

    private static void drawResizedImage(Graphics2D g2, BufferedImage src, int srcWidth, int srcHeight, int destWidth, int destHeight) {
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(src, 0, 0, destWidth, destHeight, 0, 0, srcWidth, srcHeight, null);
    }

    private static BufferedImage resizeFinalImageIfNeeded(BufferedImage image, int targetWidth, int targetHeight, int type) {
        if (targetWidth != image.getWidth() || targetHeight != image.getHeight()) {
            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, type);
            Graphics2D g2 = resizedImage.createGraphics();
            g2.drawImage(image, 0, 0, null);
            g2.dispose();
            return resizedImage;
        }
        return image;
    }

    // === END OF 'FILTHY RICH CLIENT' CODE ===

}
