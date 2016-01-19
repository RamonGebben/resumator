package io.sytac.resumator.employee;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sytac.resumator.command.Command;
import io.sytac.resumator.command.CommandHeader;
import io.sytac.resumator.model.Event;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Describes the intent to create a new Employee
 *
 * @author Carlo Sciolla
 * @since 0.1
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewEmployeeCommand implements Command<CommandHeader, NewEmployeeCommandPayload> {

    public static final String EVENT_TYPE = "newEmployee";
    private final CommandHeader header;
    private final NewEmployeeCommandPayload payload;

    public NewEmployeeCommand(final NewEmployeeCommandPayload payload, final String domain, final String timestamp) {
        final Date date = Optional.ofNullable(timestamp)
                .map(Long::decode)
                .map(Date::new)
                .orElse(new Date());

        this.payload = payload;
        this.header = new CommandHeader.Builder().setDomain(domain).setTimestamp(date.getTime()).build();
    }

    public NewEmployeeCommand(final NewEmployeeCommandPayload payload, final CommandHeader header) {
        this.payload = payload;
        this.header = header;
    }

    @SuppressWarnings("unused")
    @JsonCreator
        /* package */ NewEmployeeCommand(@JsonProperty("header") final CommandHeader header,
                                         @JsonProperty("payload") final NewEmployeeCommandPayload payload) {
        this.header = header;
        this.payload = payload;
    }

    @Override
    public CommandHeader getHeader() {
        return header;
    }

    @Override
    public NewEmployeeCommandPayload getPayload() {
        return payload;
    }

    @Override
    public String getType() {
        return EVENT_TYPE;
    }

    @Override
    public Event asEvent(final ObjectMapper json) {
        try {
            final String asJson = json.writeValueAsString(this);
            final String eventId = header.getId().orElse(UUID.randomUUID().toString());
            final Long insertOrder = header.getInsertOrder().orElse(Event.ORDER_UNSET);
            return new Event(eventId, insertOrder, asJson, new Timestamp(header.getTimestamp()), getType());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
