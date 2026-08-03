package org.basex.server;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.api.client.*;
import org.junit.jupiter.api.*;

/**
 * This class performs a client/server stress test with concurrent operations on two databases:
 * one that is only read by updating queries, and one that is updated.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
@Timeout(600)
public final class ServerLockStressTest extends SandboxTest {
  /** Database that is only read. */
  private static final String SOURCE = NAME + "source";
  /** Database that is updated. */
  private static final String TARGET = NAME + "target";
  /** Number of items in the source database. */
  private static final int ITEMS = 10;
  /** Query for counting the items of the source database. */
  private static final String COUNT = "XQUERY count(db:get('" + SOURCE + "')//item)";

  /** Server reference. */
  private BaseXServer server;

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test public void clients20runs20() throws Exception {
    run(20, 20);
  }

  /**
   * Runs the test.
   * @throws Exception exception
   */
  @Test public void clients100runs20() throws Exception {
    run(100, 20);
  }

  /**
   * Runs the stress test.
   * @param clients number of clients
   * @param runs number of runs per client
   * @throws Exception exception
   */
  private void run(final int clients, final int runs) throws Exception {
    server = createServer();
    try {
      try(ClientSession cs = createClient()) {
        final StringBuilder input = new StringBuilder("<source>");
        for(int i = 0; i < ITEMS; i++) input.append("<item>").append(i).append("</item>");
        cs.execute("CREATE DB " + SOURCE + ' ' + input.append("</source>"));
        cs.execute("CREATE DB " + TARGET);

        // every third client reads the source, copies it to the target, or updates the target
        final ArrayList<Callable<?>> tasks = new ArrayList<>(clients);
        for(int c = 0; c < clients; c++) {
          final int type = c % 3;
          final String query = switch(type) {
            // read lock on source
            case 0 -> COUNT;
            // read lock on source, write lock on target
            case 1 -> "XQUERY for $item at $p in db:get('" + SOURCE + "')//item " +
              "return db:put('" + TARGET + "', $item, 'item' || $p || '.xml')";
            // write lock on target; targets of node updates are resolved at runtime
            default -> "XQUERY for $item in db:get('" + TARGET + "')//item " +
              "return replace value of node $item with 'x'";
          };
          tasks.add(() -> {
            try(ClientSession session = createClient()) {
              session.execute("SET AUTOFLUSH false");
              for(int r = 0; r < runs; r++) {
                final String result = session.execute(query);
                // readers must never observe a partially updated source database
                if(type == 0) assertEquals(ITEMS, Integer.parseInt(result.trim()));
              }
            }
            return null;
          });
        }
        parallel(tasks);

        // source database must not have been modified
        assertEquals(ITEMS, Integer.parseInt(cs.execute(COUNT).trim()));
        cs.execute("DROP DB " + SOURCE);
        cs.execute("DROP DB " + TARGET);
      }
    } finally {
      stopServer(server);
    }
  }
}
