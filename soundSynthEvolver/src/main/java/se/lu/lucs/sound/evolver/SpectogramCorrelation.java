package se.lu.lucs.sound.evolver;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.DoubleStream;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class SpectogramCorrelation extends SpectogramFitness {
    private final static PearsonsCorrelation PEARSON = new PearsonsCorrelation();

    public SpectogramCorrelation( File file ) throws UnsupportedAudioFileException, IOException {
        super( file );
    }

    @Override
    protected Double evaluateSpectogram( List<List<Double>> target, List<List<Double>> candidate ) {
        if (target.isEmpty() || candidate.isEmpty()) {
            throw new IllegalArgumentException();
        }

        double corSum = 0;
//        int skipped = 0;
        for (int i = 0; i < target.size(); i++) {
            final List<Double> tc = target.get( i );
            final List<Double> cc = candidate.get( i );

            if (cc == null || tc == null) {
//                skipped++;
                continue;
            }

            final double[] x = new double[tc.size()];
            for (int j = 0; j < tc.size(); j++) {
                x[j] = tc.get( j );
            }

            final double[] y = new double[cc.size()];
            for (int j = 0; j < tc.size(); j++) {
                y[j] = cc.get( j );
            }
            assert x.length == y.length;

            final double xsum = DoubleStream.of( x ).sum();
            final double ysum = DoubleStream.of( y ).sum();

            if (xsum == 0.0) {
                if (ysum == 0.0) {
                    corSum++;
                }
                continue;
            }

            if (ysum == 0.0) {
                if (xsum == 0.0) {
                    corSum++;
                }
                continue;
            }

            final double cor = PEARSON.correlation( x, y );
            if (Double.isNaN( cor )) {
//                skipped++;
                continue;
            }
            corSum += cor;

        }
//        if (skipped >= target.size()) {
//            return 0f;
//        }
//        else {
        return corSum / target.size();
//        }
    }

}
