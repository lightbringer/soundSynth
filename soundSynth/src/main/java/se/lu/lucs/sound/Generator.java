package se.lu.lucs.sound;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.distribution.GammaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import se.lu.lucs.sound.BoutParameters.BreathingType;
import se.lu.lucs.sound.BoutParameters.Formant;

public class Generator {
    private final static FastFourierTransformer TRANSFORMER = new FastFourierTransformer( DftNormalization.STANDARD );

    private static final List<Double> SILENCE_TIME;

    static {
        SILENCE_TIME = new ArrayList();
        final int silencetime = (int) (44100 * .25);
        for (int i = 0; i < silencetime; i++) {
            SILENCE_TIME.add( 0.0 );
        }
    }

    private final static Random RANDOM = new Random();

    /**
     * ADDS shimmer, ie random variation in amplitude per glottal cycle, to an ALREADY SYNTHESIZED sound
     * @param ampl a vector of zero-centered numbers
     * @param pitch
     * @param shimmerDep the desired amount of shimmer, in % of amplitude range
     * @param gc
     */
    private static void addShimmer( double[] ampl, List<Double> pitch, int shimmerDep, List<Integer> glottalCycles ) {

        // get indices of glottal cycles, if not already provided
        if (glottalCycles == null) {
            throw new IllegalArgumentException( "glottalCycles must be provided" );
        }

        final int nInt = glottalCycles.size() - 1; // these are the intervals whose amplitude is to be adjusted at random
        if (nInt < 2) {
            return;
        } // need at least 2 glottal cycles to add shimmer

        // generate shimmer as normally final distributed variation in final amplitude with sd=shimmerDep/100
        final NormalDistribution rnorm = new NormalDistribution( 1, shimmerDep / 100.0 );
        final double[] shimmer = new double[nInt];

        for (int i = 0; i < nInt; i++) {
            shimmer[i] = rnorm.sample();
            shimmer[i] = Math.max( shimmer[i], 0 );
        }

        // add shimmer to original ampl
        for (int z = 0; z < nInt - 1; z++) {
            for (int i = glottalCycles.get( z ); i < glottalCycles.get( z + 1 ); i++) {
                ampl[i] *= shimmer[z];
            }
        }
    }

    /**
     * vocal fry, ie dampens or mutes a % of glottal cycles in an ALREADY SYNTHESIZED sound to create false subharmonics.
     *
     * @param ampl a vector of zero-centered amplitudes
     * @param pitch
     * @param nSubharm
     * @param subharmDep controls the amplitude of subharmonics (in %) relative to F0
     * @param gc
     */
    private static void addVocalFry( double[] ampl, List<Double> pitch, int nSubharm, int subharmDep, List<Integer> glottalCycles ) {
        if (glottalCycles == null) {
            throw new IllegalArgumentException( "glottalCycles must be provided" );
        }

        final double prop_preserved_cycles = 1 / (double) (1 + nSubharm);
        final double dampening = subharmDep / 100.0;
        final int dampenEveryNthCycle = nSubharm + 1; // dampen 1 out of every /dampenEveryNthCycle/ glottal cycles (eg 4 out of 5 if prop_preserved_cycles=20%)

        final int nInt = glottalCycles.size() - 1; // these are the intervals whose amplitude is to be adjusted at random
        if (nInt < 2 || prop_preserved_cycles > .5) {
            return;
        } // need at least 2 glottal cycles and <50% of preserved glottal cycles to add vocal fry

        for (int i = 0; i < nInt; i++) {
            if (i % dampenEveryNthCycle != 1) {
                for (int j = glottalCycles.get( i ); j < glottalCycles.get( i + 1 ); j++) {
                    ampl[j] *= 1 - dampening;
                }
            }
        }

    }

    private static List<Double> applyFFT( double[] ampl, BoutParameters p, double[] filter ) {
        final double delta = p.windowLength_points - p.overlap * p.windowLength_points / 100.0;

        final double max = ampl.length - p.windowLength_points;
        final int colNum = (int) ((max - 1) / delta);

        // prepare windowing function
        final double[] w = generateGaussianWindow( p.windowLength_points );

        final List<List<Complex>> spectrum = new ArrayList<>();
        int frameOffset = 0;
        while (frameOffset < ampl.length - p.windowLength_points) {

            final double[] frame = Arrays.copyOfRange( ampl, frameOffset, frameOffset + p.windowLength_points );
            final List<Complex> frameSpectrum = fft( frame );
            spectrum.add( frameSpectrum );
            frameOffset += delta;
        }

        // apply filter
        for (final List<Complex> frameSpec : spectrum) {
            for (int i = 0; i < frameSpec.size(); i++) {

                frameSpec.set( i, frameSpec.get( i ).multiply( filter[i] ) );
            }
        }

        //Inverse fft to reconstruct time series
        final int h = p.windowLength_points * (100 - p.overlap) / 100;

        final int xlen = p.windowLength_points + (colNum - 1) * h;
        final List<Double> ret = new ArrayList<>( xlen );
        for (int i = 0; i < xlen; i++) {
            ret.add( 0.0 );
        }

        for (int b = 0; b <= h * (colNum - 1); b += h) {

            final List<Complex> frameSpec = spectrum.get( b / h );
            final List<Complex> mirror = new ArrayList<>( frameSpec );
            mirror.remove( 0 );
            Collections.reverse( mirror );
            for (int i = 0; i < mirror.size(); i++) {
                final Complex t = mirror.get( i );
                mirror.set( i, Complex.valueOf( t.getReal(), -t.getImaginary() ) );
            }
            final Complex last = Complex.valueOf( frameSpec.get( frameSpec.size() - 1 ).getReal() );

            final List<Complex> something = new ArrayList<>();
            something.addAll( frameSpec );
            something.add( last );
            something.addAll( mirror );

            final Complex[] s = new Complex[something.size()];
            something.toArray( s );
            final Complex[] xprim = TRANSFORMER.transform( s, TransformType.INVERSE );

            for (int i = 0; i < p.windowLength_points; i++) {
                ret.set( i + b, ret.get( i + b ) + xprim[i].getReal() * w[i] );
            }
        }

        double wSum = 0;
        for (final double element : w) {
            wSum += element;
        }
        wSum *= wSum;
        final double s = wSum;
        ret.replaceAll( d -> d * h / s );

        return ret;
    }

    public static AudioInputStream convertAmplitude( List<Double> curve, AudioFormat format ) {
        final Double max = curve.stream().collect( Collectors.summarizingDouble( Double::doubleValue ) ).getMax();
        final ByteBuffer audioBuffer = ByteBuffer.allocate( curve.size() * Short.BYTES );
        final double pow = 65536;
        for (final Double d : curve) {
            final int v = (int) (d.doubleValue() / max * pow);
            audioBuffer.put( (byte) v );
            audioBuffer.put( (byte) (v >> 8) );

        }

        final byte[] audioBytes = audioBuffer.array();

        return new AudioInputStream( new ByteArrayInputStream( audioBytes ), format, curve.size() );

    }

    /**
     * cross-fades two sounds, ie fades out the first sound and fades in the second sound (linearly), with an overlap of length_ms
     * @return
     */
    private static List<Double> crossFade( List<Double> ampl1, List<Double> ampl2, double length_ms ) {
        final List<Double> result = new ArrayList<>();
        final int length_points = (int) Math.floor( length_ms * 44.1 ); // defaults to 88 points
        if (ampl1.size() < length_points || ampl2.size() < length_points) {
            result.addAll( ampl1 );
            result.addAll( ampl2 );

            return result;
        }

        final List<Double> ampl1Cross = new ArrayList<>( ampl1.subList( 0, findClosestsZeroCrossing( ampl1, ampl1.size() ) ) ); // up to the last non-negative point on the upward curve + one exta zero
        final List<Double> ampl2Cross = new ArrayList<>( ampl2.subList( findClosestsZeroCrossing( ampl2, 0 ), ampl2.size() ) ); // from the first positive point on the upward curve

        final double delta = 1 / (double) (length_points - 1);
        double v = 1;
        for (int i = ampl1Cross.size() - length_points; i < ampl1Cross.size(); i++) {
            double a = ampl1Cross.get( i );
            a *= v;
            ampl1Cross.set( i, a );
            v -= delta;
        }
        v = 0;
        for (int i = 0; i < length_points; i++) {
            double a = ampl2Cross.get( i );
            a *= v;
            ampl2Cross.set( i, a );
            v += delta;
        }
        final List<Double> cross = new ArrayList<>();

        //TODO Might need another round of debugging
        for (int i = 0; i < length_points; i++) {
            cross.add( ampl1Cross.get( ampl1Cross.size() - length_points - 1 + i ) + ampl2Cross.get( i ) );
        }

        result.addAll( ampl1.subList( 0, ampl1.size() - length_points - 1 ) );
        result.addAll( cross );
        result.addAll( ampl2.subList( length_points, ampl2.size() - 1 ) );
        return result;
    }

    /**
     * Fades in/out the ENTIRE input vector.
     *
     * @param ampl
     * @param fadeStrength can vary from -inf (fade out) to +inf (fade in); in practice -5 to +5 is enough. 0 means no change of ampl, +1 means weak linear fade-in from 50% to 100%, +5 means fast exponential fade-in from ~0% to 100%, etc.
     * @return
     */
    private static List<Double> fadeInOutExponential( List<Double> ampl, double fadeStrength ) {
        final int len = ampl.size();

        if (len == 0 || fadeStrength == 0) {
            return ampl;
        }
        final Double[] mult = new Double[len];
        double maxMult = Double.MIN_VALUE;
        for (int i = 0; i < len; i++) {
            mult[i] = 1.0 / (1.0 + Math.exp( -fadeStrength * i / len ));
            maxMult = Math.max( maxMult, mult[i] );
        }

        if (fadeStrength <= 0) {
            for (int i = 0; i < mult.length; i++) {
                mult[i] /= maxMult;
            }
        }
        else {
            final List<Double> multList = Arrays.asList( mult );
            Collections.reverse( multList );
            final double m = maxMult;
            multList.replaceAll( d -> (1 - d) / m ); // range 0 to 1

        }
        for (int i = 0; i < ampl.size(); i++) {
            ampl.set( i, ampl.get( i ) * mult[i] );
        }

        return ampl;
    }

    /**
     * applies linear fade-in and fade-out of length 'length_fade' points to one or both ends of input vector
     *
     * @param ampl
     * @param do_fadeIn
     * @param do_fadeOut
     * @param length_fade
     * @return
     */
    private static List<Double> fadeInOutLinear( List<Double> ampl, boolean do_fadeIn, boolean do_fadeOut, int length_fade ) {
        //Clamp the fade to the sound's length. Ignore higher values
        length_fade = Math.min( length_fade, ampl.size() );

        if (do_fadeIn) {
            for (int i = 0; i < length_fade; i++) {
                ampl.set( i, ampl.get( i ) * (i / (double) length_fade) );
            }
        }

        if (do_fadeOut) {
            for (int i = 0; i < length_fade; i++) {
                final int reverseIndex = i + ampl.size() - length_fade;
                ampl.set( reverseIndex, ampl.get( reverseIndex ) * (1 - i / (double) length_fade) );
            }
        }

        return ampl;
    }

    /**
     * Performs a foward fast fourier transformation and discards the second half of the resulting array.
     *
     * @param window
     * @return
     */
    public static List<Complex> fft( double[] window ) {

        final Complex[] c = TRANSFORMER.transform( window, TransformType.FORWARD );
        return Arrays.asList( c ).subList( 0, c.length / 2 );
    }

    /**
     * returns the index of the last value before zero crossing with a positive slope
     * closest to location
     *
     * @return
     */
    private static int findClosestsZeroCrossing( List<Double> ampl, int location ) {
        final Set<Integer> crossings = new HashSet<>();

        boolean predecessorPositive = ampl.get( 0 ) >= 0;
        for (int i = 1; i < ampl.size(); i++) {
            if (ampl.get( i ) > 0 && !predecessorPositive) {
                crossings.add( i - 1 );
            }
            predecessorPositive = ampl.get( i ) > 0;
        }
        if (crossings.isEmpty()) {
            throw new IllegalStateException( "no crossings in given amplitude" );
        }
        int loc = -1;
        int min = Integer.MAX_VALUE;
        for (final Integer i : crossings) {
            final int j = Math.abs( location - i );
            if (j < min) {
                min = j;
                loc = i;
            }
        }

        return loc;
    }

    public static List<Double> generateBout( BoutParameters p ) {
        final List<Double> sound = new ArrayList<>();

        //Copy the parameters as we might alter them for multiple syllables
        p = new BoutParameters( p );

        // generate syllables
        for (int i = 0; i < p.numberOfSyllables; i++) {

            final List<Double> pitchContour = getPitchContour( p.syllableDuration_mean, p.pitch_start, p.pitch_anchor, p.pitch_end, p.pitch_anchor_location,
                            p.femaleVoice );
            // generate syllable
            sound.addAll( generateSyllable( p, pitchContour ) );

            if (i < p.numberOfSyllables - 1) {
                //If there's at least one more syllable coming, mutate the parameters a bit

                //durationMs
                double sdg = p.syllableDuration_mean * p.var_bw_syllables / 100.0;
                GammaDistribution g = new GammaDistribution( p.syllableDuration_mean * p.syllableDuration_mean / (sdg * sdg),
                                1 / (p.syllableDuration_mean / (sdg * sdg)) );

                p.syllableDuration_mean = Math.max( AcousticParameters.MIN_MEAN_SYLLABLE_LENGTH,
                                Math.min( AcousticParameters.MAX_MEAN_SYLLABLE_LENGTH, (int) g.sample() ) );

                //pauseMs
                sdg = p.pauseDuration_mean * p.var_bw_syllables / 100.0;
                g = new GammaDistribution( p.pauseDuration_mean * p.pauseDuration_mean / (sdg * sdg), 1 / (p.pauseDuration_mean / (sdg * sdg)) );

                p.pauseDuration_mean = Math.max( AcousticParameters.MIN_SYLLABLE_PAUSE, Math.min( AcousticParameters.MAX_SYLLABLE_PAUSE, (int) g.sample() ) );

                //pitchStart
                sdg = p.pitch_start * p.var_bw_syllables / 100.0;
                g = new GammaDistribution( p.pitch_start * p.pitch_start / (sdg * sdg), 1 / (p.pitch_start / (sdg * sdg)) );

                p.pitch_start = Math.max( AcousticParameters.MIN_PITCH, Math.min( AcousticParameters.MAX_PITCH, (int) g.sample() ) );

                //pitchAnchor
                sdg = p.pitch_anchor * p.var_bw_syllables / 100.0;
                g = new GammaDistribution( p.pitch_anchor * p.pitch_anchor / (sdg * sdg), 1 / (p.pitch_anchor / (sdg * sdg)) );

                p.pitch_anchor = Math.max( AcousticParameters.MIN_PITCH, Math.min( AcousticParameters.MAX_PITCH, (int) g.sample() ) );

                //pitchEnd
                sdg = p.pitch_end * p.var_bw_syllables / 100.0;
                g = new GammaDistribution( p.pitch_end * p.pitch_end / (sdg * sdg), 1 / (p.pitch_end / (sdg * sdg)) );

                p.pitch_end = Math.max( AcousticParameters.MIN_PITCH, Math.min( AcousticParameters.MAX_PITCH, (int) g.sample() ) );

                //pitchEnd
                sdg = p.pitch_anchor_location * p.var_bw_syllables / 100.0;
                g = new GammaDistribution( p.pitch_anchor_location * p.pitch_anchor_location / (sdg * sdg), 1 / (p.pitch_anchor_location / (sdg * sdg)) );

                p.pitch_anchor_location = Math.max( AcousticParameters.MIN_PITCH_ANCHOR_LOCATION,
                                Math.min( AcousticParameters.MAX_PITCH_ANCHOR_LOCATION, (int) g.sample() ) );

                for (int j = 0; j < p.pauseDuration_mean * 44.1; j++) {
                    sound.add( 0.0 );
                }
            }
        }

        //add some silence before and after the entire bout
        sound.addAll( 0, SILENCE_TIME );
        sound.addAll( SILENCE_TIME );

        return sound;
    }

    /**
     * Generates an array of gaussian noise
     *
     * @param length
     * @return
     */
    private static double[] generateGaussianWindow( int length ) {
        //(exp(-12*(((1:n)/n)-0.5)^2)-exp(-12))/(1-exp(-12))
        final double[] w = new double[length];
        for (int i = 0; i < w.length; i++) {
            w[i] = Math.exp( -12 * Math.pow( i / (double) w.length - .5, 2 ) - Math.exp( -12 ) ) / (1 - Math.exp( -12 ));
        }
        return w;
    }

    /**
     * basic generator function. Returns a single completely processed syllable. See generateBout() for explanations of pars
     * @param p
     * @return
     */
    private static List<Double> generateSyllable( BoutParameters p, List<Double> pitch ) {

//                        time = as.numeric(1:length(pitch)) # as.numeric to prevent integer overflow
        final List<Integer> gc = getGlottalCycles( pitch ); // our "glottal cycles"

        // readjust major timber pars according to the given breathingStrength
        if (p.creakyBreathy < 0) { // for creaky voice, adjust:
            p.jitterDep -= p.creakyBreathy;
            p.shimmerDep -= p.creakyBreathy * 2;
            // driftDep = driftDep - breathingStrength*6
        }
        // for both creaky and breathy voices, adjust:
        p.spectralSlope += p.creakyBreathy * .3;
        p.megaFormant_strength += Math.abs( p.creakyBreathy ) * 60;
        p.formantStrength = (int) Math.max( 0, p.formantStrength - Math.abs( p.creakyBreathy ) * p.formantStrength / 4.0 );
        p.rolloff = (int) Math.max( 0, p.rolloff + p.creakyBreathy * 40 );

        // calculate the number of harmonics to generate (from lowest pitch to at least 8000 Hz) and the spectral filter (think equalizer)
        final double maxPitch = pitch.stream().max( Double::compare ).get();
        final int nHarmonics = (int) ((20000 - 2 * maxPitch) / maxPitch); // was length (seq (2*max(pitch), 20000, by=max(pitch)) )
        // filter = 2^(-rolloff/10*(1:22050)/1000) # ~defaults to -6 dB per 1000 Hz # plot(filter,type='l')
        // filter = filter[seq(1, 22050, length.out=windowLength_points/2)]
        final double[] filter = getSpectralEnvelope( p.spectralSlope, p.formantStrength, p.lenRarFilter, p.spectralNoise_strength, p.spanFilter,
                        p.megaFormant_mean, p.megaFormant_sd, p.megaFormant_strength, p.femaleVoice, p.randomVowel, 2048, p.exactFormants );

        // calculate vibrato
        double vibrato;
        double vibrato_multipl;
        if (p.vibratoDep == 0) {
            vibrato = 0;
            vibrato_multipl = 0;
        }
        else {
            vibrato_multipl = Math.pow( 2, p.vibratoDep / 12 ) - 1; // convert from semitones to % of F0
            vibrato = Math.sin( 2 * Math.PI * pitch.size() * 1000 / p.vibratoLen / 44100 ); // 1000/vibratoLen (in ms) gives the frequency of vibrato in Hz # plot(vibrato[1:10000], type='l')
        }

        // calculate jitter (random variation of F0)
        final double[] jitter = p.jitterDep > 0 ? getJitter( pitch.size(), gc, p.jitterDep ) : null;

        // calculate random drift of F0
        List<Double> drift;
        final double driftPeriod_points = 44.1 * p.driftLen;
        if (p.driftDep > 0 && driftPeriod_points <= pitch.size()) {
            drift = getDrift( pitch.size(), p.driftDep, 4 );
        }
        else {
            drift = null;
        }

        // calculate final pitch contour
        final double[] integr = new double[pitch.size()];

        double total = 0.0;
        for (int i = 0; i < pitch.size(); i++) {
            final double d = pitch.get( i ) * (1 + vibrato * vibrato_multipl + (drift != null ? drift.get( i ) : 0) + (jitter != null ? jitter[i] : 0));
            pitch.set( i, d );

            total += d;

            integr[i] = total;
            integr[i] /= 44100.0;
        }

        // generate sound with harmonics
        final double[] ampl = new double[pitch.size()];
        for (int h = 1; h < nHarmonics + 1; h++) {
            for (int i = 0; i < ampl.length; i++) {
                ampl[i] += Math.sin( 2 * h * Math.PI * integr[i] ) * Math.pow( h, -p.rolloff / 10.0 ); //NB: not 2^rolloff but h^rolloff (amplitude ~halves every octave, not every harmonic!). Visualization: plot (1:10, (1:10)^(-rolloff/10))   Lindblad (1992, "Röst") cites 12 dB as normal for humans. Alternatively, +pi/2 to shift the phase and start with max, not 0 (but then the shape is weird); good for adding vocal fry, shimmer etc - ampl vector begins in the middle of the first "glottal cycle"
            }
        }

        // add shimmer (random variation in amplitude)
        if (p.shimmerDep > 0) {
            addShimmer( ampl, pitch, p.shimmerDep, gc );
        }

        // add vocal fry (dampen or mute some % of glottal cycles, as in constricted voice)
        if (p.subharmDep > 0) {
            addVocalFry( ampl, pitch, p.nSubharm, p.subharmDep, gc );
        }

        List<Double> sound_new = applyFFT( ampl, p, filter );

        // add general fade in / fade out
        if (p.attackLen > 0) {
            sound_new = fadeInOutLinear( sound_new, true, true, (int) Math.floor( p.attackLen * 44.1 ) );
        }

        // add breathing
        if (p.breathingType == null || p.breathingType.isEmpty() || p.breathingStrength <= 0) {
            return sound_new;
        }
        else {

            List<Double> brBef = null;
            List<Double> brAft = null;
            List<Double> brDur = new ArrayList<>();
            for (int i = 0; i < sound_new.size(); i++) {
                brDur.add( 0.0 );
            }

            if (p.breathingType.contains( BreathingType.BEFORE )) {
                brBef = getBreathing( (int) ((p.breathing_dur + p.attackLen) * 44.1), filter, p );
                brBef = fadeInOutLinear( brBef, true, p.breathingType.contains( BreathingType.DURING ), (int) Math.floor( p.attackLen * 44.1 ) );
            }
            if (p.breathingType.contains( BreathingType.DURING )) {
                brDur = getBreathing( sound_new.size(), filter, p );
                brDur = fadeInOutLinear( brDur, !p.breathingType.contains( BreathingType.BEFORE ), !p.breathingType.contains( BreathingType.AFTER ),
                                (int) (p.attackLen * 44.1) ); // don't fade in/out if preceded/followed by more breathing
            }
            if (p.breathingType.contains( BreathingType.AFTER )) {
                brAft = getBreathing( (int) ((p.breathing_dur + p.attackLen) * 44.1), filter, p );
                brAft = fadeInOutLinear( brAft, !p.breathingType.contains( BreathingType.DURING ), true, (int) (p.attackLen * 44.1) );
            }

            // calculate the fade-in/out according to breathingStrength_diff, mix voiced part with breathing
            if (!p.breathingType.contains( BreathingType.DURING )) {
                // if no breathing is present during the voiced part, fade in/out breathing before/after the voiced part, reversing the given breathingStrength_diff for the "after"-part

                if (brBef != null) {
                    brBef = fadeInOutExponential( brBef, p.breathingStrength_diff );
                    sound_new = crossFade( brBef, sound_new, 2 );
                }
                if (brAft != null) {
                    brAft = fadeInOutExponential( brAft, -p.breathingStrength_diff );
                    sound_new = crossFade( sound_new, brAft, 2 );
                }
            }
            else {
                // if breathing is present during the voiced part, put together all breathing elements and fade in/out together
                List<Double> breathing;

                int padDir = 2;
                if (p.breathingType.contains( BreathingType.BEFORE ) && !p.breathingType.contains( BreathingType.AFTER )) {
                    padDir = 0;
                    breathing = crossFade( brBef, brDur, 2 );
                }
                else if (!p.breathingType.contains( BreathingType.BEFORE ) && p.breathingType.contains( BreathingType.AFTER )) {
                    padDir = 1;
                    breathing = crossFade( brDur, brAft, 2 );
                }
                else if (p.breathingType.contains( BreathingType.BEFORE ) && p.breathingType.contains( BreathingType.AFTER )) {
                    padDir = 2;
                    breathing = crossFade( brBef, brDur, 2 );
                    breathing = crossFade( breathing, brAft, 2 );
                }
                else {
                    breathing = brDur;
                }

                breathing = fadeInOutExponential( breathing, p.breathingStrength_diff );

                sound_new = matchLengths( sound_new, breathing.size(), padDir ); // pad with 0 to length(breathing) adding zeros left/right/both. Note that the length of two crossFaded files is hard to predict (b/c of looking for zero crossings), hence the need for this roundabout way here to make sure the length of breathing is the same as the length of sound_new
                assert sound_new.size() == breathing.size();

                for (int i = 0; i < sound_new.size(); i++) {
                    sound_new.set( i, (1 - p.breathingStrength) * sound_new.get( i ) + breathing.get( i ) * p.breathingStrength );
                }
            }
            return sound_new;
        }

    }

    /**
     * generates breathing noise (white noise instead of a periodic signal) filtered by the same spectral envelope as the periodic component. Returns waveform as a numeric vector
     *
     * @param len
     * @param filter
     * @param breathingStrength_diff
     * @param windowLength_points
     * @param overlap
     * @return
     */
    private static List<Double> getBreathing( int len, double[] filter, BoutParameters p ) {

        if (len <= 0) {
            return new ArrayList<>();
        }
        final double noise[] = new double[2 * len];
        for (int i = 0; i < 2 * len; i++) {
            noise[i] = Math.random() * 2 - 1;
        }

        // apply filter
        List<Double> breathing = applyFFT( noise, p, filter );

        // make sure the length of breathing is exactly right. Needed because FFT + inverse FFT does not return a vector of exactly the same length as the original
        breathing = matchLengths( breathing, len, 2 );

        return breathing;

    }

    /**
     * creates random variations in pitch
     *
     * @param size
     * @param driftDep
     * @param driftPeriod_points controls the rate of change (fast wiggling vs. slow modulation of pitch contour)
     * @return a random walk of length(time) with the amount of change controlled by driftDep (in semitones)
     */
    private static List<Double> getDrift( int time, int driftDep, int driftPeriod_points ) {

        final double drift_multipl = Math.pow( 2, driftDep / 12.0 ) - 1; // convert from semitones to % of F0
        double total = 0;
        final Double[] drift = new Double[time];

        double minDrift = Double.MAX_VALUE;
        /* essentially a random walk, longer than needed because median smoothing in rollmedian() needs more data before and after.
         *   1000 to speed up the process (MUCH faster than doing a full-resolution random walk and smoothing afterwards)
         */
        for (int i = 0; i < drift.length; i++) {
            drift[i] = total;
            if (RANDOM.nextBoolean()) {
                total++;
            }
            else {
                total--;
            }
            minDrift = Math.min( minDrift, drift[i] );
        }

        double maxDrift = Double.MIN_VALUE;
        // normalize so it starts with zero (aligned with pitchStart) and spans driftDep
        for (int i = 0; i < drift.length; i++) {
            drift[i] -= minDrift;
            maxDrift = Math.max( drift[i], maxDrift );
        }

        double first = 0;

        for (int i = 0; i < drift.length; i++) {
            drift[i] /= maxDrift;
            drift[i] *= drift_multipl;
            if (i == 0) {
                first = drift[i];
                drift[i] = 0.0;
            }
            else {
                drift[i] -= first;
            }
        }

        return Arrays.asList( drift );
    }

    /**
     * returns a 'len'-long vector (spectral filter). Add it to your log-spectrum to obtain a vowel-like spectral envelope.
     * By default it returns a random pseudo-vowel, but formants can also be fully specified (exactFormants=data.frame(...))
     * The actual numbers are best taken from real live recordings, not standard reference tables or wikipedia - sounds much more natural.
     * Just record someone and measure the frequency of peaks on log-spectrum in Audacity or PRAAT.
     *
     * NB: unlike the frequencies, the amplitudes of formants are very tricky, since they are also affected by rolloff,
     * spectralSlope, and formantStrength.
     * @param exactFormants
     * @param maleFemale
     * @param randomVowel
     * @param lenFilter
     * @return
     */
    private static double[] getFormantEnvelope( Formant exactFormants, boolean maleFemale, boolean randomVowel, int len ) {
        double[] formantFreq;
        double[] formantAmp;
        double[] formantBandwidth;
        if (randomVowel) {
            formantFreq = new double[4];
            formantFreq[0] = 200 + (900 - 200) * Math.random();
            for (int i = 1; i < formantFreq.length; i++) {
                formantFreq[i] = formantFreq[i - 1] + 350 + (2500 - 350) * Math.random();
            }
            final NormalDistribution rnorm = new NormalDistribution( 1, .1 );
            formantAmp = new double[formantFreq.length];
            for (int i = 0; i < formantAmp.length; i++) {
                formantAmp[i] = rnorm.sample();
            }

            final GammaDistribution rgamma = new GammaDistribution( 200 * 200 / (100.0 * 100.0), 1 / (200 / (100.0 * 100.0)) ); //R code had rate, Java lib needs scale which is 1/rate
            formantBandwidth = new double[formantFreq.length];
            for (int i = 0; i < formantBandwidth.length; i++) {
                formantBandwidth[i] = rgamma.sample();
            }

        }
        else if (exactFormants != null) {
            formantFreq = new double[] { exactFormants.f1_freq, exactFormants.f2_freq, exactFormants.f3_freq, exactFormants.f4_freq };
            formantAmp = new double[] { exactFormants.f1_amp, exactFormants.f2_amp, exactFormants.f3_amp, exactFormants.f4_amp };
            formantBandwidth = new double[] { exactFormants.f1_width, exactFormants.f2_width, exactFormants.f3_width, exactFormants.f4_width };
        }
        else {
            throw new IllegalArgumentException( "Error: either define formants or specify randomVowel=true" );
        }

        // adjust formants for male-female voices
        if (maleFemale) {
            for (int i = 0; i < formantFreq.length; i++) {
                formantFreq[i] *= 2;
            }
        }

        final double[] formantEnvelope = new double[22050]; //since formant frequencies are given in Hz, we start by taking a full-length filter from 1 to Nyquist freq (22050), then shorten it to length 'len'

        for (int f = 0; f < formantFreq.length; f++) {
            final double mg = formantFreq[f]; // mean of gamma distribution. Could use normal instead, but gamma is pretty handy in this case
            final double sdg = formantBandwidth[f]; // sd of gamma distribution
            final GammaDistribution dgamma = new GammaDistribution( mg * mg / (sdg * sdg), 1 / (mg / (sdg * sdg)) ); //R code had rate, Java lib needs scale which is 1/rate
            final double[] formant = new double[22050];
            double maxFormant = Double.MIN_VALUE;
            for (int i = 0; i < 22050; i++) {
                formant[i] = dgamma.density( i + 1 );
                maxFormant = Math.max( maxFormant, formant[i] );
            }
            for (int i = 0; i < 22050; i++) {
                formantEnvelope[i] += formant[i] / maxFormant * formantAmp[f];
            }

        }

        final double deltaFormat = (formantEnvelope.length - 1) / (len - 1);
        final double[] ret = new double[len];

        for (int i = 0; i < len; i++) {
            ret[i] = formantEnvelope[(int) (i * deltaFormat)];
        }
        return ret;

    }

    /**
     * @param pitch
     * @return a vector of indices giving the borders between "glottal cycles",
     * assuming that we know the true F0 at each time point (as in synthesized sounds)
     * and that max amplitude gives us the center of a glottal cycle
     */
    private static List<Integer> getGlottalCycles( List<Double> pitch ) {
        final List<Integer> glottalCycles = new ArrayList<>();
        int i = 0; // the first border is the first time point
        while (i < pitch.size()) {
            glottalCycles.add( i );
            i = (int) (i + Math.floor( 44100.0 / pitch.get( i ) )); // take steps proportionate to the current F0
        }
        if (glottalCycles.get( glottalCycles.size() - 1 ) != pitch.size()) {
            glottalCycles.add( pitch.size() );// the last border is simply the last time point
        }
        return glottalCycles;
    }

    /**
     * generates jitter (random variation of F0 with sd = jitterDep semitones)
     *
     * @param size
     * @param gc
     * @param jitterDep
     * @return
     */
    private static double[] getJitter( int time, List<Integer> glottalCycles, int jitterDep ) {
        if (jitterDep <= 0) {
            throw new IllegalArgumentException( "jitterDept must be positive" );
        }

        final int nInt = glottalCycles.size() - 1; // these are the intervals whose F0 is to be adjusted at random

        final double[] jitter;

        if (nInt >= 2) {
            jitter = new double[time];
            final double jitter_multipl = Math.pow( 2, jitterDep / 12.0 ) - 1; // convert semitones to Hz
            final NormalDistribution norm = new NormalDistribution( 0, jitter_multipl );
            final double[] rnorm = new double[nInt];
            for (int i = 0; i < rnorm.length; i++) {
                rnorm[i] = norm.sample();
            }
            int jitterOffset = 0;
            for (int i = 1; i < glottalCycles.size(); i++) {
                final int diff = glottalCycles.get( i ) - glottalCycles.get( i - 1 );
                for (int j = 0; j < diff; j++) {
                    jitter[jitterOffset] = rnorm[i - 1];
                    jitterOffset++;
                }

            }
            while (jitterOffset < jitter.length) {
                jitter[jitterOffset] = rnorm[rnorm.length - 1];
                jitterOffset++;
            }
        }
        else {
            jitter = null;
        }

        return jitter;
    }

    public static List<Double> getPitchContour( int duration_ms, int pitch_start, int pitch_anchor, int pitch_end, int pitch_anchor_location, boolean female ) {
        final int time = (int) Math.floor( duration_ms * 44.1 );
        final double pitch_anchor_time = pitch_anchor_location / 100.0 * duration_ms;
        final double pitch_anchor_time_points = Math.floor( pitch_anchor_time * 44.1 );

        double[] x;
        double[] y;

        if (pitch_anchor_location < 30) {
            x = new double[] { 0, pitch_anchor_time_points, time - pitch_anchor_time_points, time };
            y = new double[] { pitch_start, pitch_anchor, Math.floor( pitch_anchor / 1.1 ), pitch_end };
        }
        else {
            x = new double[] { 0, pitch_anchor_time_points, time };
            y = new double[] { pitch_start, pitch_anchor, pitch_end };
        }

        final LoessInterpolator inter = new LoessInterpolator( .75, 4 );

        final PolynomialSplineFunction function = inter.interpolate( x, y );
        //let's draw a smooth curve through the given pitch anchors

        final List<Double> ret = new ArrayList<>();
        for (int i = 0; i < time; i++) {
            ret.add( function.value( i ) * (female ? 2 : 1) );
        }

        //If the interpolation gave us some negative pitch, we transpose this by Math.abs(minimum)

        final Double min = ret.stream().collect( Collectors.summarizingDouble( Double::doubleValue ) ).getMin();
        if (min < 0) {
            final double delta = Math.abs( min );
            ret.replaceAll( d -> d + delta );
        }

        return ret;
    }

    /**
     * returns the full spectral filter, with a basic linear rolloff (spectralSlope), formants, high-freq amplifier (megaFormant), and some stochastic component (spectralNoise)
     * @param randomVowel
     * @param maleFemale
     * @param exactFormants
     * @param megaFormant_strength
     * @param megaFormant_sd
     * @param megaFormant_mean
     * @param plotFilter
     * @param spanFilter
     * @param spectralNoise_strength
     * @param lenRarFilter
     * @param formantStrength
     * @param spectralSlope
     * @param spectralSlope the basic shape of the final spectral envelope is linear, by default going from 0 dB at 0 Hz down into negative dB's (ie, with slope 0.3 we reach -120 dB at ~8613 Hz)
     * @param formantStrength the height of formants above the basic spectral envelope, in dB
     * @param lenRarFilter  how many points to include in the rarefied spectrum (defines how smooth the spectral filter will be)
     * @param spectralNoise_strength max height of randomly generated peaks and valleys in the spectrum, in dB. The smaller spectralNoise_strength, the closer to a simple linear rolloff from low to high frequencies + specified formants, with no random deviations
     * @param spanFilter regulates the amount of smoothing (which depends on lenRarFilter, so these two work together)

     * @param megaFormant_mean an additional broad "formant" amplifying/dumping high frequencies for creaky/breathy voices
     * @param megaFormant_sd an additional broad "formant" amplifying/dumping high frequencies for creaky/breathy voices
     * @param megaFormant_strength an additional broad "formant" amplifying/dumping high frequencies for creaky/breathy voices
     * @param exactFormants
     * @param maleFemale
     * @param randomVowel
     * @param windowLength_points
     * @param exactFormants
     * @return
     */
    private static double[] getSpectralEnvelope( double spectralSlope, int formantStrength, int lenRarFilter, int spectralNoise_strength, double spanFilter,
                    int megaFormant_mean, int megaFormant_sd, int megaFormant_strength, boolean maleFemale, boolean randomVowel, int windowLength_points,
                    Formant exactFormants ) {

        // compute the basic shape of spectral envelope (log scale): linear decay controlled by spectralSlope
        final int lenFilter = (int) Math.floor( windowLength_points / 2.0 );
        final double[] specEnv_basic = new double[lenFilter];
        for (int i = 0; i < lenFilter; i++) {
            specEnv_basic[i] = (i + 1) * -spectralSlope;
        }

        // get a megaFormant, which amplifies high frequencies for constricted, creaky voices
        double[] megaFormant;
        if (megaFormant_strength != 0) {
            final NormalDistribution norm = new NormalDistribution( megaFormant_mean, megaFormant_sd );
            megaFormant = new double[lenFilter];
            double maxMegaFormant = Double.MIN_VALUE;
            for (int i = 0; i < lenFilter; i++) {
                megaFormant[i] = norm.density( Math.min( i, 22050 ) );
                maxMegaFormant = Math.max( maxMegaFormant, megaFormant[i] );
            }
            for (int i = 0; i < lenFilter; i++) {
                megaFormant[i] = megaFormant[i] / maxMegaFormant * megaFormant_strength;
            }
        }
        else {
            megaFormant = null;
        }

        // compute some random component to make the filter more naturalistic/individual
        // decreasing SDs of spectral deviations
        final double[] SDs = new double[lenRarFilter];
        final double delta = .9 / (lenRarFilter - 1); //seq of .1 til 1 with lenRarFilterSteps;
        double v = .1;
        for (int i = 0; i < lenRarFilter; i++) {
            SDs[i] = v;
            v += delta;
        }
        final double[] noise_short = new double[lenRarFilter];
        double maxNoise = Double.MIN_VALUE;
        for (int i = 0; i < lenRarFilter; i++) {
            final NormalDistribution n = new NormalDistribution( 0.0, SDs[i] );
            noise_short[i] = n.sample();
            maxNoise = Math.max( maxNoise, Math.abs( noise_short[i] ) );
        }
        for (int i = 0; i < lenRarFilter; i++) {
            noise_short[i] /= maxNoise;
        }

        // interpolate the noise to have enough points
        final int[] freqs = new int[lenRarFilter];
        final double deltaFreq = (lenFilter - 1) / (double) lenRarFilter;
        double vFreq = 1;
        for (int i = 0; i < lenRarFilter; i++) {
            freqs[i] = (int) Math.round( vFreq );
            vFreq += deltaFreq;
        }
//        double[] noise_fullLength = new double[lenFilter];
//        for (int i = 0; i < lenRarFilter; i++) { //lenRarFilter is the length of noise_short
//            noise_fullLength[freqs[i]] = noise_short[i];
//        }

        //TODO span is ignored her
        final double[] xVal = new double[noise_short.length];

        for (int i = 0; i < noise_short.length; i++) {
            xVal[i] = i;
        }
        final LoessInterpolator fitter = new LoessInterpolator( .75, 4 );
        final PolynomialSplineFunction function = fitter.interpolate( xVal, noise_short );

        final double[] noise_fullLength = new double[lenFilter];
        double maxNoiseFullLength = Double.MIN_VALUE;
        final double deltaNoise = (noise_short.length - 1) / (double) (noise_fullLength.length - 1); // seq(0:63 lenOut=1024)
        for (int i = 0; i < noise_fullLength.length; i++) {
            noise_fullLength[i] = function.value( i * deltaNoise );
            maxNoiseFullLength = Math.max( maxNoiseFullLength, Math.abs( noise_fullLength[i] ) );
        }

        // normalize to spectralNoise_strength
        for (int i = 0; i < noise_fullLength.length; i++) {
            noise_fullLength[i] /= maxNoiseFullLength;
            noise_fullLength[i] *= spectralNoise_strength;
        }

        // get formants and normalize to range from 0 to formantStrength
        double[] formantEnvelope;
        if (formantStrength > 0) {
            formantEnvelope = getFormantEnvelope( exactFormants, maleFemale, randomVowel, lenFilter );
            for (int i = 0; i < formantEnvelope.length; i++) {
                formantEnvelope[i] *= formantStrength;
            }
        }
        else {
            formantEnvelope = new double[lenFilter];
            Arrays.fill( formantEnvelope, 1 );
        }

        // get final filter by adding all components
        final double[] log_filter = new double[lenFilter];
        double maxLogFilter = Double.MIN_VALUE;

        for (int i = 0; i < lenFilter; i++) {
            log_filter[i] = specEnv_basic[i] + (megaFormant != null ? megaFormant[i] : 0.0) + noise_fullLength[i] + formantEnvelope[i];
            maxLogFilter = Math.max( maxLogFilter, log_filter[i] );
        }

        // normalize to range from 0 dB to ~formantStrength+spectralNoise_strength dB
        for (int i = 0; i < log_filter.length; i++) {
            log_filter[i] -= maxLogFilter; // ranges from 0 to minus smth
        }
        // convert from dB to linear multiplier
        final double[] filter = new double[log_filter.length];
        for (int i = 0; i < filter.length; i++) {
            filter[i] = Math.pow( 2, log_filter[i] / 10.0 );
        }

        return filter;
    }

    public static void main( String[] args ) throws IOException, LineUnavailableException {

        final Generator g = new Generator();
        final BoutParameters p = new BoutParameters();
//        p.breathingType = new HashSet();
//        p.breathingType.add( BreathingType.BEFORE );
//        p.breathingType.add( BreathingType.DURING );
//        p.breathingType.add( BreathingType.AFTER );
//        p.breathingStrength = .7;
//        p.breathing_dur = 1000;

        //        p.subharmDep = 20;
//        p.nSubharm = 1;
//        p.shimmerDep = 0;
//        p.driftDep = 0;
//        p.formantStrength = 100;
//        p.jitterDep = 0;
//        p.pitch_start = 200;
//        p.pitch_anchor = 900;
//        p.pitch_anchor_location = 50;
//        p.pitch_end = 900;
        final Formant f = new Formant();
        f.f1_freq = 900;
        f.f2_freq = 1300;
        f.f3_freq = 2900;
        f.f4_freq = 4300;
        f.f1_amp = 1;
        f.f2_amp = 1;
        f.f3_amp = .8;
        f.f4_amp = .8;
        f.f1_width = 100;
        f.f2_width = 100;
        f.f3_width = 200;
        f.f4_width = 400;
//        p.exactFormants = f;
        p.randomVowel = true;
        p.syllableDuration_mean = 2000;
//        final List<Double> curve = g.getPitchContour( p.syllableDuration_mean, p.pitch_start, p.pitch_anchor, p.pitch_end, p.pitch_anchor_location );
        final List<Double> curve = Generator.generateBout( p );

        final Double max = curve.stream().collect( Collectors.summarizingDouble( Double::doubleValue ) ).getMax();

        final ByteBuffer audioBuffer = ByteBuffer.allocate( curve.size() * Short.BYTES );
        final double pow = 65536;
        for (final Double d : curve) {
            final int v = (int) (d.doubleValue() / max * pow);
            audioBuffer.put( (byte) v );
            audioBuffer.put( (byte) (v >> 8) );

        }

        final byte[] audioBytes = audioBuffer.array();

        final AudioFormat format = new AudioFormat( 44100, 16, 1, true, false );
        final SourceDataLine audioSource = AudioSystem.getSourceDataLine( format );
        audioSource.open( format );
        audioSource.start();
        audioSource.write( audioBytes, 0, audioBytes.length );
        audioSource.stop();
        audioSource.close();
//        final FileWriter o = new FileWriter( "out.txt" );
//        final BufferedWriter out = new BufferedWriter( o );
//        final NumberFormat format = NumberFormat.getNumberInstance();
//        format.setMaximumFractionDigits( 10 );
//        for (int i = 0; i < curve.size(); i++) {
//            out.write( i + ";" + format.format( curve.get( i ) ) + System.lineSeparator() );
//        }
//        out.close();
    }

    /**
     * adjusts a vector to match the required length by either trimming one or both ends or padding them with zeros.
     * @param sound_new
     * @param length
     * @param padDir specifies where to cut/add zeros ('left' = 0 / 'right' = 1 / 'central'  = 2)
     * @return
     * @return
     */
    private static List<Double> matchLengths( List<Double> myseq, int len, int padDir ) {

        if (padDir == -1 || myseq.size() == len) {
            return myseq;
        }

        switch (padDir) {
            case 2:
                if (myseq.size() < len) { // for padding, first add a whole lot of zeros and then trim using the same algorithm as for trimming
                    for (int i = 0; i < len; i++) {
                        myseq.add( 0, 0.0 );
                    }
                    for (int i = 0; i < len; i++) {
                        myseq.add( 0.0 );
                    }
                }
                final double halflen = len / 2.0;
                final double center = (1 + myseq.size()) / 2.0;
                final int start = (int) Math.ceil( center - halflen );
                myseq = myseq.subList( start, start + len - 1 );
                break;
            case 0:
                while (myseq.size() > len) {
                    myseq.remove( 0 );
                }
                while (myseq.size() < len) {
                    myseq.add( 0, 0.0 );
                }
                break;
            case 1:
                while (myseq.size() > len) {
                    myseq.remove( myseq.size() - 1 );
                }
                while (myseq.size() < len) {
                    myseq.add( 0.0 );
                }
                break;
            default:
                throw new IllegalStateException();
        }
        return myseq;
    }
}
