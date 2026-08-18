package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

import org.basex.core.jobs.*;
import org.basex.http.*;
import org.basex.io.*;
import org.junit.jupiter.api.*;

/**
 * Tests for the jobs panel of the Activity view.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ActivityTest extends DBATest {
  /** ID of the registered service. */
  private static final String ID = "DORMANT";

  /**
   * Drops the test service after each test.
   * @throws IOException I/O exception
   */
  @AfterEach public void cleanup() throws IOException {
    services().unregister(ID);
  }

  /**
   * A service without a job is listed in the jobs table, and its definition is shown.
   * @throws IOException I/O exception
   */
  @Test public void dormantService() throws IOException {
    register();
    assertTrue(get("activity").contains(ID), "service missing from jobs table");
    final String page = get("activity?job=" + ID);
    assertTrue(page.contains("Service: " + ID), "service panel not served");
    assertTrue(page.contains(">Unregister<"), "registered service cannot be unregistered");
  }

  /**
   * A service is unregistered via the jobs panel.
   * @throws IOException I/O exception
   */
  @Test public void unregisterService() throws IOException {
    register();
    final String page = post("jobs/unregister", Map.of("id", ID));
    assertTrue(page.contains("was unregistered"), "service not unregistered");
    assertFalse(page.contains(ID + "<"), "service still listed");
  }

  /**
   * Registers a service, without scheduling a job for it.
   * @throws IOException I/O exception
   */
  private static void register() throws IOException {
    final JobOptions options = new JobOptions();
    options.set(JobOptions.ID, ID);
    options.set(JobOptions.INTERVAL, "P1D");
    services().register(new QueryJobSpec(options, new HashMap<>(), new IOContent("1"), null));
  }

  /**
   * Returns the services of the server.
   * @return services
   */
  private static Services services() {
    return HTTPContext.get().context().services;
  }
}
