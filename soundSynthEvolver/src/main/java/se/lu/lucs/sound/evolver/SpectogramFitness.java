package se.lu.lucs.sound.evolver;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.math3.complex.Complex;

import se.lu.lucs.sound.AcousticParameters;
import se.lu.lucs.sound.Generator;

public abstract class SpectogramFitness implements FitnessFunction {
    private final static int WINDOW_LENGTH = AcousticParameters.DEFAULT_WINDOW_LENGTH;
    private final static int OVERLAP = AcousticParameters.DEFAULT_OVERLAP;

    private static List<List<Double>> generateSpectogram( double[] ampl ) {
        final double delta = WINDOW_LENGTH - OVERLAP * WINDOW_LENGTH / 100.0;
        final List<List<Double>> spectrum = new ArrayList<>();
        int frameOffset = 0;
        while (frameOffset < ampl.length - WINDOW_LENGTH) {

            final double[] frame = Arrays.copyOfRange( ampl, frameOffset, frameOffset + WINDOW_LENGTH );
            final List<Complex> frameSpectrum = Generator.fft( frame );

            final List<Double> realFrameSpectrum = new ArrayList();
            for (final Complex c : frameSpectrum) {
                realFrameSpectrum.add( Math.abs( c.getReal() ) );
            }
            spectrum.add( realFrameSpectrum );
            frameOffset += delta;
        }
        return spectrum;
    }

    public static double[] loadFile( File sourceWav ) throws UnsupportedAudioFileException, IOException {
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

        int index = 0;
        final double[] ampl = new double[(int) s.getFrameLength()];
        double max = Double.MIN_VALUE;
        while (s.available() > 0) {
            s.read( frameBuffer, offSet, 2 );
            final double d = ByteBuffer.wrap( frameBuffer ).order( order ).getDouble();
            assert d != Double.NaN;
            ampl[index] = d;
            max = Math.max( d, max );
            index++;
        }

        for (int i = 0; i < ampl.length; i++) {
            ampl[i] /= max * Short.MAX_VALUE * 2;
        }

        assert index == s.getFrameLength();
        s.close();
        return ampl;
    }

    private static void matchLength( List<List<Double>> spec, int length ) {
        if (spec.size() >= length) {
            return;
        }
        final int paddingSize = (length - spec.size()) / 2;
        for (int i = 0; i < paddingSize; i++) {
            spec.add( 0, null );
        }
        while (spec.size() < length) {
            spec.add( null );
        }

    }

    private final List<List<Double>> targetSpectogram;

    public SpectogramFitness( File sourceWav ) throws UnsupportedAudioFileException, IOException {

        targetSpectogram = generateSpectogram( loadFile( sourceWav ) );

    }

    @Override
    public Double evaluate( List<Double> amplitude ) {
        final double[] am = new double[amplitude.size()];
        for (int i = 0; i < am.length; i++) {
            am[i] = amplitude.get( i );
        }
        final List<List<Double>> candidateSpectogram = generateSpectogram( am );

        List<List<Double>> targetSpectogram = this.targetSpectogram;
        if (candidateSpectogram.size() > targetSpectogram.size()) {
            targetSpectogram = new ArrayList( targetSpectogram );
            matchLength( targetSpectogram, candidateSpectogram.size() );
        }
        else if (candidateSpectogram.size() < targetSpectogram.size()) {
            matchLength( candidateSpectogram, targetSpectogram.size() );
        }
        assert candidateSpectogram.size() == targetSpectogram.size();

        return evaluateSpectogram( targetSpectogram, candidateSpectogram );
    }

    protected abstract Double evaluateSpectogram( List<List<Double>> target, List<List<Double>> candidate );
}
