package se.lu.lucs.sound.evolver;

import org.apache.commons.math3.distribution.RealDistribution;

import se.lu.lucs.sound.BoutParameters;

public interface MutationFunction {
    void mutate( BoutParameters b );

    default double mutate( RealDistribution d, double mean, double min, double max ) {
        return Math.max( min, Math.min( max, mean + d.sample() * (max - min) ) );
    }

    void setMutationStrength( double f );
}
