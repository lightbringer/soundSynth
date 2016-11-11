package se.lu.lucs.sound;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import se.lu.lucs.sound.evolver.Evolver;

public class SoundBreeder {
    public final static int POPULATION_SIZE = 8;

    private static List<BoutParameters> createBoutParameters() {
        final List<BoutParameters> ret = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            ret.add( new BoutParameters() );
        }

        return ret;
    }

    private final Evolver evolver;

    private List<BoutParameters> population;

    public SoundBreeder() {
        evolver = new Evolver( null );
        evolver.setEliteSize( 1f ); //We will manually refill the population each generation, and we want to keep those we added
        evolver.setMutateProbability( 0f );
        evolver.setCrossoverProbability( 1f );
        evolver.setPopulationSize( POPULATION_SIZE );
    }

    public void advance() {
        evolver.setPopulation( population );

        evolver.advancePopulation();

        population = evolver.getPopulation().stream().map( g -> g.parameters ).collect( Collectors.toList() );
    }

    public List<BoutParameters> getPopulation() {
        if (population == null) {
            population = createBoutParameters();
        }
        return population;
    }

    public void reset() {
        setPopulation( createBoutParameters() );
    }

    public void setPopulation( List<BoutParameters> population ) {
        this.population = population;
    }
}
