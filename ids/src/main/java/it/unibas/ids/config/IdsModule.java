package it.unibas.ids.config;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import it.unibas.ids.analyzer.AnalysisContext;
import it.unibas.ids.analyzer.IEventAnalyzer;
import it.unibas.ids.analyzer.RuleBasedAnalyzer;
import it.unibas.ids.aspect.SuspiciousEventLoggingAspect;
import org.aspectj.lang.Aspects;

public class IdsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IEventAnalyzer.class).to(RuleBasedAnalyzer.class);
        bind(IdsProperties.class).in(Singleton.class);
        bind(AnalysisContext.class).in(Singleton.class);

        requestInjection(Aspects.aspectOf(SuspiciousEventLoggingAspect.class));
    }

}
