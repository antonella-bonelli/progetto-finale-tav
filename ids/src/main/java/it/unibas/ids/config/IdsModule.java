package it.unibas.ids.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import it.unibas.ids.analyzer.EventAnalyzer;
import it.unibas.ids.analyzer.RuleBasedAnalyzer;

public class IdsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(EventAnalyzer.class).to(RuleBasedAnalyzer.class);
        bind(IdsProperties.class).in(Singleton.class);
    }

}
