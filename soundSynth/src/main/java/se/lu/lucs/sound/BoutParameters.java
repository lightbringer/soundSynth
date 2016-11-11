package se.lu.lucs.sound;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class BoutParameters implements AcousticParameters, Serializable {
    public enum BreathingType {
        AFTER, BEFORE, DURING
    }

    public static class Formant {

        public static final int MIN_F1_FREQ = 200;
        public static final int MAX_F1_FREQ = 1200;
        public static final int MIN_F1_AMP = 0;
        public static final int MAX_F1_AMP = 2;
        public static final int MIN_F1_WIDTH = 25;
        public static final int MAX_F1_WIDTH = 400;
        public static final int MIN_F2_FREQ = 600;
        public static final int MAX_F2_FREQ = 3500;
        public static final int MIN_F2_AMP = 0;
        public static final int MAX_F2_AMP = 2;
        public static final int MIN_F2_WIDTH = 25;
        public static final int MAX_F2_WIDTH = 400;
        public static final int MIN_F3_FREQ = 1500;
        public static final int MAX_F3_FREQ = 5000;
        public static final int MIN_F3_AMP = 0;
        public static final int MAX_F3_AMP = 2;
        public static final int MIN_F3_WIDTH = 25;
        public static final int MAX_F3_WIDTH = 400;
        public static final int MIN_F4_FREQ = 2000;
        public static final int MAX_F4_FREQ = 6000;
        public static final int MIN_F4_AMP = 0;
        public static final int MAX_F4_AMP = 2;
        public static final int MIN_F4_WIDTH = 25;
        public static final int MAX_F4_WIDTH = 400;
        public int f1_freq;
        public int f2_freq;
        public int f3_freq;
        public int f4_freq;
        public double f1_amp;
        public double f2_amp;
        public double f3_amp;
        public double f4_amp;
        public int f1_width;
        public int f2_width;
        public int f3_width;
        public int f4_width;

        /**
         * The defaults form an 'a'
         */
        public Formant() {
            f1_freq = 900;
            f2_freq = 1300;
            f3_freq = 2900;
            f4_freq = 4300;
            f1_amp = 1;
            f2_amp = 1;
            f3_amp = .8;
            f4_amp = .8;
            f1_width = 100;
            f2_width = 100;
            f3_width = 200;
            f4_width = 400;
        }

        public Formant( Formant other ) {
            f1_freq = other.f1_freq;
            f2_freq = other.f2_freq;
            f3_freq = other.f3_freq;
            f4_freq = other.f4_freq;
            f1_amp = other.f1_amp;
            f2_amp = other.f2_amp;
            f3_amp = other.f3_amp;
            f4_amp = other.f4_amp;
            f1_width = other.f1_width;
            f2_width = other.f2_width;
            f3_width = other.f3_width;
            f4_width = other.f4_width;
        }

        public Formant( int f1_freq, int f2_freq, int f3_freq, int f4_freq, double f1_amp, double f2_amp, double f3_amp, double f4_amp, int f1_width,
                        int f2_width, int f3_width, int f4_width ) {
            super();
            this.f1_freq = f1_freq;
            this.f2_freq = f2_freq;
            this.f3_freq = f3_freq;
            this.f4_freq = f4_freq;
            this.f1_amp = f1_amp;
            this.f2_amp = f2_amp;
            this.f3_amp = f3_amp;
            this.f4_amp = f4_amp;
            this.f1_width = f1_width;
            this.f2_width = f2_width;
            this.f3_width = f3_width;
            this.f4_width = f4_width;
        }

    }

    public int numberOfSyllables = 1;
    public int syllableDuration_mean = 300;

    public int pauseDuration_mean = 200;
    public int var_bw_syllables = 10;
    public int pitch_start = 100;
    public int pitch_anchor = 150;
    public int pitch_end = 100;
    public int pitch_anchor_location = 10;
    public int attackLen = 50;
    public int jitterDep = 0;
    public int vibratoLen = 100;
    public int vibratoDep = 0;
    public int shimmerDep = 0;
    public int driftLen = 250;
    public int driftDep = 0;
    public double creakyBreathy = 0;
    public int rolloff = 6;
    public double spectralSlope = .3;
    public int formantStrength = 60;
    public int spectralNoise_strength = 40;
    public int megaFormant_mean = 6000;
    public int megaFormant_sd = 2000;
    public int megaFormant_strength = 0;
    public Formant exactFormants = null;
    public boolean femaleVoice = false; //true = female
    public boolean randomVowel = true;
    public int nSubharm = 0;
    public int subharmDep = 0;
    public int lenRarFilter = 64;
    public double spanFilter = 0.1;
    public double breathingStrength = 0;
    public double breathingStrength_diff = 0;
    public int breathing_dur = 100;
    public Set<BreathingType> breathingType = null;
    public int overlap = DEFAULT_OVERLAP;
    public int windowLength_points = DEFAULT_WINDOW_LENGTH;

    public BoutParameters() {

    }

    public BoutParameters( BoutParameters other ) {
        numberOfSyllables = other.numberOfSyllables;
        syllableDuration_mean = other.syllableDuration_mean;
        pauseDuration_mean = other.pauseDuration_mean;
        var_bw_syllables = other.var_bw_syllables;
        pitch_start = other.pitch_start;
        pitch_anchor = other.pitch_anchor;
        pitch_end = other.pitch_end;
        pitch_anchor_location = other.pitch_anchor_location;
        attackLen = other.attackLen;
        jitterDep = other.jitterDep;
        vibratoLen = other.vibratoLen;
        vibratoDep = other.vibratoDep;
        shimmerDep = other.shimmerDep;
        driftLen = other.driftLen;
        driftDep = other.driftDep;
        creakyBreathy = other.creakyBreathy;
        rolloff = other.rolloff;
        spectralSlope = other.spectralSlope;
        formantStrength = other.formantStrength;
        spectralNoise_strength = other.spectralNoise_strength;
        megaFormant_mean = other.megaFormant_mean;
        megaFormant_sd = other.megaFormant_sd;
        megaFormant_strength = other.megaFormant_strength;
        exactFormants = other.exactFormants != null ? new Formant( other.exactFormants ) : null;
        femaleVoice = other.femaleVoice;
        randomVowel = other.randomVowel;
        nSubharm = other.nSubharm;
        subharmDep = other.subharmDep;
        lenRarFilter = other.lenRarFilter;
        spanFilter = other.spanFilter;
        breathingStrength = other.breathingStrength;
        breathingStrength_diff = other.breathingStrength_diff;
        breathing_dur = other.breathing_dur;
        breathingType = other.breathingType != null ? new HashSet( other.breathingType ) : null;
        overlap = other.overlap;
        windowLength_points = other.windowLength_points;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append( getClass().getName() );
        builder.append( " {\n\tnumberOfSyllables: " );
        builder.append( numberOfSyllables );
        builder.append( "\n\tsyllableDuration_mean: " );
        builder.append( syllableDuration_mean );
        builder.append( "\n\tpauseDuration_mean: " );
        builder.append( pauseDuration_mean );
        builder.append( "\n\tvar_bw_syllables: " );
        builder.append( var_bw_syllables );
        builder.append( "\n\tpitch_start: " );
        builder.append( pitch_start );
        builder.append( "\n\tpitch_anchor: " );
        builder.append( pitch_anchor );
        builder.append( "\n\tpitch_end: " );
        builder.append( pitch_end );
        builder.append( "\n\tpitch_anchor_location: " );
        builder.append( pitch_anchor_location );
        builder.append( "\n\tattackLen: " );
        builder.append( attackLen );
        builder.append( "\n\tjitterDep: " );
        builder.append( jitterDep );
        builder.append( "\n\tvibratoLen: " );
        builder.append( vibratoLen );
        builder.append( "\n\tvibratoDep: " );
        builder.append( vibratoDep );
        builder.append( "\n\tshimmerDep: " );
        builder.append( shimmerDep );
        builder.append( "\n\tdriftLen: " );
        builder.append( driftLen );
        builder.append( "\n\tdriftDep: " );
        builder.append( driftDep );
        builder.append( "\n\tcreakyBreathy: " );
        builder.append( creakyBreathy );
        builder.append( "\n\trolloff: " );
        builder.append( rolloff );
        builder.append( "\n\tspectralSlope: " );
        builder.append( spectralSlope );
        builder.append( "\n\tformantStrength: " );
        builder.append( formantStrength );
        builder.append( "\n\tspectralNoise_strength: " );
        builder.append( spectralNoise_strength );
        builder.append( "\n\tmegaFormant_mean: " );
        builder.append( megaFormant_mean );
        builder.append( "\n\tmegaFormant_sd: " );
        builder.append( megaFormant_sd );
        builder.append( "\n\tmegaFormant_strength: " );
        builder.append( megaFormant_strength );
        builder.append( "\n\texactFormants: " );
        builder.append( exactFormants );
        builder.append( "\n\tfemaleVoice: " );
        builder.append( femaleVoice );
        builder.append( "\n\trandomVowel: " );
        builder.append( randomVowel );
        builder.append( "\n\tnSubharm: " );
        builder.append( nSubharm );
        builder.append( "\n\tsubharmDep: " );
        builder.append( subharmDep );
        builder.append( "\n\tlenRarFilter: " );
        builder.append( lenRarFilter );
        builder.append( "\n\tspanFilter: " );
        builder.append( spanFilter );
        builder.append( "\n\tbreathingStrength: " );
        builder.append( breathingStrength );
        builder.append( "\n\tbreathingStrength_diff: " );
        builder.append( breathingStrength_diff );
        builder.append( "\n\tbreathing_dur: " );
        builder.append( breathing_dur );
        builder.append( "\n\tbreathingType: " );
        builder.append( breathingType );
        builder.append( "\n\toverlap: " );
        builder.append( overlap );
        builder.append( "\n\twindowLength_points: " );
        builder.append( windowLength_points );
        builder.append( "\n}" );
        return builder.toString();
    }

}
