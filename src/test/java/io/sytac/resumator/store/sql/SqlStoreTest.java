package io.sytac.resumator.store.sql;

import io.sytac.resumator.AbstractResumatorTest;
import io.sytac.resumator.Configuration;
import io.sytac.resumator.model.Event;
import io.sytac.resumator.store.IllegalInsertOrderException;
import io.sytac.resumator.store.IllegalStreamOrderException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test SQL connections
 *
 * @author Carlo Sciolla
 * @since 0.1
 */
public class SqlStoreTest extends AbstractResumatorTest {

    private SqlStore store;
    private Configuration configuration;

    @Before
    public void setUp() throws Exception {
        configuration = new Configuration();
        store = new SqlStore(configuration);
        new SchemaManager(configuration, store).migrate();
    }

    @Test
    public void connectedToTheTestDB(){
        assertEquals(configuration.getProperty("resumator.db.user").get(), "sa");
    }

    @Test
    public void canStoreOneEvent() {
        Event event = createRandomEvent();
        store.put(event);
    }

    private Event createRandomEvent() {
        return new Event(UUID.randomUUID().toString(), "stream", 1l, 1l, "test".getBytes(), new Timestamp(0), "test");
    }

    private Event createRandomEvent(final Long insertSequence) {
        return new Event(UUID.randomUUID().toString(), "stream", insertSequence, 1l, "test".getBytes(), new Timestamp(0), "test");
    }

    private Event createRandomEvent(final Long insertSequence, final String streamId, final Long streamOrder) {
        return new Event(UUID.randomUUID().toString(), streamId, insertSequence, streamOrder, "test".getBytes(), new Timestamp(0), "test");
    }

    @Test
    public void canRetrieveOneEvent() {
        assertTrue("DB is not clean at the start of a test", store.getAll().size() == 0);
        Event event = createRandomEvent();
        store.put(event);
        Event retrieved = store.getAll().get(0);
        assertEquals("Retrieved event doesn't match with the stored event", event.getId(), retrieved.getId());
    }

    @Test(expected = IllegalInsertOrderException.class)
    public void insertSequenceMustBeUnique(){
        Event event1 = createRandomEvent(1l);
        Event event2 = createRandomEvent(1l);

        store.put(event1);
        store.put(event2);
    }

    @Test(expected = IllegalStreamOrderException.class)
    public void streamOrderMustBeUnique(){
        Event event1 = createRandomEvent(1l, "stream", 1l);
        Event event2 = createRandomEvent(2l, "stream", 1l);

        store.put(event1);
        store.put(event2);
    }

    @After
    public void cleanDB(){
        store.removeAll();
    }
}