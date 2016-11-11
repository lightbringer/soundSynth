package se.lu.lucs.sound.evolver;

import se.lu.lucs.sound.BoutParameters;

@FunctionalInterface
public interface CrossOverFunction {
    BoutParameters[] crossOver( BoutParameters a, BoutParameters b );
}
