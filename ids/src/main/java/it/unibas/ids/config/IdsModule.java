package it.unibas.ids.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import it.unibas.common.interfaces.IEventSubscriber;
import it.unibas.ids.alert.AlertManager;
import it.unibas.ids.analyzer.AdvancedAnalyzer;
import it.unibas.ids.analyzer.AnalysisContext;
import it.unibas.ids.analyzer.IEventAnalyzer;
import it.unibas.ids.analyzer.SimpleAnalyzer;
import it.unibas.ids.aspect.SuspiciousEventLoggingAspect;
import it.unibas.ids.collector.EventCollector;
import it.unibas.ids.vista.CloneConfigDialogFactory;
import it.unibas.ids.controllo.Controllo;
import it.unibas.ids.controllo.IControllo;
import it.unibas.ids.vista.CloneConfigDialog;
import it.unibas.ids.vista.ICloneConfigDialog;
import it.unibas.ids.vista.IVista;
import it.unibas.ids.vista.Vista;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.Aspects;

@Slf4j
public class IdsModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IEventAnalyzer.class).to(AdvancedAnalyzer.class).in(Scopes.SINGLETON);
        bind(IVista.class).to(Vista.class).in(Scopes.SINGLETON);
        bind(IControllo.class).to(Controllo.class).in(Scopes.SINGLETON);
        bind(IdsProperties.class).in(Singleton.class);
        bind(AnalysisContext.class).in(Singleton.class);
        bind(IEventSubscriber.class).to(EventCollector.class).in(Scopes.SINGLETON);
        bind(AlertManager.class).in(Scopes.SINGLETON);
//        install(new FactoryModuleBuilder()
//                .implement(ICloneConfigDialog.class, CloneConfigDialog.class)
//                .build(CloneConfigDialogFactory.class));

        requestInjection(Aspects.aspectOf(SuspiciousEventLoggingAspect.class));
    }

    @Provides
    @Named("advancedAnalyzer")
    @Singleton
    public AdvancedAnalyzer provideDefaultAdvancedAnalyzer(AlertManager alertManager) {
        AdvancedAnalyzer analyzer = new AdvancedAnalyzer(alertManager);
        log.info("Created default AdvancedAnalyzer with rules: {}", analyzer.getRules());
        return analyzer;
    }

    @Provides
    @Named("simpleAnalyzer")
    @Singleton
    public SimpleAnalyzer provideDefaultSimpleAnalyzer(AlertManager alertManager) {
        SimpleAnalyzer analyzer = new SimpleAnalyzer(alertManager);
        log.info("Created default SimpleAnalyzer with rules: {}", analyzer.getRules());
        return analyzer;
    }
}
