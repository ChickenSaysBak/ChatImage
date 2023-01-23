// ChatImage © 2023 ChickenSaysBak
// This code is licensed under MIT license (see LICENSE file for details).
package me.chickensaysbak.chatimage.core;

import com.mortennobel.imagescaling.ResampleFilters;
import com.mortennobel.imagescaling.ResampleOp;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageMaker {

    public static final String SOLID_PIXEL = "█";
    public static final String TRANSPARENT_PIXEL = "▒";
    public static final String BLANK_PIXEL = ChatColor.BOLD + " " + ChatColor.RESET + " ";

    public static final int MID_ALPHA = 128;
    public static final int LOW_ALPHA = 64;

    private ImageMaker() {}

    /**
     * Adds text to the right side of a Minecraft chat image
     * @param chatImage the Minecraft chat image to add text to
     * @param text the text to add
     * @return the updated Minecraft chat image with text
     */
    public static TextComponent addText(TextComponent chatImage, String text) {

        List<BaseComponent> components = new ArrayList<>(chatImage.getExtra());
        List<BaseComponent> newComponents = new ArrayList<>();

        String[] lines = text.split("\\n");
        int currentLine = 0;

        for (BaseComponent component : components) {

            // Adds text before existing image line breaks; text is only added if it matches the corresponding line number.
            if (currentLine < lines.length && component.toPlainText().equals("\n")) {
                String currentText = ChatColor.translateAlternateColorCodes('&', " " + lines[currentLine]);
                newComponents.addAll(Arrays.asList(TextComponent.fromLegacyText(currentText)));
                ++currentLine;
            }

            newComponents.add(component);

        }

        TextComponent result = chatImage.duplicate();
        result.setExtra(newComponents);
        return result;

    }

    /**
     * Creates a Minecraft chat image based on the provided parameters.
     * @param original the base image
     * @param maximum the maximum dimension an image can be to fit in the chat box
     * @param smooth whether to use smooth rendering or not
     * @param trim whether to trim transparency or not
     * @return a Minecraft chat image
     */
    public static TextComponent createChatImage(BufferedImage original, Dimension maximum, boolean smooth, boolean trim) {

        boolean hasPartialTransparency = hasPartialTransparency(original);

        BufferedImage image = original;
        if (trim) image = trimTransparency(image, hasPartialTransparency); // Ran 1st to ensure best possible resolution pre-resize.
        Dimension scaled = getScaledDimension(image, maximum);
        image = smooth ? resizeImageSmooth(image, scaled) : resizeImage(image, scaled);
        if (trim) image = trimTransparency(image, hasPartialTransparency); // Ran again to remove excess whitespace post-resize.

        return convertToText(image, hasPartialTransparency);

    }

    /**
     * Converts an image to a text based image that can be sent to Minecraft chat.
     * @param image the image to convert
     * @param hasPartialTransparency whether to use transparent pixels or not
     * @return a text based image
     */
    private static TextComponent convertToText(BufferedImage image, boolean hasPartialTransparency) {

        TextComponent result = new TextComponent();

        for (int y = 0; y < image.getHeight(); ++y) {

            for (int x = 0; x < image.getWidth(); ++x) {

                int rgb = image.getRGB(x, y);
                int alpha = new Color(rgb, true).getAlpha();
                TextComponent pixel = new TextComponent();

                if (isPixelVisible(alpha, hasPartialTransparency) && isPixelNormal(rgb)) {

                    String standardHex = "#" + Integer.toHexString(rgb).substring(2); // Strips alpha characters.
                    pixel.setColor(ChatColor.of(standardHex));

                    pixel.setText(alpha > MID_ALPHA ? SOLID_PIXEL : TRANSPARENT_PIXEL);
                    if (hasPartialTransparency && alpha <= LOW_ALPHA) pixel.setFont("minecraft:uniform"); // Uses smaller pixels.

                } else pixel.setText(BLANK_PIXEL);

                result.addExtra(pixel);

            }

            if (y < image.getHeight()-1) result.addExtra("\n");

        }

        return result;

    }

    /**
     * Trims off transparent edges of an image
     * @param original the image to trim
     * @param hasPartialTransparency whether to acknowledge transparent pixels or not
     * @return the trimmed image
     */
    private static BufferedImage trimTransparency(BufferedImage original, boolean hasPartialTransparency) {

        int width = original.getWidth(), height = original.getHeight();

        // Sides start on opposite ends and work their way across as pixels are found.
        // Note that the Y-axis is reversed for image coordinates, so top and bottom are flipped.
        int top = height, bottom = 0, left = width, right = 0;

        for (int x = 0; x < width; ++x) for (int y = 0; y < height; ++y) {

            int rgb = original.getRGB(x, y);
            int alpha = new Color(rgb, true).getAlpha();

            if (isPixelVisible(alpha, hasPartialTransparency) && isPixelNormal(rgb)) {
                top = Math.min(top, y);
                bottom = Math.max(bottom, y);
                left = Math.min(left, x);
                right = Math.max(right, x);
            }

        }

        int newWidth = right - left + 1, newHeight = bottom - top + 1; // 1 is added since coordinates start at 0.
        return original.getSubimage(left, top, newWidth, newHeight);

    }

    /**
     * Resizes an image using smooth rendering; if failure occurs, simple rendering is used instead. Depends on java-image-scaling.
     * @param original the image to resize
     * @param dimension the dimension to resize to
     * @return the resized image
     */
    private static BufferedImage resizeImageSmooth(BufferedImage original, Dimension dimension) {

        int width = (int) dimension.getWidth(), height = (int) dimension.getHeight();

        try {
            ResampleOp resizeOp = new ResampleOp(width, height);
            resizeOp.setFilter(ResampleFilters.getLanczos3Filter());
            return resizeOp.filter(original, null);
        }

        catch (RuntimeException e) {
            return resizeImage(original, dimension);
        }

    }

    /**
     * Resizes an image using simple rendering. Does not require dependencies.
     * @param original the image to resize
     * @param dimension the dimension to resize to
     * @return the resized image
     */
    private static BufferedImage resizeImage(BufferedImage original, Dimension dimension) {

        double width = dimension.getWidth(), height = dimension.getHeight();

        AffineTransform af = new AffineTransform();
        af.scale(width / (double) original.getWidth(), height / (double) original.getHeight());
        return new AffineTransformOp(af, AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(original, null);

    }

    /**
     * Gets the scaled dimension of an image within the provided bounds. Maintains aspect ratio.
     * @param original the image to scale
     * @param bounds the maximum size the image can be
     * @return the scaled dimension of the image
     */
    private static Dimension getScaledDimension(BufferedImage original, Dimension bounds) {

        int original_width = original.getWidth(), original_height = original.getHeight();
        int bound_width = bounds.width, bound_height = bounds.height;
        int new_width = original_width, new_height = original_height;

        if (original_width > bound_width) {
            new_width = bound_width;
            new_height = (new_width * original_height) / original_width;
        }

        if (new_height > bound_height) {
            new_height = bound_height;
            new_width = (new_height * original_width) / original_height;
        }

        return new Dimension(new_width, new_height);

    }

    /**
     * Checks if an image has a significant number of partially transparent pixels.
     * This is used to avoid randomly using transparent characters in places where they naturally look better solid.
     * @param original the image to check
     * @return true if at least 5% of the image's visible pixels are partially transparent
     */
    private static boolean hasPartialTransparency(BufferedImage original) {

        final double THRESHOLD = 0.05;

        int midAlphaPixels = 0;
        int visiblePixels = 0;

        for (int y = 0; y < original.getHeight(); ++y) for (int x = 0; x < original.getWidth(); ++x) {

            int rgb = original.getRGB(x, y);
            int alpha = new Color(rgb, true).getAlpha();

            if (alpha > 0 && alpha <= MID_ALPHA) ++midAlphaPixels;
            if (alpha > 0) ++visiblePixels;

        }

        double ratio = (double) midAlphaPixels/visiblePixels;
        return ratio >= THRESHOLD;

    }

    /**
     * Checks if a pixel is considered normal.
     * Used to ignore extraneous pixels found along the border of some transparent images.
     * @param rgb the rgb of the pixel to check
     * @return true if the pixel is normal
     */
    private static boolean isPixelNormal(int rgb) {
        return Integer.toHexString(rgb).length() == 8;
    }

    /**
     * Checks if a pixel should be visible.
     * If partial transparency is not supported for the image, the pixel's alpha must be high to be considered visible.
     * @param alpha the alpha value of the pixel to check
     * @param hasPartialTransparency whether to acknowledge transparent pixels or not
     * @return true if the pixel should be visible
     */
    private static boolean isPixelVisible(int alpha, boolean hasPartialTransparency) {
        return alpha > 0 && (hasPartialTransparency || alpha > MID_ALPHA);
    }

}