package se.lu.lucs.sound.evolver;

import java.util.List;

/**
 * A functional interface to provide a fitness function for amplitudes
 * Note that the evaluate function is required to return higher values
 * for better individuals as the Evolver tries to maximise the score
 *
 * @author Tobias
 *
 */
@FunctionalInterface
public interface FitnessFunction {
    Double evaluate( List<Double> amplitude );
}
