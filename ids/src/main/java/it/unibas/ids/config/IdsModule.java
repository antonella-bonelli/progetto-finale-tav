package it.unibas.ids.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import it.unibas.common.interfaces.IEventSubscriber;
import it.unibas.ids.analyzer.AnalysisContext;
import it.unibas.ids.analyzer.IEventAnalyzer;
import it.unibas.ids.analyzer.AdvancedAnalyzer;
import it.unibas.ids.aspect.SuspiciousEventLoggingAspect;
import it.unibas.ids.collector.EventCollector;
import org.aspectj.lang.Aspects;

public class IdsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IEventAnalyzer.class).to(AdvancedAnalyzer.class).in(Scopes.SINGLETON);
        bind(IdsProperties.class).in(Singleton.class);
        bind(AnalysisContext.class).in(Singleton.class);
        bind(IEventSubscriber.class).to(EventCollector.class).in(Scopes.SINGLETON);

        requestInjection(Aspects.aspectOf(SuspiciousEventLoggingAspect.class));
    }

}
