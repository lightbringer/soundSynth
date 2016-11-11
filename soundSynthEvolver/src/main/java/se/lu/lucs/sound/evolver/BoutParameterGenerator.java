package se.lu.lucs.sound.evolver;

import se.lu.lucs.sound.BoutParameters;

@FunctionalInterface
public interface BoutParameterGenerator {
    BoutParameters generate();
}
