package se.lu.lucs.sound.evolver;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.fastdtw.dtw.FastDTW;
import com.fastdtw.timeseries.TimeSeries;
import com.fastdtw.timeseries.TimeSeriesBase;
import com.fastdtw.timeseries.TimeSeriesBase.Builder;
import com.fastdtw.util.Distances;

/**
 * This class does a FastDTW on the amplitude and a supplied target amplitude
 * from a file. The returned fitness is the inverse of the distance returned by the DTW
 *
 * @author Tobias
 *
 */
public class MorphTargetAmplitude implements FitnessFunction {
    private final TimeSeries amplitude;

    public MorphTargetAmplitude( File sourceWav ) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        final AudioInputStream s = AudioSystem.getAudioInputStream( sourceWav );
        final AudioFormat format = s.getFormat();
        if (format.getChannels() > 1) {
            throw new IllegalStateException( "class only supports mono streams" );
        }
        if (format.getSampleRate() != 44100.0) {
            throw new IllegalStateException( "sample rate must be 44kHz, not " + format.getSampleRate() );
        }
        if (format.getSampleSizeInBits() != 16) {
            throw new IllegalStateException( "class only supports 16-bit samples" );
        }

        final ByteOrder order = format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        final byte[] frameBuffer = new byte[Double.BYTES];
        final int offSet = format.isBigEndian() ? Double.BYTES - 2 : 0;

        Builder builder = TimeSeriesBase.builder();
        long index = 0;

        final List<Double> curve = new ArrayList();
        while (s.available() > 0) {
            s.read( frameBuffer, offSet, 2 );
            final double d = ByteBuffer.wrap( frameBuffer ).order( order ).getDouble();
            assert d != Double.NaN;
            curve.add( d );
            builder = builder.add( index, d );
            index++;
        }
        final Double max = curve.stream().collect( Collectors.summarizingDouble( Double::doubleValue ) ).getMax();

        final ByteBuffer audioBuffer = ByteBuffer.allocate( curve.size() * Short.BYTES );
        final double pow = 65536;
        for (final Double d : curve) {
            final int v = (int) (d.doubleValue() / max * pow);
            audioBuffer.put( (byte) v );
            audioBuffer.put( (byte) (v >> 8) );

        }

        final byte[] audioBytes = audioBuffer.array();

        final AudioFormat format2 = new AudioFormat( 44100, 16, 1, true, false );
        final SourceDataLine audioSource = AudioSystem.getSourceDataLine( format2 );
        audioSource.open( format2 );
        audioSource.start();
        audioSource.write( audioBytes, 0, audioBytes.length );
        audioSource.stop();
        audioSource.close();

        amplitude = builder.build();
        s.close();
    }

    @Override
    public Double evaluate( List<Double> amplitude ) {
        Builder builder = TimeSeriesBase.builder();
        long index = 0;
        for (final Double d : amplitude) {
            builder = builder.add( index, d );
            index++;
        }
        final TimeSeries t = builder.build();

        System.out.println( "Created TimeSeries of length " + amplitude.size() + ". Invoking DTW" );
        return 1.0 - FastDTW.compare( this.amplitude, t, Distances.EUCLIDEAN_DISTANCE ).getDistance();
    }

}
