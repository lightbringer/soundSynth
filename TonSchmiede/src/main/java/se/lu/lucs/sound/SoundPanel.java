package se.lu.lucs.sound;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.PanelUI;

public class SoundPanel extends JPanel implements MouseListener {

    private static class SoundPanelUI extends PanelUI {

        @Override
        public void paint( Graphics g, JComponent c ) {
            super.paint( g, c );

            final SoundPanel sp = (SoundPanel) c;

            if (sp.isPlaying) {
                g.setColor( Color.GREEN );
            }
            else {
                if (sp.isSelected) {
                    g.setColor( Color.BLUE );
                }
                else {
                    g.setColor( Color.RED );
                }
            }
            g.fillRect( 0, 0, c.getWidth(), c.getHeight() );
            if (sp.previewImage != null) {
                g.drawImage( sp.previewImage, 10, 10, c.getWidth() - 10, c.getHeight() - 10, 0, 0, sp.previewImage.getWidth(), sp.previewImage.getHeight(),
                                null );
            }
            else {
                g.clearRect( 10, 10, c.getWidth() - 10, c.getHeight() - 10 );

                g.drawString( sp.parameters.toString(), -50, 100 );
            }
        }

    }

    private final static SoundPanelUI UI = new SoundPanelUI();
    public static final Color LIGHT_BLUE = new Color( 128, 192, 255 );
    public static final Color DARK_BLUE = new Color( 0, 0, 127 );
    private static SoundPanel playing;
    private final BufferedImage previewImage;

    private boolean isPlaying;
    private boolean isSelected;

    private BoutParameters parameters;

    private byte[] audioBytes;

    private final TonSchmiede schmiede;

    public SoundPanel( TonSchmiede s ) {
        setUI( UI );
        addMouseListener( this );
        schmiede = s;
        previewImage = new BufferedImage( 500, 100, BufferedImage.TYPE_INT_ARGB );

    }

    public byte[] getAudioBytes() {
        return audioBytes;
    }

    public BoutParameters getParameters() {
        return parameters;
    }

    public boolean isSelected() {
        return isSelected;
    }

    private void makePath( List<Double> samples ) {

        final Path2D.Float current = new Path2D.Float();

        final float hd2 = previewImage.getHeight() / 2f;

        current.moveTo( 0, hd2 );
        for (int frame = 0; frame < samples.size(); frame++) {

            current.lineTo( (float) frame / samples.size() * previewImage.getWidth(), hd2 - samples.get( frame ) * hd2 * 1000 );
        }

        final Graphics2D g2d = (Graphics2D) previewImage.getGraphics();

        g2d.setBackground( Color.BLACK );
        g2d.clearRect( 0, 0, previewImage.getWidth(), previewImage.getHeight() );

        g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        g2d.setRenderingHint( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE );

        g2d.setPaint( Color.WHITE );
        g2d.draw( current );

    }

    @Override
    public void mouseClicked( MouseEvent e ) {

    }

    @Override
    public void mouseEntered( MouseEvent e ) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited( MouseEvent e ) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed( MouseEvent e ) {
        repaint();
        if (e.getButton() == MouseEvent.BUTTON3) {
            isSelected = !isSelected;
        }
        else {

            if (playing != null) {
                playing.isPlaying = false;
                schmiede.stopPlay();
            }
            isPlaying = true;
            schmiede.playCurve( this );
        }

    }

    @Override
    public void mouseReleased( MouseEvent e ) {

    }

    public void setAmplitude( List<Double> ampl ) {
        makePath( ampl );
        final Double max = ampl.stream().collect( Collectors.summarizingDouble( Double::doubleValue ) ).getMax();

        final ByteBuffer audioBuffer = ByteBuffer.allocate( ampl.size() * Short.BYTES );
        final double pow = 25536;
        int index = 0;
//        final Graphics g = previewImage.getGraphics();
//        g.clearRect( 0, 0, ampl.size(), 100 );
//        g.setColor( Color.GREEN );
        for (final Double d : ampl) {
            final int v = (int) (d.doubleValue() / max * pow);
            if (v > Short.MAX_VALUE) {
                System.out.println( "ping" );
            }
            audioBuffer.put( (byte) v );
            audioBuffer.put( (byte) (v >> 8) );
//            g.drawOval( index, index, 10, 10 );

            index++;
        }

        isSelected = false;
        isPlaying = false;
        audioBytes = audioBuffer.array();

        repaint();
    }

    public void setParameters( BoutParameters parameters ) {
        this.parameters = parameters;
    }

    public void setPlaying( boolean b ) {
        if (isPlaying != b) {
            SwingUtilities.invokeLater( () -> repaint() );
        }
        isPlaying = b;

    }
}
