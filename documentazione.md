# Documentazione Tecnica - Sistema IDS
1. *Dependency Injection*
    
   Implementazione con Google Guice
      Il sistema utilizza Google Guice per la gestione della Dependency Injection, implementata attraverso moduli di configurazione e binding espliciti.
      Struttura dei Moduli
      ```java
      public class IdsModule extends AbstractModule {
      @Override
      protected void configure() {
      bind(IEventAnalyzer.class).to(AdvancedAnalyzer.class).in(Scopes.SINGLETON);
      bind(IdsProperties.class).in(Singleton.class);
      bind(AnalysisContext.class).in(Singleton.class);
      bind(IEventSubscriber.class).to(EventCollector.class).in(Scopes.SINGLETON);

           // Integrazione AspectJ
           requestInjection(Aspects.aspectOf(SuspiciousEventLoggingAspect.class));
      }
      }
      Pattern Applicazione Singleton
      javapublic class Applicazione {
      private static Applicazione singleton = new Applicazione();
      private Injector injector;

      private Applicazione() {
      this.injector = Guice.createInjector(Stage.DEVELOPMENT, new IdsModule());
      }

      public <T> T getComponentInstance(Class<T> chiave) {
      return this.injector.getInstance(chiave);
      }
      }
      ```

2. Injection nei Componenti
   Le dipendenze vengono iniettate tramite:

Constructor Injection: @Inject sui costruttori
Setter Injection: @Inject sui metodi setter del Controllo
Field Injection: Per l'integrazione AspectJ

Esempi:
java@Inject
public AdvancedAnalyzer(AlertManager alertManager) {
super(alertManager);
}

@Inject
public void setAzioneEsci(ExitAction azione) {
this.addAction(azione);
}
2. Implementazione dell'Aspect
   Configurazione AspectJ
   Il sistema implementa l'Aspect-Oriented Programming utilizzando AspectJ per il logging automatico e la gestione delle notifiche.
   Aspect per Eventi Sospetti
   java@Aspect
   public class SuspiciousEventLoggingAspect {

   @Pointcut("execution(* it.unibas.ids.analyzer.IEventAnalyzer.analyzeEvent(..))")
   public void analyzeEventCall(Event event) {}

   @After(value = "analyzeEventCall(event)", argNames = "event")
   public void logSuspiciousEvent(Event event) {
   if (AnalysisContextHolder.isModalAnalysis()) return;

        // Logging automatico nella UI
        if (mainView != null) {
            String message = ViewUtil.formatLogMessage(event);
            mainView.appendEventLog(event, message);
        }
   }

   @AfterReturning("execution(* it.unibas.ids.alert.AlertManager.addAlert(..))")
   public void sendCriticalNotifications(JoinPoint joinPoint) {
   Alert alert = (Alert) joinPoint.getArgs()[0];
   if (alert.getThreatLevel() == ThreatLevel.CRITICAL) {
   emailService.sendCriticalAlert(alert);
   }
   }
   }
   Integrazione con Guice
   L'aspect viene integrato nel container DI tramite:
   javarequestInjection(Aspects.aspectOf(SuspiciousEventLoggingAspect.class));
   Context Holder per Controllo Esecuzione
   javapublic class AnalysisContextHolder {
   private static final ThreadLocal<Boolean> modalAnalysis = ThreadLocal.withInitial(() -> false);

   public static void setModalAnalysis(boolean value) {
   modalAnalysis.set(value);
   }
   }
3. Gestione Thread e Sincronizzazione
   Architettura Multi-Thread
   Il sistema implementa una gestione avanzata dei thread per garantire performance e sicurezza.
   Event Generator - Thread Pool
   javapublic abstract class AbstractEventGenerator {
   private ExecutorService executorService;
   private Semaphore eventSemaphore;

   @Override
   public void start() {
   eventSemaphore = new Semaphore(config.getMaxEvents());
   executorService = Executors.newFixedThreadPool(
   config.getNumberOfSources(),
   r -> {
   Thread t = new Thread(r);
   t.setDaemon(true);  // Thread daemon per shutdown pulito
   return t;
   }
   );
   }
   }
   TCP Publisher - Server Concorrente
   javapublic class TCPSocketPublisher {
   private final CopyOnWriteArrayList<Socket> clients = new CopyOnWriteArrayList<>();

   public void start() {
   new Thread(() -> {
   serverSocket = new ServerSocket(port);
   while (true) {
   Socket client = serverSocket.accept();
   clients.add(client);  // Thread-safe collection
   }
   }).start();
   }
   }
   Event Publisher - Producer-Consumer Pattern
   javapublic class EventPublisher {
   private final BlockingQueue<Event> eventQueue;
   private final AtomicBoolean isRunning = new AtomicBoolean(false);
   private final AtomicLong publishedEvents = new AtomicLong(0);

   private void publishingLoop() {
   while (isRunning.get() || !eventQueue.isEmpty()) {
   Event event = eventQueue.poll(publishIntervalMs, TimeUnit.MILLISECONDS);
   if (event != null) {
   notifySubscribers(event);
   publishedEvents.incrementAndGet();
   }
   }
   }
   }
   Sincronizzazione Strutture Dati

CopyOnWriteArrayList: Per collezioni thread-safe con letture frequenti
AtomicBoolean/AtomicLong: Per contatori e flag thread-safe
BlockingQueue: Per comunicazione producer-consumer
Semaphore: Per controllo accesso a risorse limitate

4. Gestione della Clonazione
   Pattern Prototype per Configurazioni
   Il sistema implementa la clonazione per permettere test e configurazioni multiple senza interferenze.
   Clonazione Eventi
   java@SuperBuilder
   @Data
   public abstract class Event implements Cloneable {
   @Override
   public Event clone() {
   try {
   Event cloned = (Event) super.clone();
   return cloned;
   } catch (CloneNotSupportedException e) {
   throw new RuntimeException("Clone not supported", e);
   }
   }
   }
   Clonazione Analyzer
   javapublic abstract class AAnalyzer implements IEventAnalyzer, Cloneable {
   @Override
   public IEventAnalyzer clone() {
   try {
   return (IEventAnalyzer) super.clone();
   } catch (CloneNotSupportedException e) {
   throw new RuntimeException("Clone non supportato", e);
   }
   }
   }
   Deep Clone delle Regole
   javapublic class AnalysisRules implements Cloneable {
   @Override
   public AnalysisRules clone() {
   try {
   AnalysisRules cloned = (AnalysisRules) super.clone();

            // Deep clone delle mappe
            cloned.eventThresholds = new HashMap<>(this.eventThresholds);
            cloned.groups = Set.copyOf(this.groups);
            cloned.levels = Set.copyOf(this.levels);
            
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone AnalysisRules", e);
        }
   }
   }
   Context Isolation per Testing
   javaprivate void runCloneAnalysis() {
   IEventAnalyzer analyzer = advancedAnalyzer.clone();
   analyzer.updateRules(rules);

   // Isolamento del contesto per evitare interferenze
   AnalysisContextHolder.setModalAnalysis(true);
   try {
   for (Event event : events) {
   analyzer.analyzeEvent(event.clone());
   }
   } finally {
   AnalysisContextHolder.setModalAnalysis(false);
   }
   }