package it.unibas.publisher;

import it.unibas.model.Event;

public interface EventSubscriber {

        void onEvent(Event event);

        default String getSubscriberName() {
            return this.getClass().getSimpleName();
        }

        default boolean isInterestedIn(Event event) {
            return true;
        }
}
