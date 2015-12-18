package io.sytac.resumator.events;

import io.sytac.resumator.command.Command;
import io.sytac.resumator.model.Event;

import java.util.function.Consumer;

/**
 * Publish domain events within the system
 *
 * @author Carlo Sciolla
 * @since 0.1
 */
public interface EventPublisher {

    /**
     * A new employee was added to the system
     *
     * @param command The command describing the added employee
     * @return The {@link Event} that was broadcasted
     */
    <T extends Command> Event publish(T command);

    /**
     * Register a new event listener for a given type of events
     *
     * @param consumer The listener that will receive new events
     */
    <T extends Event> void subscribe(Consumer<T> consumer, Class<T> type);
}
