package me.dags.installer.ui;

import me.dags.installer.Launcher;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public class ImageLayer {

    private final BufferedImage bufferedImage;
    static int derp = 1;

    private Image instance;
    private int currentWidth;
    private int currentHeight;
    private double factor = 0.2;

    private boolean cover = false;
    private int marginTop = 0;
    private int marginLeft = 0;

    public ImageLayer(String path) throws IOException {
        bufferedImage = ImageIO.read(Launcher.class.getResource(path));
        instance = bufferedImage;
        currentWidth = bufferedImage.getWidth();
        currentHeight = bufferedImage.getHeight();
        factor *= derp++;
    }

    public ImageLayer(String path, double fact) throws IOException {
        bufferedImage = ImageIO.read(Launcher.class.getResource(path));
        instance = bufferedImage;
        currentWidth = bufferedImage.getWidth();
        currentHeight = bufferedImage.getHeight();
        factor = fact;
    }

    public boolean cover() {
        return cover;
    }

    public int getHeight() {
        return currentHeight;
    }

    public int getWidth() {
        return currentWidth;
    }

    public int getMarginTop() {
        return marginTop;
    }

    public int getMarginLeft() {
        return marginLeft;
    }

    public double getFactor() {
        return factor;
    }

    public ImageLayer setCover(boolean cover) {
        this.cover = cover;
        return this;
    }

    public ImageLayer scale(double factor) {
        int width = (int) (getWidth() * factor);
        int height = (int) (getHeight() * factor);
        updateSize(width, height);
        return this;
    }

    public ImageLayer updateSize(int width, int height) {
        if (width != currentWidth || height != currentHeight) {
            resize(width, height);
        }
        return this;
    }

    public ImageLayer margins(int top, int left) {
        this.marginTop = top;
        this.marginLeft = left;
        return this;
    }

    public Image resize(int width, int height) {
        return instance = bufferedImage.getScaledInstance(currentWidth = width, currentHeight = height, Image.SCALE_SMOOTH);
    }

    public Image getInstance() {
        return instance;
    }
}
