package se.lu.lucs.sound.evolver;

import org.apache.commons.math3.distribution.NormalDistribution;

import se.lu.lucs.sound.AcousticParameters;
import se.lu.lucs.sound.BoutParameters;

public class MutateAll implements MutationFunction {
    private static final double DEFAULT_MUTATION_STRENGTH = 0.1;
    protected NormalDistribution normalDistribution;

    public MutateAll() {
        normalDistribution = new NormalDistribution( 0, DEFAULT_MUTATION_STRENGTH );
    }

    @Override
    public void mutate( BoutParameters p ) {

        p.numberOfSyllables = (int) mutate( normalDistribution, p.numberOfSyllables, AcousticParameters.MIN_SYLLABLES, AcousticParameters.MAX_SYLLABLES );
        p.syllableDuration_mean = (int) mutate( normalDistribution, p.syllableDuration_mean, AcousticParameters.MIN_MEAN_SYLLABLE_LENGTH,
                        AcousticParameters.MAX_MEAN_SYLLABLE_LENGTH );
        p.pauseDuration_mean = (int) mutate( normalDistribution, p.pauseDuration_mean, AcousticParameters.MIN_SYLLABLE_PAUSE,
                        AcousticParameters.MAX_SYLLABLE_PAUSE );
        p.var_bw_syllables = (int) mutate( normalDistribution, p.var_bw_syllables, AcousticParameters.MIN_SYLLABLE_VARIATION,
                        AcousticParameters.MAX_SYLLABLE_VARIATION );
        p.pitch_start = (int) mutate( normalDistribution, p.pitch_start, AcousticParameters.MIN_PITCH, AcousticParameters.MAX_PITCH );
        p.pitch_anchor = (int) mutate( normalDistribution, p.pitch_anchor, AcousticParameters.MIN_PITCH, AcousticParameters.MAX_PITCH );
        p.pitch_end = (int) mutate( normalDistribution, p.pitch_end, AcousticParameters.MIN_PITCH, AcousticParameters.MAX_PITCH );
        p.pitch_anchor_location = (int) mutate( normalDistribution, p.pitch_anchor_location, AcousticParameters.MIN_PITCH_ANCHOR_LOCATION,
                        AcousticParameters.MAX_PITCH_ANCHOR_LOCATION );
        p.attackLen = (int) mutate( normalDistribution, p.attackLen, AcousticParameters.MIN_ATTACK_LENGTH, AcousticParameters.MAX_ATTACK_LENGTH );
        p.jitterDep = (int) mutate( normalDistribution, p.jitterDep, AcousticParameters.MIN_JITTER, AcousticParameters.MAX_JITTER );
        p.vibratoLen = (int) mutate( normalDistribution, p.vibratoLen, AcousticParameters.MIN_VIBRATO_LENGTH, AcousticParameters.MAX_VIBRATO_LENGTH );
        p.vibratoDep = (int) mutate( normalDistribution, p.vibratoDep, AcousticParameters.MIN_VIBRATO_DEPTH, AcousticParameters.MAX_VIBRATO_DEPTH );
        p.shimmerDep = (int) mutate( normalDistribution, p.shimmerDep, AcousticParameters.MIN_SHIMMER, AcousticParameters.MAX_SHIMMER );
        //TODO
//            p.driftLen = mutate(p.driftLen, AcousticParameters.MIN_DRIFT_ENGTH, AcousticParameters.MAX_DRIFT_ENGTH);
        p.driftDep = (int) mutate( normalDistribution, p.driftDep, AcousticParameters.MIN_DRIFT_DEPTH, AcousticParameters.MAX_DRIFT_DEPTH );
        p.creakyBreathy = mutate( normalDistribution, p.creakyBreathy, AcousticParameters.MIN_CREAKY_BREATHY, AcousticParameters.MAX_CREAKY_BREATHY );
        p.rolloff = (int) mutate( normalDistribution, p.rolloff, AcousticParameters.MIN_ROLLOFF, AcousticParameters.MAX_ROLLOFF );
        p.spectralSlope = mutate( normalDistribution, p.spectralSlope, AcousticParameters.MIN_SPECTRAL_SLOPE, AcousticParameters.MAX_SPECTRAL_SLOPE );
        p.formantStrength = (int) mutate( normalDistribution, p.formantStrength, AcousticParameters.MIN_FORMANT_STRENGTH,
                        AcousticParameters.MAX_FORMANT_STRENGTH );
        p.spectralNoise_strength = (int) mutate( normalDistribution, p.spectralNoise_strength, AcousticParameters.MIN_SPECTRAL_NOISE,
                        AcousticParameters.MAX_SPECTRAL_NOISE );
        //TODO
//            p.megaFormant_mean = (int) mutate(p.megaFormant_mean,  AcousticParameters.MIN_MEGA_FORMANT_ME, AcousticParameters.MAX_MEGA_FORMANT_STRENGTH);
        //TODO
//            p.megaFormant_sd = mutate(p.megaFormant_sd,  AcousticParameters.MIN_M, AcousticParameters.MAX_SHIMMER);
        p.megaFormant_strength = (int) mutate( normalDistribution, p.megaFormant_strength, AcousticParameters.MIN_MEGA_FORMANT_STRENGTH,
                        AcousticParameters.MAX_MEGA_FORMANT_STRENGTH );
//        public Formant exactFormants = null;
        p.femaleVoice = p.femaleVoice;
//        public boolean randomVowel = true;
        p.nSubharm = (int) mutate( normalDistribution, p.nSubharm, AcousticParameters.MIN_SUBHARMONICS, AcousticParameters.MAX_SUBHARMONICS );
        p.subharmDep = (int) mutate( normalDistribution, p.subharmDep, AcousticParameters.MIN_SUBHARMONICS_STRENGTH,
                        AcousticParameters.MAX_SUBHARMONICS_STRENGTH );
//        public int lenRarFilter = 64;
//        public double spanFilter = 0.1;
        p.breathingStrength = mutate( normalDistribution, p.breathingStrength, AcousticParameters.MIN_BREATHING_STRENGTH,
                        AcousticParameters.MAX_BREATHING_STRENGTH );
        p.breathingStrength_diff = mutate( normalDistribution, p.breathingStrength_diff, AcousticParameters.MIN_BREATHING_DYNAMIC,
                        AcousticParameters.MAX_BREATHING_DYNAMIC );
        p.breathing_dur = (int) mutate( normalDistribution, p.breathing_dur, AcousticParameters.MIN_BREATHING_LENGTH, AcousticParameters.MAX_BREATHING_LENGTH );
//        public Set<BreathingType> breathingType = null;
    }

    @Override
    public void setMutationStrength( double sd ) {
        if (sd != normalDistribution.getStandardDeviation()) {
            normalDistribution = new NormalDistribution( 0, sd );
        }
    }

}
