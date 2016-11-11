package se.lu.lucs.sound;

import java.awt.Dimension;
import java.util.Hashtable;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class BreathingTab extends JPanel {

    public BreathingTab( BoutParameters boutParameters, Supplier<Void> updateSound ) {
        setBorder( BorderFactory.createEmptyBorder( GeneratorApplication.COMPONENT_PADDING, 100, GeneratorApplication.COMPONENT_PADDING, 100 ) );
        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

        add( new JLabel( "Add Breathing" ) );

        final JCheckBox beforeCheckBox = new JCheckBox( "Before" );
        beforeCheckBox.setSelected( boutParameters.breathingType.contains( BoutParameters.BreathingType.BEFORE ) );
        beforeCheckBox.addActionListener( ( e ) -> {
            if (beforeCheckBox.isSelected()) {
                boutParameters.breathingType.add( BoutParameters.BreathingType.BEFORE );
            }
            else {
                boutParameters.breathingType.remove( BoutParameters.BreathingType.BEFORE );
            }
            updateSound.get();
        } );
        add( beforeCheckBox );

        final JCheckBox duringCheckBox = new JCheckBox( "During" );
        duringCheckBox.setSelected( boutParameters.breathingType.contains( BoutParameters.BreathingType.DURING ) );
        duringCheckBox.addActionListener( ( e ) -> {
            if (duringCheckBox.isSelected()) {
                boutParameters.breathingType.add( BoutParameters.BreathingType.DURING );
            }
            else {
                boutParameters.breathingType.remove( BoutParameters.BreathingType.DURING );
            }
            updateSound.get();
        } );
        add( duringCheckBox );

        final JCheckBox afterCheckBox = new JCheckBox( "After" );
        afterCheckBox.setSelected( boutParameters.breathingType.contains( BoutParameters.BreathingType.AFTER ) );
        afterCheckBox.addActionListener( ( e ) -> {
            if (afterCheckBox.isSelected()) {
                boutParameters.breathingType.add( BoutParameters.BreathingType.AFTER );
            }
            else {
                boutParameters.breathingType.remove( BoutParameters.BreathingType.AFTER );
            }
            updateSound.get();
        } );
        add( afterCheckBox );

        add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );
        add( new JLabel( "Breathing Strength" ) );
        final JSlider breathingStrengthSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_BREATHING_STRENGTH * 100,
                        BoutParameters.MAX_BREATHING_STRENGTH * 100, (int) (boutParameters.breathingStrength * 100), 100, ( e ) -> {
                            boutParameters.breathingStrength = ((JSlider) e.getSource()).getValue() / 100.0;
                            updateSound.get();
                        } );
        final Hashtable<Integer, JComponent> labels = new Hashtable<>();
        for (int i = BoutParameters.MIN_BREATHING_STRENGTH * 100; i <= BoutParameters.MAX_BREATHING_STRENGTH * 100; i += 10) {
            labels.put( i, new JLabel( new Double( i / 100.0 ).toString() ) );
        }

        breathingStrengthSlider.setLabelTable( labels );
        add( breathingStrengthSlider );

        add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );
        add( new JLabel( "Breathing dynamics" ) );
        final JSlider breathingDynamicsSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_BREATHING_DYNAMIC, BoutParameters.MAX_BREATHING_DYNAMIC,
                        (int) boutParameters.breathingStrength_diff, 2, ( e ) -> {
                            boutParameters.breathingStrength_diff = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );

        add( breathingDynamicsSlider );

        add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );
        add( new JLabel( "Breathing length" ) );
        final JSlider breathingLengthSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_BREATHING_LENGTH, BoutParameters.MAX_BREATHING_LENGTH,
                        boutParameters.breathing_dur, 100, ( e ) -> {
                            boutParameters.breathing_dur = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );
        add( breathingLengthSlider );
    }

}
