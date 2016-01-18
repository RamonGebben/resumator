package io.sytac.resumator.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import io.sytac.resumator.command.CommandFactory;
import io.sytac.resumator.employee.Employee;
import io.sytac.resumator.employee.NewEmployeeCommand;
import io.sytac.resumator.employee.NewEmployeeCommandPayload;
import io.sytac.resumator.events.EventPublisher;
import io.sytac.resumator.model.Event;
import io.sytac.resumator.organization.InMemoryOrganizationRepository;
import io.sytac.resumator.organization.NewOrganizationCommand;
import io.sytac.resumator.organization.OrganizationRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class BootstrapTest {

    private Bootstrap bootstrap;
    private EventStore store;
    private ObjectMapper json;
    private OrganizationRepository orgs;

    @Mock
    private EventPublisher eventPublisherMock;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        json = new ObjectMapper();
        json.registerModule(new Jdk8Module());

        store = mock(EventStore.class);
        when(store.getAll()).thenReturn(Collections.emptyList());

        final Map<String, String> orgDetails = new HashMap<>();
        orgDetails.put("name", "ACME inc.");
        orgDetails.put("domain", "acme.biz");

        when(eventPublisherMock.publish(any(NewOrganizationCommand.class))).thenReturn(mock(Event.class));
        orgs = new InMemoryOrganizationRepository(eventPublisherMock);
        orgs.register(new CommandFactory().newOrganizationCommand(orgDetails));

        bootstrap = new Bootstrap(store, orgs, json);
    }

    @Test
    public void canReplayZeroEvents() {
        bootstrap.start(result -> assertTrue("The replay of 0 events failed", result.failures.isEmpty()));
        verify(store, times(1)).setReadOnly(false);
        verify(store, times(1)).setReadOnly(true);
    }

    @Test
    public void canReplayNewEmployeeEvent() {
        when(store.getAll()).thenReturn(Collections.singletonList(newEmployeeEvent()));
        bootstrap.start(result -> assertTrue("The replay of creating one employee failed", result.successfullyReplayed.get() == 1));
        verify(store, times(1)).setReadOnly(false);
        verify(store, times(1)).setReadOnly(true);
        Optional<Employee> employee = orgs.fromDomain("acme.biz").get().findEmployeeByName("name", "surname");
        assertTrue("The employee was not stored in the organization after replaying the creation event", employee.isPresent());
    }

    private Event newEmployeeEvent() {
        final NewEmployeeCommandPayload employeeCommandPayload = new NewEmployeeCommandPayload("acme.biz", "title", "name", "surname", "email",
                "phonenumber", "github", "linkedin", "1984-04-22T00: 00: 00.000Z", "ITALIAN", "", null, null, null, null);
        final NewEmployeeCommand command = new NewEmployeeCommand(employeeCommandPayload, Long.toString(new Date().getTime()));
        return command.asEvent(json);
    }

}