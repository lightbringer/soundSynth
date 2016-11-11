package se.lu.lucs.sound;

import java.awt.Component;
import java.awt.Dimension;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class IntonationTab extends JPanel {

    public IntonationTab( BoutParameters boutParameters, Supplier<Void> updateSound ) {
        setBorder( BorderFactory.createEmptyBorder( GeneratorApplication.COMPONENT_PADDING, 100, GeneratorApplication.COMPONENT_PADDING, 100 ) );
        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        add( createPitchGroup( boutParameters, updateSound ) );
        add( createModulationGroup( boutParameters, updateSound ) );
    }

    private Component createModulationGroup( BoutParameters boutParameters, Supplier<Void> updateSound ) {
        final JPanel g = new JPanel();
        g.setBorder( BorderFactory.createTitledBorder(
                        BorderFactory.createEmptyBorder( GeneratorApplication.COMPONENT_PADDING, GeneratorApplication.COMPONENT_PADDING, 20, 200 ),
                        "Modulation" ) );
        g.setLayout( new BoxLayout( g, BoxLayout.Y_AXIS ) );

        g.add( new JLabel( "Vibrato period" ) );
        final JSlider vibratoPeriodSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_VIBRATO_LENGTH, BoutParameters.MAX_VIBRATO_LENGTH,
                        boutParameters.vibratoLen, 500, ( e ) -> {
                            boutParameters.vibratoLen = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );
        g.add( vibratoPeriodSlider );
        g.add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );

        g.add( new JLabel( "Vibrato depth" ) );
        final JSlider vibratoDepthSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_VIBRATO_DEPTH, BoutParameters.MAX_VIBRATO_DEPTH,
                        boutParameters.vibratoDep, 1, ( e ) -> {
                            boutParameters.vibratoDep = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );
        g.add( vibratoDepthSlider );
        g.add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );

        g.add( new JLabel( "Random drift" ) );
        final JSlider randomDriftSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_DRIFT_DEPTH, BoutParameters.MAX_DRIFT_DEPTH,
                        boutParameters.driftDep, 1, ( e ) -> {
                            boutParameters.driftDep = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );
        g.add( randomDriftSlider );

        return g;
    }

    private Component createPitchGroup( BoutParameters boutParameters, Supplier<Void> updateSound ) {
        final JPanel g = new JPanel();
        g.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEmptyBorder( GeneratorApplication.COMPONENT_PADDING,
                        GeneratorApplication.COMPONENT_PADDING, GeneratorApplication.COMPONENT_PADDING, 200 ), "Pitch" ) );
        g.setLayout( new BoxLayout( g, BoxLayout.Y_AXIS ) );

        g.add( new JLabel( "Pitch at start of the sound" ) );
        final JSlider pitchStartSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_PITCH, BoutParameters.MAX_PITCH, boutParameters.pitch_start,
                        500, ( e ) -> {
                            boutParameters.pitch_start = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );
        g.add( pitchStartSlider );
        g.add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );

        g.add( new JLabel( "Pitch at anchor point" ) );
        final JSlider pitchAnchorPointSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_PITCH, BoutParameters.MAX_PITCH,
                        boutParameters.pitch_anchor, 500, ( e ) -> {
                            boutParameters.pitch_anchor = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );
        g.add( pitchAnchorPointSlider );
        g.add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );

        g.add( new JLabel( "Pitch at end of the sound" ) );
        final JSlider pitchEndSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_PITCH, BoutParameters.MAX_PITCH, boutParameters.pitch_end, 500,
                        ( e ) -> {
                            boutParameters.pitch_end = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );
        g.add( pitchEndSlider );
        g.add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );

        g.add( new JLabel( "Pitch anchor location" ) );
        final JSlider pitchAnchorLocationSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_PITCH_ANCHOR_LOCATION,
                        BoutParameters.MAX_PITCH_ANCHOR_LOCATION, boutParameters.pitch_anchor_location, 10, ( e ) -> {
                            boutParameters.pitch_anchor_location = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );
        g.add( pitchAnchorLocationSlider );
        return g;
    }

}
