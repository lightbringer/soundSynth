package se.lu.lucs.sound.evolver;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.math3.exception.OutOfRangeException;

import se.lu.lucs.sound.AcousticParameters;
import se.lu.lucs.sound.BoutParameters;
import se.lu.lucs.sound.Generator;

public class Evolver implements AcousticParameters {
    public static class Genome {

        public BoutParameters parameters;
        public boolean isEvaluate;
        public List<Double> fitness;

        Genome() {
            this( new BoutParameters() );
        }

        Genome( BoutParameters p ) {
            super();
            parameters = p;
            fitness = new ArrayList<>();
        }

        public Genome( BoutParameters p, Double f ) {
            this( p );
            fitness.add( f );
        }

        Genome( Genome other ) {
            this( new BoutParameters( other.parameters ) );

            isEvaluate = other.isEvaluate;
            fitness = new ArrayList<>( other.fitness );
        }

        public Double fitness() {
            return fitness.stream().collect( Collectors.averagingDouble( Double::doubleValue ) );
        }
    }

    public final static int DEFAULT_POP_SIZE = 2000;

    public final static float DEFAULT_CROSSOVER_PROBABILITY = .5f;

    public final static float DEFAULT_MUTATION_PROBABILITY = .5f;

    private final static Random RANDOM = new Random();

    public static final float DEFAULT_CROSSOVER_PROBABILTY = .9f;

    public static final float DEFAULT_MUTATE_PROBABILITY = .1f;

    public static final float DEFAULT_ELITE_SIZE = .4f;

    private final static AudioFormat FORMAT = new AudioFormat( 44100, 16, 1, true, false );

    public static void main( String[] args ) throws UnsupportedAudioFileException, IOException, LineUnavailableException {

//        final MorphTargetAmplitude t = new MorphTargetAmplitude( new File( "aaa_target.wav" ) );
        final SpectogramFitness sp = new SpectogramCorrelation( new File( "aaa_target.wav" ) );
        final Evolver e = new Evolver( 50, sp );
        e.setCrossoverProbability( .8f );
        e.setMutateProbability( .6f );
        e.setEliteSize( .3f );
        e.setAlwaysEvaluate( true );
        e.setEvaluationSamples( -20 );
        e.getMutationFunction().setMutationStrength( .35 );
        final long time = System.currentTimeMillis();
        e.evolve( 1000, Float.MAX_VALUE );
        System.out.println( System.currentTimeMillis() - time + "ms" );
        System.out.println( e.getBest().parameters );
        final List<Double> curve = Generator.generateBout( e.getBest().parameters );

        playCurve( curve, (long) (curve.size() / 44.1) );

        final AudioInputStream s = Generator.convertAmplitude( curve, FORMAT );

        AudioSystem.write( s, AudioFileFormat.Type.WAVE, new File( "result.wav" ) );

    }

    private static void playCurve( List<Double> curve, long time ) throws LineUnavailableException {
        final Double max = curve.stream().collect( Collectors.summarizingDouble( Double::doubleValue ) ).getMax();

        final ByteBuffer audioBuffer = ByteBuffer.allocate( curve.size() * Short.BYTES );
        final double pow = 65536;
        for (final Double d : curve) {
            final int v = (int) (d.doubleValue() / max * pow);
            audioBuffer.put( (byte) v );
            audioBuffer.put( (byte) (v >> 8) );

        }

        final byte[] audioBytes = audioBuffer.array();

        final SourceDataLine audioSource = AudioSystem.getSourceDataLine( FORMAT );
        audioSource.open( FORMAT );
        audioSource.start();
        audioSource.write( audioBytes, 0, audioBytes.length );
        try {
            System.out.println( "Sleeping " + time + "ms" );
            Thread.sleep( time );
        }
        catch (final InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        audioSource.stop();
        audioSource.close();

    }

    private static float random( float min, float max ) {
        return (float) (min + RANDOM.nextDouble() * (max - min));
    }

    private static Genome rouletteWheel( List<Genome> population2, int eliteBoundary ) {

        float totalWeight = 0;
        for (int i = 0; i < eliteBoundary; i++) {
            final Double w = population2.get( i ).fitness();
            assert w >= 0 : "negative weights not allowed";
            // increase sum
            totalWeight += w;
        }
        if (totalWeight > 0) {
            // roulette wheel selection
            double r = RANDOM.nextDouble() * totalWeight;
            int i = 0;
            while (i < eliteBoundary && r > 0) {
                r -= population2.get( i ).fitness();
                i++;
            }
            return population2.get( i - 1 );
        }
        else {
            throw new IllegalStateException( "all fitnesses are zero" );
        }

    }

    private int evaluationSamples;

    private float crossoverProbability;

    private float mutateProbability;

    private float eliteSize;

    private int populationSize;

    private List<Genome> population;

    private final FitnessFunction fitnessFunction;

    private boolean alwaysEvaluate;
    private CrossOverFunction crossOverFunction;

    private MutationFunction mutationFunction;

    private BoutParameterGenerator generator;

    public Evolver( FitnessFunction f ) {
        this( DEFAULT_POP_SIZE, f );
    }

    public Evolver( float crossoverProbability, float mutateProbability, float eliteSize, int populationSize, List<Genome> population,
                    FitnessFunction fitnessFunction ) {
        super();
        this.crossoverProbability = crossoverProbability;
        this.mutateProbability = mutateProbability;
        this.eliteSize = eliteSize;
        this.populationSize = populationSize;
        this.population = population;
        this.fitnessFunction = fitnessFunction;

        mutationFunction = new MutateAll();
        crossOverFunction = new RandomCrossOver();
        generator = this::randomBout;

        while (population.size() < populationSize) {
            population.add( randomGenome() );
        }
    }

    public Evolver( int populationSize, FitnessFunction fitnessFunction ) {
        this( DEFAULT_CROSSOVER_PROBABILTY, DEFAULT_MUTATE_PROBABILITY, DEFAULT_ELITE_SIZE, populationSize, new ArrayList<Genome>(), fitnessFunction );

    }

    public void advancePopulation() {
//        assert population.size() % 2 == 0;

        //Sort the population by fitness. If we're minimising the fitness, the highest element
        //will be last
        final Comparator<Genome> comp = ( e2, e1 ) -> Double.compare( e1.fitness(), e2.fitness() );

        Collections.sort( population, comp );

        //Cull the population until only its elite remains and it's of odd length
        final int eliteRemainder = (int) (population.size() * eliteSize);

        while (population.size() > eliteRemainder) {
            population.remove( population.size() - 1 );
        }

        //Refill the population with offsprings of the elite (if any)
        //However, procreate may skip some, based on this.crossoverProbality.
        //These will be replaced with random individuals
        final int eliteBoundary = population.size();

        final Genome[] temp = new Genome[2];
        while (population.size() < populationSize) {
            boolean addMe = false;
            //If we have kept some elites AND there is a chance to combine/mutate them
            //Otherwise, we would just create copies of the elite
            if (eliteBoundary > 0 && (mutateProbability > 0f || crossoverProbability > 0f)) {
                temp[0] = rouletteWheel( population, eliteBoundary );
                temp[1] = rouletteWheel( population, eliteBoundary );
                if (Math.random() > 1f - crossoverProbability) {
//                  System.out.println( "Crossing " + population.indexOf( temp[0] ) + " and " + population.indexOf( temp[1] ) );
                    final BoutParameters[] tempBout = crossOverFunction.crossOver( temp[0].parameters, temp[1].parameters );
                    temp[0] = new Genome( tempBout[0] );
                    temp[1] = new Genome( tempBout[1] );

                    addMe = true;
                }
                else {
                    temp[0] = new Genome( temp[0] );
                    temp[1] = new Genome( temp[1] );
                }

                if (RANDOM.nextDouble() > 1f - mutateProbability) {
                    addMe = true;
                    mutationFunction.mutate( temp[0].parameters );
                    mutationFunction.mutate( temp[1].parameters );
                }

            }
            else {
//                System.out.println( "Adding two random" );
                addMe = true;
                temp[0] = randomGenome();
                temp[1] = randomGenome();

            }
            //If addMe is false, we just created two genomes that are an exact copy of their parents and not mutated
            if (addMe) {
                population.add( temp[0] );
                if (population.size() < populationSize) {
                    population.add( temp[1] );
                }
            }
        }

    }

    private void evaluate( Genome g ) {

        if (!g.isEvaluate || alwaysEvaluate) {

            try {
                if (evaluationSamples > 0) {
                    for (int i = 0; i < evaluationSamples; i++) {
                        final List<Double> amplitude = Generator.generateBout( g.parameters );
                        g.fitness.add( fitnessFunction.evaluate( amplitude ) );
                    }
                }
                else {
                    Double p = g.fitness.isEmpty() ? Double.NaN : g.fitness.get( g.fitness.size() - 1 );
                    int iter = 0;
                    while (iter < Math.abs( evaluationSamples )) {
                        final List<Double> amplitude = Generator.generateBout( g.parameters );
                        final Double n = fitnessFunction.evaluate( amplitude );
                        g.fitness.add( n );
                        if (p == n) {
                            break;
                        }
                        p = n;

                        iter++;
                    }
//                    System.out.println( "Stopped after " + iter );
                }
            }
            catch (final Exception e) {
                System.err.println( e.getMessage() );
                g.fitness.clear();
            }
            finally {
                g.isEvaluate = true;
            }
        }
    }

    public void evolve( int maxIterations, float targetFitnessThreshold ) {
        int iter = 0;
        boolean fitnessReached = false;
        Double bestFitness = 0.0;
        Double globalDelta = 0.0;
        while (true) {
            System.out.println( "Starting iteration " + iter );
            //Evaluate all genomes
            population.parallelStream().forEach( g -> evaluate( g ) );

            advancePopulation();

            final Double newFitness = population.get( 0 ).fitness();
            final Double delta = iter > 0 ? newFitness - bestFitness : 0;
            System.out.println( "delta " + delta );
            globalDelta += delta;
            System.out.println( "globalDelta " + globalDelta );
            bestFitness = newFitness;
            System.out.println( "Max fitness is " + bestFitness );

            fitnessReached = population.get( 0 ).fitness() >= targetFitnessThreshold;
            iter++;

            if (fitnessReached || iter >= maxIterations) {
                break;
            }

        }
    }

    public Genome getBest() {
        return population.get( 0 );
    }

    public CrossOverFunction getCrossOverFunction() {
        return crossOverFunction;
    }

    public BoutParameterGenerator getGenerator() {
        return generator;
    }

    public MutationFunction getMutationFunction() {
        return mutationFunction;
    }

    public List<Genome> getPopulation() {
        return population;
    }

    private BoutParameters randomBout() {
        final BoutParameters parameters = new BoutParameters();
//      parameters.numberOfSyllables = (int) random( MIN_SYLLABLES, MAX_SYLLABLES );
        //FIXME
        parameters.numberOfSyllables = 1;
        parameters.syllableDuration_mean = (int) random( MIN_MEAN_SYLLABLE_LENGTH, MAX_MEAN_SYLLABLE_LENGTH );
        parameters.pauseDuration_mean = (int) random( MIN_SYLLABLE_PAUSE, MAX_SYLLABLE_PAUSE );
        parameters.var_bw_syllables = (int) random( MIN_SYLLABLE_VARIATION, MAX_SYLLABLE_VARIATION );
        parameters.pitch_start = (int) random( MIN_PITCH, MAX_PITCH );
        parameters.pitch_anchor = (int) random( MIN_PITCH, MAX_PITCH );
        parameters.pitch_end = (int) random( MIN_PITCH, MAX_PITCH );
        parameters.pitch_anchor_location = (int) random( MIN_PITCH_ANCHOR_LOCATION, MAX_PITCH_ANCHOR_LOCATION );
        parameters.attackLen = (int) random( MIN_ATTACK_LENGTH, MAX_ATTACK_LENGTH );
        parameters.jitterDep = (int) random( MIN_JITTER, MAX_JITTER );
        parameters.vibratoLen = (int) random( MIN_VIBRATO_LENGTH, MAX_VIBRATO_LENGTH );
        parameters.vibratoDep = (int) random( MIN_VIBRATO_DEPTH, MAX_VIBRATO_DEPTH );
        parameters.shimmerDep = (int) random( MIN_SHIMMER, MAX_SHIMMER );
//      p.driftLen = (int) random(MIN_PITCH, MAX_DRIFT_DEPTH);
        parameters.driftDep = (int) random( MIN_DRIFT_DEPTH, MAX_DRIFT_DEPTH );
        parameters.creakyBreathy = random( MIN_CREAKY_BREATHY, MAX_CREAKY_BREATHY );
        parameters.rolloff = (int) random( MIN_ROLLOFF, MAX_ROLLOFF );
        parameters.spectralSlope = random( MIN_SPECTRAL_SLOPE, MAX_SPECTRAL_SLOPE );
        parameters.formantStrength = (int) random( MIN_FORMANT_STRENGTH, MAX_FORMANT_STRENGTH );
        parameters.spectralNoise_strength = (int) random( MIN_SPECTRAL_NOISE, MAX_SPECTRAL_NOISE );
//      p.megaFormant_mean = (int)
//      p.megaFormant_sd = (int) random(MIN_PITCH, MAX_ME);
        parameters.megaFormant_strength = (int) random( MIN_MEGA_FORMANT_STRENGTH, MAX_MEGA_FORMANT_STRENGTH );
//      public Formant exactFormants = null;
        parameters.femaleVoice = RANDOM.nextBoolean();
//      public boolean randomVowel = true;
        parameters.nSubharm = (int) random( MIN_SUBHARMONICS, MAX_SUBHARMONICS );
        parameters.subharmDep = (int) random( MIN_SUBHARMONICS_STRENGTH, MAX_SUBHARMONICS_STRENGTH );
//      p.lenRarFilter = 64;
//      spanFilter = 0.1;
        parameters.breathingStrength = random( MIN_BREATHING_STRENGTH, MAX_BREATHING_STRENGTH );
        parameters.breathingStrength_diff = random( MIN_BREATHING_DYNAMIC, MAX_BREATHING_DYNAMIC );
        parameters.breathing_dur = (int) random( MIN_BREATHING_STRENGTH, MAX_BREATHING_LENGTH );
//      public Set<BreathingType> breathingType = null;
//      p.overlap = 75;
//      p.windowLength_points = 2048;
        return parameters;
    }

    private Genome randomGenome() {
        final Genome g = new Genome();
        g.parameters = generator.generate();

        return g;
    }

    public void setAlwaysEvaluate( boolean alwaysEvaluate ) {
        this.alwaysEvaluate = alwaysEvaluate;
    }

    public void setCrossOverFunction( CrossOverFunction crossOverfunction ) {
        crossOverFunction = crossOverfunction;
    }

    /**
     * Probability [0,1] that two culled genomes will be replaced with offpsrings
     * of two elite genomes. Setting this to a lower value will replace more culled
     * genomes with new random ones
     * @param crossoverProbability
     */
    public void setCrossoverProbability( float crossoverProbability ) {
        if (crossoverProbability < 0f || crossoverProbability > 1f) {
            throw new OutOfRangeException( crossoverProbability, 0f, 1f );
        }
        this.crossoverProbability = crossoverProbability;
    }

    /**
     * Sets the percentage [0,1] that is NOT culled after the evaluation,
     * i.e. 1 keeps the whole population and no new individuals will be inserted,
     * 0 fills the next generation with random genomes
     *
     * @param eliteSize
     */
    public void setEliteSize( float eliteSize ) {
        if (eliteSize < 0f || eliteSize > 1f) {
            throw new OutOfRangeException( eliteSize, 0f, 1f );
        }

        this.eliteSize = eliteSize;
    }

    /**
     * Set to < 0 for dynamic sampling
     * @param evaluationSamples
     */
    public void setEvaluationSamples( int evaluationSamples ) {
        if (evaluationSamples == 0) {
            throw new IllegalArgumentException( "sample size must be != 0" );
        }
        this.evaluationSamples = evaluationSamples;
    }

    public void setGenerator( BoutParameterGenerator generator ) {
        this.generator = generator;
    }

    /**
     * The probability [0,1]  that newly created offspring will be mutated
     * @param mutateProbability
     */
    public void setMutateProbability( float mutateProbability ) {
        if (mutateProbability < 0f || mutateProbability > 1f) {
            throw new OutOfRangeException( mutateProbability, 0f, 1f );
        }

        this.mutateProbability = mutateProbability;
    }

    public void setMutationFunction( MutationFunction mutationFunction ) {
        this.mutationFunction = mutationFunction;
    }

    public void setPopulation( List<BoutParameters> selected ) {
        setPopulation( selected, 1.0 );
    }

    public void setPopulation( List<BoutParameters> selected, Double fitness ) {
        setPopulationInternal( selected.stream().map( p -> new Genome( p, fitness ) ).collect( Collectors.toList() ) );

    }

    /**
     * Sets the population to a manually crafted one. The target
     * population size is increased if necessary to the size of the supplied
     * population so that the number of genomes will be maintained
     * @param population
     */
    private void setPopulationInternal( List<Genome> population ) {
        this.population = population;
        populationSize = Math.max( populationSize, population.size() );
    }

    public void setPopulationSize( int populationSize ) {
        if (populationSize < 1) {
            throw new OutOfRangeException( populationSize, 1, Integer.MAX_VALUE );
        }

        this.populationSize = populationSize;
    }

}
