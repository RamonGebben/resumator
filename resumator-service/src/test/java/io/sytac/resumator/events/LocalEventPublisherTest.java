package io.sytac.resumator.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sytac.resumator.Configuration;
import io.sytac.resumator.command.Command;
import io.sytac.resumator.command.CommandHeader;
import io.sytac.resumator.command.CommandPayload;
import io.sytac.resumator.employee.NewEmployeeCommand;
import io.sytac.resumator.employee.NewEmployeeCommandPayload;
import io.sytac.resumator.model.*;
import io.sytac.resumator.model.enums.Degree;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class LocalEventPublisherTest {

    private LocalEventPublisher events;

    @Before
    public void setUp() throws Exception {
        events = new LocalEventPublisher(new Configuration(), new ObjectMapper());
    }

    @Test
    public void canPublishEvents() {
        Event event = events.publish(getEmployeeCommand());
        assertEquals("Wrong event details", Event.ORDER_UNSET, event.getInsertOrder());
    }

    @Test
    public void canReceiveSpecificEvents() {
        AtomicInteger invoked = new AtomicInteger(0);
        final Consumer<Event> consumer = newEmployeeEvent -> invoked.incrementAndGet();
        final NewEmployeeCommand command = getEmployeeCommand();

        events.subscribe(consumer, NewEmployeeCommand.EVENT_TYPE);
        Event event = events.publish(command);
        assertEquals("Wrong event details", Event.ORDER_UNSET, event.getInsertOrder());
        assertEquals("Not invoked just one time!", 1, invoked.get());
        events.publish(command);
        assertEquals("Not invoked just one time!", 2, invoked.get());

        events.publish(new BogusCommand());
        assertEquals("Received undesirable events types!", 2, invoked.get());
    }
    private NewEmployeeCommand getEmployeeCommand() {
        final List<Education> education = Arrays.asList(new Education(Degree.MASTER_DEGREE, "Field", "University", true, 2000));
        final List<Course> courses = Arrays.asList(new Course("Course1", "Course 1", 1452863321441L));
        final List<String> technologies = Arrays.asList("Java", "Turbo Pascal");
        final List<String> methodologies = Arrays.asList("Scrum", "Exreme programming");
        final List<Experience> experience  = Arrays.asList(new Experience("CompanyName", "Title", "Location", "Short Desciption",
                technologies, methodologies, 546040800000L, 546090800000L));
        final List<Language> languages = Arrays.asList(new Language("English", "FULL_PROFESSIONAL"));

        final NewEmployeeCommandPayload payload = new NewEmployeeCommandPayload("ACME", "Title", "Foo", "Bar", "Email",
                "+31000999000", "Github", "Linkedin", "1984-04-22T00: 00: 00.000Z", "ITALY", "About ME", education, courses, experience, languages);

        return new NewEmployeeCommand(payload, Long.toString(new Date().getTime()));
    }

    private class BogusCommand implements Command {

        @Override
        public CommandHeader getHeader() {
            return null;
        }

        @Override
        public CommandPayload getPayload() {
            return null;
        }

        @Override
        public String getType() {
            return null;
        }

        @Override
        public Event asEvent(ObjectMapper json) {
            return new Event("id", Event.ORDER_UNSET, "bogus", new Timestamp(1L), "bogus");
        }
    }
}