/* -----------------
 * CircleDrawer.java
 * -----------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 *
 */

package se.lu.lucs.sound;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.jfree.ui.Drawable;

/**
 * An implementation of the {@link Drawable} interface, to illustrate the use of the
 * {@link org.jfree.chart.annotations.XYDrawableAnnotation} class.  Used by
 * MarkerDemo1.java.
 */
public class CircleDrawer implements Drawable {

    /** The outline paint. */
    private final Paint outlinePaint;

    /** The outline stroke. */
    private final Stroke outlineStroke;

    /** The fill paint. */
    private final Paint fillPaint;

    /**
     * Creates a new instance.
     *
     * @param outlinePaint  the outline paint.
     * @param outlineStroke  the outline stroke.
     * @param fillPaint  the fill paint.
     */
    public CircleDrawer( Paint outlinePaint, Stroke outlineStroke, Paint fillPaint ) {
        this.outlinePaint = outlinePaint;
        this.outlineStroke = outlineStroke;
        this.fillPaint = fillPaint;
    }

    /**
     * Draws the circle.
     *
     * @param g2  the graphics device.
     * @param area  the area in which to draw.
     */
    @Override
    public void draw( Graphics2D g2, Rectangle2D area ) {
        final Ellipse2D ellipse = new Ellipse2D.Double( area.getX(), area.getY(), area.getWidth(), area.getHeight() );
        if (fillPaint != null) {
            g2.setPaint( fillPaint );
            g2.fill( ellipse );
        }
        if (outlinePaint != null && outlineStroke != null) {
            g2.setPaint( outlinePaint );
            g2.setStroke( outlineStroke );
            g2.draw( ellipse );
        }

    }
}