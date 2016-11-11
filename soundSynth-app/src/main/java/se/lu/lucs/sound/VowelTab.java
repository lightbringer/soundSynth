package se.lu.lucs.sound;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Hashtable;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import se.lu.lucs.sound.BoutParameters.Formant;

public class VowelTab extends JPanel {
    //TODO U lacked two values for f4
    public enum Vowel {
        RANDOM( null ), I( new Formant( 250, 2800, 4100, 5100, 1, 1, 1, 1, 50, 250, 300, 200 ) ), A( new Formant() ), O( new Formant( 550, 700, 3100, 4100, .9,
                        .9, 1, 1, 50, 50, 200, 200 ) ), U( new Formant( 270, 600, 4000, 4300, 1, .9, .8, 0, 50, 50, 300, 400 ) ), E(
                                        new Formant( 440, 2600, 3400, 4200, .8, .8, .7, .7, 50, 175, 200, 200 ) ), SCHWA(
                                                        new Formant( 700, 1400, 2800, 3500, .7, .7, .7, .7, 100, 100, 100, 100 ) ), CUSTOM( null );

        final Formant formant;

        private Vowel( Formant f ) {
            formant = f;
        }
    }

    private ExactFormantTab exactFormantTab;

    public VowelTab( BoutParameters boutParameters, Supplier<Void> updateSound ) {
        assert boutParameters.randomVowel;

        setBorder( BorderFactory.createEmptyBorder( GeneratorApplication.COMPONENT_PADDING, 100, GeneratorApplication.COMPONENT_PADDING, 100 ) );
        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

        add( new JLabel( "Vowel" ) );
        final JComboBox<Vowel> vowelBox = new JComboBox<>( Vowel.values() );
        vowelBox.setSelectedItem( Vowel.RANDOM );
        vowelBox.addActionListener( ( e ) -> {
            final Vowel v = (Vowel) ((JComboBox<Vowel>) e.getSource()).getSelectedItem();
            switch (v) {
                case RANDOM:
                    exactFormantTab.setEnabled( false );
                    boutParameters.randomVowel = true;
                    break;
                case CUSTOM:
                    exactFormantTab.setEnabled( true );
                    boutParameters.randomVowel = false;
                    break;
                default:
                    exactFormantTab.setEnabled( false );
                    boutParameters.randomVowel = false;
                    exactFormantTab.update( v.formant );
                    boutParameters.exactFormants = new Formant( v.formant );
                    break;
            }
            updateSound.get();
        } );
        add( vowelBox );

        add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );
        add( new JLabel( "Formant Strength" ) );
        final JSlider formantStrenghSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_FORMANT_STRENGTH, BoutParameters.MAX_FORMANT_STRENGTH,
                        boutParameters.formantStrength, 10, ( e ) -> {
                            boutParameters.formantStrength = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );
        add( formantStrenghSlider );
        add( createAdvancedGroup( boutParameters, updateSound ) );
        add( Box.createVerticalGlue() );
    }

    private Component createAdvancedGroup( BoutParameters boutParameters, Supplier<Void> updateSound ) {
        final JPanel g = new JPanel();
        g.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEmptyBorder( GeneratorApplication.COMPONENT_PADDING,
                        GeneratorApplication.COMPONENT_PADDING, GeneratorApplication.COMPONENT_PADDING, 200 ), "Advanced spectral settings" ) );
        g.setLayout( new BoxLayout( g, BoxLayout.Y_AXIS ) );

        g.add( new JLabel( "Rolloff of harmonics" ) );
        final JSlider rolloffSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_ROLLOFF, BoutParameters.MAX_ROLLOFF, boutParameters.rolloff, 10,
                        ( e ) -> {
                            boutParameters.rolloff = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );
        g.add( rolloffSlider );

        g.add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );
        g.add( new JLabel( "Spectral slope" ) );
        final JSlider spectralSlopeSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_SPECTRAL_SLOPE * 100,
                        BoutParameters.MAX_SPECTRAL_SLOPE * 100, (int) (boutParameters.spectralSlope * 100), 10, ( e ) -> {
                            boutParameters.spectralSlope = ((JSlider) e.getSource()).getValue() / 100.0;
                            updateSound.get();
                        } );
        final Hashtable<Integer, JComponent> labels = new Hashtable<>();
        for (int i = BoutParameters.MIN_SPECTRAL_SLOPE * 100; i <= BoutParameters.MAX_SPECTRAL_SLOPE * 100; i += 10) {
            labels.put( i, new JLabel( new Double( i / 100.0 ).toString() ) );
        }

        spectralSlopeSlider.setLabelTable( labels );
        g.add( spectralSlopeSlider );

        g.add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );
        g.add( new JLabel( "Spectral noise strength" ) );
        final JSlider spectralNoiseSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_SPECTRAL_NOISE, BoutParameters.MAX_SPECTRAL_NOISE,
                        boutParameters.spectralNoise_strength, 10, ( e ) -> {
                            boutParameters.spectralNoise_strength = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );

        g.add( spectralNoiseSlider );

        g.add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );
        g.add( new JLabel( "Emphasize 6 ± 2 kHz" ) );
        final JSlider megaFormantStrengthSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_MEGA_FORMANT_STRENGTH,
                        BoutParameters.MAX_MEGA_FORMANT_STRENGTH, boutParameters.megaFormant_strength, 10, ( e ) -> {
                            boutParameters.megaFormant_strength = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );

        g.add( megaFormantStrengthSlider );

        return g;
    }

    public void setExactFormantTab( ExactFormantTab exactFormantTab ) {
        this.exactFormantTab = exactFormantTab;
    }

}
