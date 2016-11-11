package se.lu.lucs.sound;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYDrawableAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ExtensionFileFilter;

import se.lu.lucs.sound.BoutParameters.Formant;

public class GeneratorApplication extends JFrame {
    private final static String AMPLITUDE_SERIES_KEY = "Amplitude";
    public final static int COMPONENT_PADDING = 10;
    private static final String PITCH_CONTOUR_SERIES_KEY = "Pitch Contour";

    private final static CircleDrawer AMPLITUDE_MARKER = new CircleDrawer( Color.BLUE, new BasicStroke( 1.0f ), null );

    public static JSlider generateSlider( int min, int max, int defaultValue, int labelStep, ChangeListener handler ) {
        final JSlider s = new JSlider( min, max, defaultValue );

        s.addChangeListener( ( e ) -> {
            final JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                handler.stateChanged( e );
            }
        } );
        s.setPaintTicks( true );
        s.setPaintLabels( true );
        s.setLabelTable( s.createStandardLabels( labelStep ) );

        return s;
    }

    public static void main( String[] args ) {
        try {
            UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
        }
        catch (final Exception e) {
            //NOP
        }
        new GeneratorApplication().setVisible( true );

    }

    private static byte[] soundToBytes( List<Double> sound ) {
        final Double max = sound.stream().collect( Collectors.summarizingDouble( Double::doubleValue ) ).getMax();
        final byte[] audioBytes = new byte[sound.size() * Short.BYTES];
        int index = 0;
        for (final Double d : sound) {

            final int v = (int) (d / max * Short.MAX_VALUE);

            audioBytes[index++] = (byte) v;
            audioBytes[index++] = (byte) (v >> 8);

        }
        return audioBytes;
    }

    private final BoutParameters boutParameters;

    private final List<Double> sound;

    private List<Double> contour;

    private XYSeries amplitudeDataSet;
    private SourceDataLine audioSource;

    private JButton playButton;
    private XYSeries pitchContourDataSet;

    private ExactFormantTab exactFormantTab;
    private VowelTab vowelTab;
    private XYPlot contourPlot;

    public GeneratorApplication() {
        super( "Sound Generator" );

        setDefaultCloseOperation( DISPOSE_ON_CLOSE );
        setSize( 1280, 1024 );
        setJMenuBar( createMenuBar() );
        final AudioFormat audioFormat = new AudioFormat( 44100, 16, 1, true, false );
        try {
            audioSource = AudioSystem.getSourceDataLine( audioFormat );
            audioSource.open( audioFormat );
        }
        catch (final LineUnavailableException e) {
            handleException( e );
        }
        boutParameters = new BoutParameters();
        boutParameters.breathingType = new HashSet<>();
        boutParameters.exactFormants = new Formant();
        sound = new ArrayList<>();
        contour = new ArrayList<>();

        setLayout( new BoxLayout( getContentPane(), BoxLayout.Y_AXIS ) );
        getContentPane().add( createPreviewContainer() );
        getContentPane().add( createOptionsContainer() );

        updateSound();
    }

    private Component createAmplitudeGraph() {
        amplitudeDataSet = new XYSeries( AMPLITUDE_SERIES_KEY );
        final XYDataset d = new XYSeriesCollection( amplitudeDataSet );
        final XYItemRenderer renderer1 = new StandardXYItemRenderer();
        final NumberAxis rangeAxis1 = new NumberAxis( "dB" );
        final NumberAxis rangeAxis2 = new NumberAxis( "ms" );
        final XYPlot amplitudePlot = new XYPlot( d, rangeAxis2, rangeAxis1, renderer1 );
        final ChartPanel amplitudeComponent = new ChartPanel( new JFreeChart( amplitudePlot ) );

        return amplitudeComponent;
    }

    private Component createBreathingTab() {
        return new BreathingTab( boutParameters, () -> updateSound() );
    }

    private ExactFormantTab createFormantsTab() {
        exactFormantTab = new ExactFormantTab( boutParameters, () -> updateSound() );
        return exactFormantTab;
    }

    private Component createIntonationTab() {
        return new IntonationTab( boutParameters, () -> updateSound() );
    }

    private JMenuBar createMenuBar() {
        final JMenuBar bar = new JMenuBar();

        final JMenu file = new JMenu( "File" );
        final JMenuItem saveAsWAV = new JMenuItem( "Save as WAV" );
        saveAsWAV.addActionListener( ( e ) -> {
            final JFileChooser c = new JFileChooser( new File( "." ) );
            final FileFilter f = new ExtensionFileFilter( "Sound files", "wav" );
            c.setFileFilter( f );
            if (JFileChooser.APPROVE_OPTION == c.showSaveDialog( GeneratorApplication.this )) {
                try {
                    saveSound( c.getSelectedFile() );
                }
                catch (final Exception e1) {
                    handleException( e1 );
                }
            }

        } );
        file.add( saveAsWAV );
        bar.add( file );
        return bar;
    }

    private Component createOptionsContainer() {
        final JTabbedPane tabs = new JTabbedPane();
        tabs.addTab( "Syllables", new JScrollPane( createSyllablesTab() ) );
        tabs.addTab( "Intonation", new JScrollPane( createIntonationTab() ) );
        tabs.addTab( "Timbre", new JScrollPane( createTimbreTab() ) );
        tabs.addTab( "Breathing", new JScrollPane( createBreathingTab() ) );
        exactFormantTab = createFormantsTab();
        vowelTab = createVowelTab();
        vowelTab.setExactFormantTab( exactFormantTab );
        tabs.addTab( "Vowel", new JScrollPane( vowelTab ) );
        tabs.addTab( "Exact Formants", new JScrollPane( exactFormantTab ) );
        return tabs;

    }

    private Component createPitchContourGraph() {
        pitchContourDataSet = new XYSeries( PITCH_CONTOUR_SERIES_KEY );
        final XYDataset d = new XYSeriesCollection( pitchContourDataSet );
        final XYItemRenderer renderer1 = new StandardXYItemRenderer();
        final NumberAxis rangeAxis1 = new NumberAxis( "Hz" );
        final NumberAxis rangeAxis2 = new NumberAxis( "ms" );
        contourPlot = new XYPlot( d, rangeAxis2, rangeAxis1, renderer1 );
        final ChartPanel pitchContourComponent = new ChartPanel( new JFreeChart( contourPlot ) );

        return pitchContourComponent;
    }

    private Component createPlayButton() {
        playButton = new JButton( "Play" );
        playButton.addActionListener( ( e ) -> playSound() );
        audioSource.addLineListener( ( e ) -> {
            if (e.getType() == LineEvent.Type.STOP) {
                playButton.setEnabled( true );
            }
            else if (e.getType() == LineEvent.Type.START) {
                playButton.setEnabled( false );
            }
        } );
        return playButton;
    }

    private Component createPreviewContainer() {
        final JPanel t = new JPanel();
        t.setBorder( BorderFactory.createEmptyBorder( COMPONENT_PADDING, COMPONENT_PADDING, COMPONENT_PADDING, COMPONENT_PADDING ) );
        t.setLayout( new BoxLayout( t, BoxLayout.X_AXIS ) );
        t.add( createAmplitudeGraph() );
        t.add( createPitchContourGraph() );
        t.add( createSpectrumGraph() );
        t.add( createSpectogramGraph() );
        t.add( createPlayButton() );
        t.setMinimumSize( new Dimension( 300, 300 ) );
        return t;
    }

    private Component createSpectogramGraph() {
        final JPanel t = new JPanel();
        t.setBackground( Color.BLUE );
        t.setPreferredSize( new Dimension( 100, 100 ) );
        return t;
    }

    private Component createSpectrumGraph() {
        final JPanel t = new JPanel();
        t.setBackground( Color.YELLOW );
        t.setPreferredSize( new Dimension( 100, 100 ) );
        return t;
    }

    private Component createSyllablesTab() {
        return new SyllableTab( boutParameters, () -> updateSound() );
    }

    private Component createTimbreTab() {
        return new TimbreTab( boutParameters, () -> updateSound() );
    }

    private VowelTab createVowelTab() {
        return new VowelTab( boutParameters, () -> updateSound() );
    }

    @Override
    public void dispose() {
        super.dispose();
        audioSource.drain();
        audioSource.stop();
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

    private void playSound() {

        final byte[] audioBytes = soundToBytes( sound );
        audioSource.start();
        audioSource.write( audioBytes, 0, audioBytes.length );
        audioSource.flush();
        audioSource.stop();

    }

    private void saveSound( File file ) throws IOException {
        final byte[] audioBytes = soundToBytes( sound );

        final AudioInputStream stream = new AudioInputStream( new ByteArrayInputStream( audioBytes ), audioSource.getFormat(), audioBytes.length );
        AudioSystem.write( stream, AudioFileFormat.Type.WAVE, file );
    }

    private void updateDataSet( XYSeries s, List<Double> values ) {
        s.clear();
        for (int i = 0; i < values.size(); i++) {
            s.add( i, values.get( i ), false );
        }
        s.fireSeriesChanged();
    }

    private Void updateSound() {

        playButton.setEnabled( false );
        SwingUtilities.invokeLater( () -> {
            try {
                sound.clear();
                sound.addAll( Generator.generateBout( boutParameters ) );
                contour = Generator.getPitchContour( boutParameters.syllableDuration_mean, boutParameters.pitch_start, boutParameters.pitch_anchor,
                                boutParameters.pitch_end, boutParameters.pitch_anchor_location, boutParameters.femaleVoice );
                updateDataSet( amplitudeDataSet, sound );

                contourPlot.clearAnnotations();
                updateDataSet( pitchContourDataSet, contour );

                final XYAnnotation start = new XYDrawableAnnotation( 0, boutParameters.pitch_start, 30, 30, AMPLITUDE_MARKER );
                contourPlot.addAnnotation( start );
                final XYAnnotation end = new XYDrawableAnnotation( pitchContourDataSet.getItems().size() - 1, boutParameters.pitch_end, 30, 30,
                                AMPLITUDE_MARKER );
                contourPlot.addAnnotation( end );

                final XYAnnotation anchor = new XYDrawableAnnotation(
                                (pitchContourDataSet.getItems().size() - 1) * boutParameters.pitch_anchor_location / 100.0, boutParameters.pitch_anchor, 30, 30,
                                AMPLITUDE_MARKER );
                contourPlot.addAnnotation( anchor );

                playButton.setEnabled( true );
            }
            catch (final Exception e) {
                handleException( e );
            }
        } );
        return null;
    }
}
