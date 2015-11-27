package io.sytac.resumator.model;

import java.util.Date;

/**
 * Models events flowing through the system
 *
 * @author Carlo Sciolla
 * @since 0.1
 */
public class Event {

    final String id;
    final String streamId;
    final Long insertOrder;
    final Long streamOrder;
    final byte[] payload;
    final Date createdAt;
    final String eventType;

    public Event(String id, String streamId, Long insertOrder, Long streamOrder, byte[] payload, Date createdAt, String eventType) {
        this.id = id;
        this.streamId = streamId;
        this.insertOrder = insertOrder;
        this.streamOrder = streamOrder;
        this.payload = payload;
        this.createdAt = createdAt;
        this.eventType = eventType;
    }
}
