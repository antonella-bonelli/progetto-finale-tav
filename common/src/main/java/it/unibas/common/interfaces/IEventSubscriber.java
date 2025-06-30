package it.unibas.common.interfaces;

import it.unibas.common.model.Event;

public interface IEventSubscriber {

    void onEvent(Event event);

    default String getSubscriberName() {
        return this.getClass().getSimpleName();
    }

}
