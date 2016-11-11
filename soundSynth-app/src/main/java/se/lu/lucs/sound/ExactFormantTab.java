package se.lu.lucs.sound;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.Hashtable;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import se.lu.lucs.sound.BoutParameters.Formant;

public class ExactFormantTab extends JPanel {

    private static void enableComponents( Container container, boolean enable ) {
        final Component[] components = container.getComponents();
        for (final Component component : components) {
            component.setEnabled( enable );
            if (component instanceof Container) {
                enableComponents( (Container) component, enable );
            }
        }
    }

    private boolean isExternalUpdate;
    private final JSlider f1_freq;
    private final JSlider f1_amp;
    private final JSlider f1_width;
    private final JSlider f2_freq;
    private final JSlider f2_amp;
    private final JSlider f2_width;
    private final JSlider f3_freq;
    private final JSlider f3_amp;
    private final JSlider f3_width;
    private final JSlider f4_freq;
    private final JSlider f4_amp;
    private final JSlider f4_width;

    public ExactFormantTab( BoutParameters boutParameters, Supplier<Void> updateSound ) {
        setBorder( BorderFactory.createEmptyBorder( GeneratorApplication.COMPONENT_PADDING, 100, GeneratorApplication.COMPONENT_PADDING, 100 ) );
        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

        //F1
        add( new JLabel( "F1 Frequency" ) );
        f1_freq = GeneratorApplication.generateSlider( BoutParameters.Formant.MIN_F1_FREQ, BoutParameters.Formant.MAX_F1_FREQ,
                        boutParameters.exactFormants.f1_freq, 100, ( e ) -> {
                            if (!isExternalUpdate) {
                                boutParameters.exactFormants.f1_freq = ((JSlider) e.getSource()).getValue();
                                updateSound.get();
                            }
                        } );
        add( f1_freq );
        add( new JLabel( "F1 Amplitude" ) );
        f1_amp = GeneratorApplication.generateSlider( BoutParameters.Formant.MIN_F1_AMP * 100, BoutParameters.Formant.MAX_F1_AMP * 100,
                        (int) boutParameters.exactFormants.f1_amp * 100, 100, ( e ) -> {
                            if (!isExternalUpdate) {
                                boutParameters.exactFormants.f1_amp = ((JSlider) e.getSource()).getValue() / 100.0;
                                updateSound.get();
                            }
                        } );
        final Hashtable<Integer, JComponent> labels = new Hashtable<>();
        for (int i = BoutParameters.Formant.MIN_F1_AMP * 100; i <= BoutParameters.Formant.MAX_F1_AMP * 100; i += 10) {
            labels.put( i, new JLabel( new Double( i / 100.0 ).toString() ) );
        }

        f1_amp.setLabelTable( labels );
        add( f1_amp );
        add( new JLabel( "F1 Bandwith" ) );
        f1_width = GeneratorApplication.generateSlider( BoutParameters.Formant.MIN_F1_WIDTH, BoutParameters.Formant.MAX_F1_WIDTH,
                        boutParameters.exactFormants.f1_width, 100, ( e ) -> {
                            if (!isExternalUpdate) {
                                boutParameters.exactFormants.f1_width = ((JSlider) e.getSource()).getValue();
                                updateSound.get();
                            }
                        } );
        add( f1_width );

        add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );

        //F2
        add( new JLabel( "F2 Frequency" ) );
        f2_freq = GeneratorApplication.generateSlider( BoutParameters.Formant.MIN_F2_FREQ, BoutParameters.Formant.MAX_F2_FREQ,
                        boutParameters.exactFormants.f2_freq, 500, ( e ) -> {
                            if (!isExternalUpdate) {
                                boutParameters.exactFormants.f2_freq = ((JSlider) e.getSource()).getValue();
                                updateSound.get();
                            }
                        } );
        add( f2_freq );
        add( new JLabel( "F2 Amplitude" ) );
        f2_amp = GeneratorApplication.generateSlider( BoutParameters.Formant.MIN_F2_AMP * 100, BoutParameters.Formant.MAX_F2_AMP * 100,
                        (int) boutParameters.exactFormants.f2_amp * 100, 100, ( e ) -> {
                            if (!isExternalUpdate) {
                                boutParameters.exactFormants.f2_amp = ((JSlider) e.getSource()).getValue() / 100.0;
                                updateSound.get();
                            }
                        } );
        final Hashtable<Integer, JComponent> labels2 = new Hashtable<>();
        for (int i = BoutParameters.Formant.MIN_F2_AMP * 100; i <= BoutParameters.Formant.MAX_F2_AMP * 100; i += 10) {
            labels2.put( i, new JLabel( new Double( i / 100.0 ).toString() ) );
        }

        f2_amp.setLabelTable( labels2 );
        add( f2_amp );
        add( new JLabel( "F2 Bandwith" ) );
        f2_width = GeneratorApplication.generateSlider( BoutParameters.Formant.MIN_F2_WIDTH, BoutParameters.Formant.MAX_F2_WIDTH,
                        boutParameters.exactFormants.f2_width, 100, ( e ) -> {
                            if (!isExternalUpdate) {
                                boutParameters.exactFormants.f2_width = ((JSlider) e.getSource()).getValue();
                                updateSound.get();
                            }
                        } );
        add( f2_width );

        add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );

        //F3
        add( new JLabel( "F3 Frequency" ) );
        f3_freq = GeneratorApplication.generateSlider( BoutParameters.Formant.MIN_F3_FREQ, BoutParameters.Formant.MAX_F3_FREQ,
                        boutParameters.exactFormants.f3_freq, 500, ( e ) -> {
                            if (!isExternalUpdate) {
                                boutParameters.exactFormants.f3_freq = ((JSlider) e.getSource()).getValue();
                                updateSound.get();
                            }
                        } );
        add( f3_freq );
        add( new JLabel( "F3 Amplitude" ) );
        f3_amp = GeneratorApplication.generateSlider( BoutParameters.Formant.MIN_F3_AMP * 100, BoutParameters.Formant.MAX_F3_AMP * 100,
                        (int) boutParameters.exactFormants.f3_amp * 100, 100, ( e ) -> {
                            if (!isExternalUpdate) {
                                boutParameters.exactFormants.f3_amp = ((JSlider) e.getSource()).getValue() / 100.0;
                                updateSound.get();
                            }
                        } );
        final Hashtable<Integer, JComponent> labels3 = new Hashtable<>();
        for (int i = BoutParameters.Formant.MIN_F3_AMP * 100; i <= BoutParameters.Formant.MAX_F3_AMP * 100; i += 10) {
            labels3.put( i, new JLabel( new Double( i / 100.0 ).toString() ) );
        }

        f3_amp.setLabelTable( labels3 );
        add( f3_amp );
        add( new JLabel( "F3 Bandwith" ) );
        f3_width = GeneratorApplication.generateSlider( BoutParameters.Formant.MIN_F3_WIDTH, BoutParameters.Formant.MAX_F3_WIDTH,
                        boutParameters.exactFormants.f3_width, 100, ( e ) -> {
                            if (!isExternalUpdate) {
                                boutParameters.exactFormants.f3_width = ((JSlider) e.getSource()).getValue();
                                updateSound.get();
                            }
                        } );
        add( f3_width );

        add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );

        //F4
        add( new JLabel( "F4 Frequency" ) );
        f4_freq = GeneratorApplication.generateSlider( BoutParameters.Formant.MIN_F4_FREQ, BoutParameters.Formant.MAX_F4_FREQ,
                        boutParameters.exactFormants.f4_freq, 500, ( e ) -> {
                            if (!isExternalUpdate) {
                                boutParameters.exactFormants.f4_freq = ((JSlider) e.getSource()).getValue();
                                updateSound.get();
                            }
                        } );
        add( f4_freq );
        add( new JLabel( "F4 Amplitude" ) );
        f4_amp = GeneratorApplication.generateSlider( BoutParameters.Formant.MIN_F4_AMP * 100, BoutParameters.Formant.MAX_F4_AMP * 100,
                        (int) boutParameters.exactFormants.f4_amp * 100, 100, ( e ) -> {
                            if (!isExternalUpdate) {
                                boutParameters.exactFormants.f4_amp = ((JSlider) e.getSource()).getValue() / 100.0;
                                updateSound.get();
                            }
                        } );
        final Hashtable<Integer, JComponent> labels4 = new Hashtable<>();
        for (int i = BoutParameters.Formant.MIN_F4_AMP * 100; i <= BoutParameters.Formant.MAX_F3_AMP * 100; i += 10) {
            labels4.put( i, new JLabel( new Double( i / 100.0 ).toString() ) );
        }

        f4_amp.setLabelTable( labels4 );
        add( f4_amp );
        add( new JLabel( "F4 Bandwith" ) );
        f4_width = GeneratorApplication.generateSlider( BoutParameters.Formant.MIN_F4_WIDTH, BoutParameters.Formant.MAX_F4_WIDTH,
                        boutParameters.exactFormants.f4_width, 100, ( e ) -> {
                            if (!isExternalUpdate) {
                                boutParameters.exactFormants.f4_width = ((JSlider) e.getSource()).getValue();
                                updateSound.get();
                            }
                        } );
        add( f4_width );

        setEnabled( false );
    }

    @Override
    public void setEnabled( boolean e ) {
        super.setEnabled( e );

        enableComponents( this, e );
    }

    public void update( Formant formant ) {
        isExternalUpdate = true;
        f1_freq.setValue( formant.f1_freq );
        f1_amp.setValue( (int) (formant.f1_amp * 100) );
        f1_width.setValue( formant.f1_width );
        f2_freq.setValue( formant.f2_freq );
        f2_amp.setValue( (int) (formant.f2_amp * 100) );
        f2_width.setValue( formant.f2_width );
        f3_freq.setValue( formant.f3_freq );
        f3_amp.setValue( (int) (formant.f3_amp * 100) );
        f3_width.setValue( formant.f3_width );
        f4_freq.setValue( formant.f4_freq );
        f4_amp.setValue( (int) (formant.f4_amp * 100) );
        f4_width.setValue( formant.f4_width );
        isExternalUpdate = false;
    }
}
