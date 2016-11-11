package se.lu.lucs.sound;

import java.awt.Component;
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

public class TimbreTab extends JPanel {
    public TimbreTab( BoutParameters boutParameters, Supplier<Void> updateSound ) {
        setBorder( BorderFactory.createEmptyBorder( GeneratorApplication.COMPONENT_PADDING, 100, GeneratorApplication.COMPONENT_PADDING, 100 ) );
        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

        add( new JLabel( "Female Voice" ) );
        final JCheckBox femaleVoiceCheckBox = new JCheckBox();
        femaleVoiceCheckBox.setSelected( boutParameters.femaleVoice );
        femaleVoiceCheckBox.addActionListener( ( e ) -> {
            boutParameters.femaleVoice = femaleVoiceCheckBox.isSelected();
            updateSound.get();
        } );
        add( femaleVoiceCheckBox );
        add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );

        add( new JLabel( "Creaky vs Breathy" ) );
        final JSlider creakyBreathySlider = GeneratorApplication.generateSlider( BoutParameters.MIN_CREAKY_BREATHY * 100,
                        BoutParameters.MAX_CREAKY_BREATHY * 100, (int) (boutParameters.creakyBreathy * 100.0), 10, ( e ) -> {
                            boutParameters.creakyBreathy = ((JSlider) e.getSource()).getValue() / 100.0;
                            updateSound.get();
                        } );
        final Hashtable<Integer, JComponent> labels = new Hashtable<>();
        labels.put( -100, new JLabel( "Creaky" ) );
        for (int i = -90; i <= 90; i += 10) {
            labels.put( i, new JLabel( new Double( i / 100.0 ).toString() ) );
        }
        labels.put( 100, new JLabel( "Breathy" ) );
        creakyBreathySlider.setLabelTable( labels );
        add( creakyBreathySlider );
        add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );

        add( new JLabel( "Attack Length" ) );
        final JSlider attackLengthSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_ATTACK_LENGTH, BoutParameters.MAX_ATTACK_LENGTH,
                        boutParameters.attackLen, 10, ( e ) -> {
                            boutParameters.attackLen = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );

        add( attackLengthSlider );
        add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );
        add( createNoiseGroup( boutParameters, updateSound ) );
        add( createVocalFryGroup( boutParameters, updateSound ) );
    }

    private Component createNoiseGroup( BoutParameters boutParameters, Supplier<Void> updateSound ) {
        final JPanel g = new JPanel();
        g.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEmptyBorder( GeneratorApplication.COMPONENT_PADDING,
                        GeneratorApplication.COMPONENT_PADDING, GeneratorApplication.COMPONENT_PADDING, 200 ), "Noise" ) );
        g.setLayout( new BoxLayout( g, BoxLayout.Y_AXIS ) );

        g.add( new JLabel( "Jitter" ) );
        final JSlider jitterSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_JITTER, BoutParameters.MAX_JITTER, boutParameters.jitterDep, 10,
                        ( e ) -> {
                            boutParameters.jitterDep = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );

        g.add( jitterSlider );
        g.add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );

        g.add( new JLabel( "Shimmer" ) );
        final JSlider shimmerSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_SHIMMER, BoutParameters.MAX_SHIMMER, boutParameters.shimmerDep,
                        10, ( e ) -> {
                            boutParameters.shimmerDep = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );

        g.add( shimmerSlider );

        return g;
    }

    private Component createVocalFryGroup( BoutParameters boutParameters, Supplier<Void> updateSound ) {
        final JPanel g = new JPanel();
        g.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEmptyBorder( GeneratorApplication.COMPONENT_PADDING,
                        GeneratorApplication.COMPONENT_PADDING, GeneratorApplication.COMPONENT_PADDING, 200 ), "Vocal fry (subharmonics)" ) );
        g.setLayout( new BoxLayout( g, BoxLayout.Y_AXIS ) );

        g.add( new JLabel( "Number of Subharmonics" ) );
        final JSlider subharmonicsSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_SUBHARMONICS, BoutParameters.MAX_SUBHARMONICS,
                        boutParameters.nSubharm, 5, ( e ) -> {
                            boutParameters.nSubharm = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );

        g.add( subharmonicsSlider );
        g.add( Box.createRigidArea( new Dimension( 0, GeneratorApplication.COMPONENT_PADDING ) ) );

        g.add( new JLabel( "Subharmonics Strength" ) );
        final JSlider subharmonicsStrengthSlider = GeneratorApplication.generateSlider( BoutParameters.MIN_SUBHARMONICS_STRENGTH,
                        BoutParameters.MAX_SUBHARMONICS_STRENGTH, boutParameters.subharmDep, 10, ( e ) -> {
                            boutParameters.subharmDep = ((JSlider) e.getSource()).getValue();
                            updateSound.get();
                        } );

        g.add( subharmonicsStrengthSlider );

        return g;
    }
}
