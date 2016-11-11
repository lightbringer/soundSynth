package se.lu.lucs.sound;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import se.lu.lucs.sound.BoutParameters.BreathingType;
import se.lu.lucs.sound.BoutParameters.Formant;
import se.lu.lucs.sound.evolver.Evolver;
import se.lu.lucs.sound.evolver.MutateAll;

public class TonSchmiede extends JPanel {
    private final static int POPULATION_SIZE = 6;
    private final static int ROW_SIZE = 3;

    public static final AudioFormat FORMAT = new AudioFormat( 44100, 16, 1, true, false );

    public static void main( String[] args ) {
        try {
            // Set System L&F
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        }
        catch (final UnsupportedLookAndFeelException e) {
            // handle exception
        }
        catch (final ClassNotFoundException e) {
            // handle exception
        }
        catch (final InstantiationException e) {
            // handle exception
        }
        catch (final IllegalAccessException e) {
            // handle exception
        }
        final JFrame app = new JFrame();
        app.setSize( 1027, 768 );
        app.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        app.getContentPane().add( new TonSchmiede() );
        app.setVisible( true );
    }

    private SourceDataLine audioSource;
    private final Evolver evolver;

    private final Set<SoundPanel> soundPanels;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public TonSchmiede() {

        soundPanels = new HashSet<>();
        evolver = new Evolver( null );
        evolver.setEliteSize( 1f ); //We will manually refill the population each generation, and we want to keep those we added
        evolver.setMutationFunction( new MutateAll() {

            @Override
            public void mutate( BoutParameters p ) {
                setMutationStrength( .01 );
                p.pitch_start = (int) mutate( normalDistribution, p.pitch_start, AcousticParameters.MIN_PITCH, AcousticParameters.MAX_PITCH );
                p.pitch_anchor = (int) mutate( normalDistribution, p.pitch_anchor, AcousticParameters.MIN_PITCH, AcousticParameters.MAX_PITCH );
                p.pitch_end = (int) mutate( normalDistribution, p.pitch_end, AcousticParameters.MIN_PITCH, AcousticParameters.MAX_PITCH );
                p.pitch_anchor_location = (int) mutate( normalDistribution, p.pitch_anchor_location, AcousticParameters.MIN_PITCH_ANCHOR_LOCATION,
                                AcousticParameters.MAX_PITCH_ANCHOR_LOCATION );

            }
        } );
        evolver.setCrossOverFunction( ( b1, b2 ) -> {
            return new BoutParameters[] { new BoutParameters( b1 ), new BoutParameters( b2 ) };
        } );
        evolver.setCrossoverProbability( 1f );
        evolver.setMutateProbability( 1f );
        evolver.setPopulationSize( POPULATION_SIZE );
        evolver.setGenerator( this::generateBout );
        try {
            audioSource = AudioSystem.getSourceDataLine( FORMAT );
            audioSource.open( FORMAT );

            final JPanel audioClips = new JPanel();
            final GridLayout layout = new GridLayout( POPULATION_SIZE / ROW_SIZE, ROW_SIZE );
            for (int i = 0; i < POPULATION_SIZE; i++) {
                final SoundPanel p = new SoundPanel( this );
                soundPanels.add( p );
                final BoutParameters b = generateBout();

                final List<Double> curve = Generator.generateBout( b );
                p.setAmplitude( curve );
                p.setParameters( b );
                audioClips.add( p );
            }

            audioClips.setLayout( layout );

            setLayout( new BorderLayout() );
            add( audioClips, BorderLayout.CENTER );
            final JPanel buttonPanel = new JPanel();
            final JButton nextButton = new JButton( "Next" );
            nextButton.addActionListener( this::nextGeneration );
            buttonPanel.add( nextButton );
            final JButton doneButton = new JButton( "I'm done" );
            buttonPanel.add( doneButton );
            doneButton.addActionListener( this::checkAndSubmit );
            add( buttonPanel, BorderLayout.SOUTH );
        }
        catch (final LineUnavailableException e) {
            handleException( e );
        }

    }

    private void checkAndSubmit( ActionEvent e ) {
        final List<BoutParameters> selected = soundPanels.stream().filter( sp -> sp.isSelected() ).map( sp -> sp.getParameters() )
                        .collect( Collectors.toList() );
        if (selected.size() != 1) {
            JOptionPane.showMessageDialog( this, "Please selected exactly one waveform" );
        }
        else {
            JOptionPane.showMessageDialog( this, "PLACEHOLDER_SUCCESS" );
        }
    }

    private BoutParameters generateBout() {
        final BoutParameters b = new BoutParameters();

        //Laugh
        b.numberOfSyllables = 6;
//        b.durTotal= 840;
        b.pauseDuration_mean = 50;
        b.var_bw_syllables = 20;
//        b.proportionNoisy= 90;
        b.pitch_start = 360;
        b.pitch_anchor = 210;
        b.pitch_end = 140;
        b.pitch_anchor_location = 27;
        b.creakyBreathy = -.1;
        b.attackLen = 10;
        b.subharmDep = 30;
        b.breathingType = new HashSet();
        b.breathingType.add( BreathingType.DURING );
        b.breathingType.add( BreathingType.AFTER );

        b.breathingStrength = .8;
        b.breathingStrength_diff = -1.5;
        b.breathing_dur = 30;
        b.randomVowel = false;
        b.exactFormants = new Formant();
        b.exactFormants.f1_freq = 700;
        b.exactFormants.f2_freq = 1400;
        b.exactFormants.f3_freq = 2800;
        b.exactFormants.f4_freq = 3500;
        b.exactFormants.f1_amp = .7;
        b.exactFormants.f2_amp = .7;
        b.exactFormants.f3_amp = .7;
        b.exactFormants.f4_amp = .7;
        b.exactFormants.f1_width = 100;
        b.exactFormants.f2_width = 100;
        b.exactFormants.f3_width = 100;
        b.exactFormants.f4_width = 100;
        b.formantStrength = 40;

        return b;
    }

    private void handleException( Throwable e ) {
        //If we're running with an attached debugger, throw nw Exception instead of consuming it
        if (ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf( "-agentlib:jdwp" ) > 0) {
            throw new RuntimeException( e );
        }

        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter( sw );
        if (e.getCause() != null) {
            e = e.getCause();
        }
        e.printStackTrace( pw );

        JOptionPane.showMessageDialog( this, sw.toString(), e.getLocalizedMessage(), JOptionPane.ERROR_MESSAGE );

    }

    private void nextGeneration( ActionEvent e ) {
        final List<BoutParameters> selected = soundPanels.stream().filter( sp -> sp.isSelected() ).map( sp -> sp.getParameters() )
                        .collect( Collectors.toList() );
        evolver.setPopulation( selected );

        evolver.advancePopulation();

        updatePanels( evolver.getPopulation().stream().map( g -> g.parameters ).collect( Collectors.toList() ) );
    }

    public void playCurve( SoundPanel sp ) {
        stopPlay();
        assert !audioSource.isActive();

        final int length = sp.getAudioBytes().length;
        executor.submit( () -> {
            synchronized (audioSource) {
                audioSource.start();
                audioSource.write( sp.getAudioBytes(), 0, length );
            }

            try {
                final long ms = (long) (length / 2.0 / 44.1);
                Thread.sleep( ms );
            }
            catch (final InterruptedException e1) {
                //NOP
            }
            synchronized (audioSource) {
                audioSource.stop();
            }

            sp.setPlaying( false );
        } );
    }

    public void stopPlay() {
        synchronized (audioSource) {
            if (audioSource.isActive()) {
                audioSource.stop();
                audioSource.flush();
            }
        }

    }

    private void updatePanels( List<BoutParameters> map ) {
        assert map.size() == POPULATION_SIZE;

        final Iterator<SoundPanel> iter = soundPanels.iterator();
        for (int i = 0; i < map.size(); i++) {
            final SoundPanel sp = iter.next();
            final List<Double> ampl = Generator.generateBout( map.get( i ) );
            sp.setAmplitude( ampl );
            sp.setParameters( map.get( i ) );
        }

    }
}
