package se.lu.lucs.sound.evolver;

import java.util.Random;

import se.lu.lucs.sound.BoutParameters;

public class RandomCrossOver implements CrossOverFunction {
    private final static Random RANDOM = new Random();

    @Override
    public BoutParameters[] crossOver( BoutParameters p1, BoutParameters p2 ) {

        final BoutParameters o1 = new BoutParameters( p1 );
        final BoutParameters o2 = new BoutParameters( p2 );

        if (RANDOM.nextBoolean()) {
            o1.numberOfSyllables = p1.numberOfSyllables;
            o2.numberOfSyllables = p2.numberOfSyllables;
        }
        else {
            o1.numberOfSyllables = p2.numberOfSyllables;
            o2.numberOfSyllables = p1.numberOfSyllables;
        }

        if (RANDOM.nextBoolean()) {
            o1.var_bw_syllables = p1.var_bw_syllables;
            o2.var_bw_syllables = p2.var_bw_syllables;
        }
        else {
            o1.var_bw_syllables = p2.var_bw_syllables;
            o2.var_bw_syllables = p1.var_bw_syllables;
        }

        if (RANDOM.nextBoolean()) {
            o1.pitch_start = p1.pitch_start;
            o2.pitch_start = p2.pitch_start;
        }
        else {
            o1.pitch_start = p2.pitch_start;
            o2.pitch_start = p1.pitch_start;
        }

        if (RANDOM.nextBoolean()) {
            o1.pitch_anchor = p1.pitch_anchor;
            o2.pitch_anchor = p2.pitch_anchor;
        }
        else {
            o1.pitch_anchor = p2.pitch_anchor;
            o2.pitch_anchor = p1.pitch_anchor;
        }

        if (RANDOM.nextBoolean()) {
            o1.pitch_end = p1.pitch_end;
            o2.pitch_end = p2.pitch_end;
        }
        else {
            o1.pitch_end = p2.pitch_end;
            o2.pitch_end = p1.pitch_end;
        }

        if (RANDOM.nextBoolean()) {
            o1.pitch_anchor_location = p1.pitch_anchor_location;
            o2.pitch_anchor_location = p2.pitch_anchor_location;
        }
        else {
            o1.pitch_anchor_location = p2.pitch_anchor_location;
            o2.pitch_anchor_location = p1.pitch_anchor_location;
        }

        if (RANDOM.nextBoolean()) {
            o1.attackLen = p1.attackLen;
            o2.attackLen = p2.attackLen;
        }
        else {
            o1.attackLen = p2.attackLen;
            o2.attackLen = p1.attackLen;
        }

        if (RANDOM.nextBoolean()) {
            o1.jitterDep = p1.jitterDep;
            o2.jitterDep = p2.jitterDep;
        }
        else {
            o1.jitterDep = p2.jitterDep;
            o2.jitterDep = p1.jitterDep;
        }

        if (RANDOM.nextBoolean()) {
            o1.vibratoLen = p1.vibratoLen;
            o2.vibratoLen = p2.vibratoLen;
        }
        else {
            o1.vibratoLen = p2.vibratoLen;
            o2.vibratoLen = p1.vibratoLen;
        }

        if (RANDOM.nextBoolean()) {
            o1.vibratoDep = p1.vibratoDep;
            o2.vibratoDep = p2.vibratoDep;
        }
        else {
            o1.vibratoDep = p2.vibratoDep;
            o2.vibratoDep = p1.vibratoDep;
        }

        if (RANDOM.nextBoolean()) {
            o1.shimmerDep = p1.shimmerDep;
            o2.shimmerDep = p2.shimmerDep;
        }
        else {
            o1.shimmerDep = p2.shimmerDep;
            o2.shimmerDep = p1.shimmerDep;
        }

        if (RANDOM.nextBoolean()) {
            o1.driftLen = p1.driftLen;
            o2.driftLen = p2.driftLen;
        }
        else {
            o1.driftLen = p2.driftLen;
            o2.driftLen = p1.driftLen;
        }

        if (RANDOM.nextBoolean()) {
            o1.driftDep = p1.driftDep;
            o2.driftDep = p2.driftDep;
        }
        else {
            o1.driftDep = p2.driftDep;
            o2.driftDep = p1.driftDep;
        }

        if (RANDOM.nextBoolean()) {
            o1.creakyBreathy = p1.creakyBreathy;
            o2.creakyBreathy = p2.creakyBreathy;
        }
        else {
            o1.creakyBreathy = p2.creakyBreathy;
            o2.creakyBreathy = p1.creakyBreathy;
        }

        if (RANDOM.nextBoolean()) {
            o1.rolloff = p1.rolloff;
            o2.rolloff = p2.rolloff;
        }
        else {
            o1.rolloff = p2.rolloff;
            o2.rolloff = p1.rolloff;
        }

        if (RANDOM.nextBoolean()) {
            o1.spectralSlope = p1.spectralSlope;
            o2.spectralSlope = p2.spectralSlope;
        }
        else {
            o1.spectralSlope = p2.spectralSlope;
            o2.spectralSlope = p1.spectralSlope;
        }

        if (RANDOM.nextBoolean()) {
            o1.formantStrength = p1.formantStrength;
            o2.formantStrength = p2.formantStrength;
        }
        else {
            o1.formantStrength = p2.formantStrength;
            o2.formantStrength = p1.formantStrength;
        }

        if (RANDOM.nextBoolean()) {
            o1.spectralNoise_strength = p1.spectralNoise_strength;
            o2.spectralNoise_strength = p2.spectralNoise_strength;
        }
        else {
            o1.spectralNoise_strength = p2.spectralNoise_strength;
            o2.spectralNoise_strength = p1.spectralNoise_strength;
        }

        if (RANDOM.nextBoolean()) {
            o1.megaFormant_mean = p1.megaFormant_mean;
            o2.megaFormant_mean = p2.megaFormant_mean;
        }
        else {
            o1.megaFormant_mean = p2.megaFormant_mean;
            o2.megaFormant_mean = p1.megaFormant_mean;
        }

        if (RANDOM.nextBoolean()) {
            o1.megaFormant_sd = p1.megaFormant_sd;
            o2.megaFormant_sd = p2.megaFormant_sd;
        }
        else {
            o1.megaFormant_sd = p2.megaFormant_sd;
            o2.megaFormant_sd = p1.megaFormant_sd;
        }

        if (RANDOM.nextBoolean()) {
            o1.megaFormant_strength = p1.megaFormant_strength;
            o2.megaFormant_strength = p2.megaFormant_strength;
        }
        else {
            o1.megaFormant_strength = p2.megaFormant_strength;
            o2.megaFormant_strength = p1.megaFormant_strength;
        }
//        public Formant exactFormants = null;

        if (RANDOM.nextBoolean()) {
            o1.femaleVoice = p1.femaleVoice;
            o2.femaleVoice = p2.femaleVoice;
        }
        else {
            o1.femaleVoice = p2.femaleVoice;
            o2.femaleVoice = p1.femaleVoice;
        }
//        public boolean randomVowel = true;

        if (RANDOM.nextBoolean()) {
            o1.nSubharm = p1.nSubharm;
            o2.nSubharm = p2.nSubharm;
        }
        else {
            o1.nSubharm = p2.nSubharm;
            o2.nSubharm = p1.nSubharm;
        }

        if (RANDOM.nextBoolean()) {
            o1.subharmDep = p1.subharmDep;
            o2.subharmDep = p2.subharmDep;
        }
        else {
            o1.subharmDep = p2.subharmDep;
            o2.subharmDep = p1.subharmDep;
        }
//        public int lenRarFilter = 64;
//        public double spanFilter = 0.1;

        if (RANDOM.nextBoolean()) {
            o1.breathingStrength = p1.breathingStrength;
            o2.breathingStrength = p2.breathingStrength;
        }
        else {
            o1.breathingStrength = p2.breathingStrength;
            o2.breathingStrength = p1.breathingStrength;
        }

        if (RANDOM.nextBoolean()) {
            o1.breathingStrength_diff = p1.breathingStrength_diff;
            o2.breathingStrength_diff = p2.breathingStrength_diff;
        }
        else {
            o1.breathingStrength_diff = p2.breathingStrength_diff;
            o2.breathingStrength_diff = p1.breathingStrength_diff;
        }

        if (RANDOM.nextBoolean()) {
            o1.breathing_dur = p1.breathing_dur;
            o2.breathing_dur = p2.breathing_dur;
        }
        else {
            o1.breathing_dur = p2.breathing_dur;
            o2.breathing_dur = p1.breathing_dur;
        }
//        public Set<BreathingType> breathingType = null;

        return new BoutParameters[] { o1, o2 };
    }

}
