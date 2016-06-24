package me.dags.installer.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * @author dags <dags@dags.me>
 */
public class ParallaxImage extends JPanel implements MouseMotionListener, ComponentListener {

    private int mouseX = 0;
    private int mouseY = 0;

    private final double scalar;
    private final ImageResource background;

    private ImageResource overlay;
    private int marginLeft = 0;
    private int marginTop = 0;
    private int marginRight = 0;
    private int marginBottom = 0;

    public ParallaxImage(ImageResource imageResource, double scalar) {
        scalar = scalar < 0 ? -scalar : scalar;
        this.background = imageResource;
        this.scalar = scalar < 1 ? 1 + scalar : scalar;
        this.addMouseMotionListener(this);
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        resize();

        int imageWidth = (int) (this.getWidth() * scalar);
        int imageHeight = (int) (this.getHeight() * scalar);

        int panelCenterX = this.getWidth() / 2, panelCenterY = this.getHeight() / 2;
        int imageCenterX = imageWidth / 2, imageCenterY = imageHeight / 2;

        int xPos = panelCenterX - imageCenterX;
        int yPos = panelCenterY - imageCenterY;

        double xFact = 1 - ((double) panelCenterX / (double) imageCenterX);
        double yFact = 1 - ((double) panelCenterY / (double) imageCenterY);

        int xOff = (this.getWidth() / 2) - mouseX;
        int yOff = (this.getHeight() / 2) - mouseY;

        int xDrawPos = xPos + (int) (xOff * xFact);
        int yDrawPos = yPos + (int) (yOff * yFact);

        g.drawImage(background.getInstance(), xDrawPos, yDrawPos, this);

        if (overlay != null) {
            int x = marginLeft != -1 ? marginLeft : marginRight != -1 ? getWidth() - marginRight : 0;
            int y = marginTop != - 1 ? marginTop : marginBottom != -1 ? getHeight() - marginBottom : 0;
            g.drawImage(overlay.getInstance(), x, y, this);
        }

        repaint();
    }

    public void resize() {
        int scaledHeight = (int) (this.getHeight() * scalar);
        int scaledWidth = (int) (background.getActualWidth() * scalar);
        background.updateSize(scaledWidth, scaledHeight);
    }

    public void setOverlay(ImageResource overlay, int marginLeft, int marginTop, int marginRight, int marginBottom) {
        this.overlay = overlay;
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
        this.marginBottom = marginBottom;
        this.marginTop = marginTop;
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        resize();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
}
