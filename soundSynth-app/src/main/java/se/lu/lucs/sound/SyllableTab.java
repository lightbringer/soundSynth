package se.lu.lucs.sound;

import java.awt.Dimension;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class SyllableTab extends JPanel {
    public SyllableTab( BoutParameters boutParameters, Supplier<Void> updateSound ) {
        setBorder( BorderFactory.createEmptyBorder( GeneratorApplication.COMPONENT_PADDING, 100, GeneratorApplication.COMPONENT_PADDING, 100 ) );
        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        add( new JLabel( "Number of Syllables" ) );
        final JSlider numSyllabesSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_SYLLABLES, BoutParameters.MAX_SYLLABLES, boutParameters.numberOfSyllables,
                        1, ( e ) -> {
                            boutParameters.numberOfSyllables = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );

        add( numSyllabesSlider );
        add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );

        add( new JLabel( "Mean syllable length" ) );
        final JSlider meanSyllableLengthSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_MEAN_SYLLABLE_LENGTH,
                        BoutParameters.MAX_MEAN_SYLLABLE_LENGTH, boutParameters.syllableDuration_mean, 500, ( e ) -> {
                            boutParameters.syllableDuration_mean = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );

        add( meanSyllableLengthSlider );
        add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );

        add( new JLabel( "Mean pause between syllables" ) );
        final JSlider syllablePauseSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_SYLLABLE_PAUSE, BoutParameters.MAX_SYLLABLE_PAUSE,
                        boutParameters.pauseDuration_mean, 500, ( e ) -> {
                            boutParameters.pauseDuration_mean = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );
        add( syllablePauseSlider );
        add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );

        add( new JLabel( "How much syllables vary" ) );
        final JSlider syllableVariationSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_SYLLABLE_VARIATION,
                        BoutParameters.MAX_SYLLABLE_VARIATION, boutParameters.var_bw_syllables, 100, ( e ) -> {
                            boutParameters.var_bw_syllables = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );
        add( syllableVariationSlider );
    }
}
