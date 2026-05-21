package org.basex.server;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests the execution of parallel commands.
 *
 * @author BaseX Team, BSD License
 * @author Andreas Weiler
 */
public final class ConcurrencyTest extends SandboxTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/factbook.zip";
  /** Test queries. */
  private static final String[] QUERIES = {
    '(' + _DB_GET.args(NAME) + "//province)[position() < 10] ! (insert node <test/> into .)",
    "(1 to 100000)[. = 0]"
  };

  /** Server reference. */
  private BaseXServer server;
  /** Socket reference. */
  private Session sess;

  /**
   * Starts the server.
   * @throws IOException exception
   */
  @BeforeEach public void start() throws IOException {
    server = createServer();
    sess = createClient();
    sess.execute(new CreateDB(NAME, FILE));
  }

  /**
   * Stops the server.
   * @throws Exception exception
   */
  @AfterEach public void stop() throws Exception {
    try(Session s = sess) {
      s.execute(new DropDB(NAME));
    } finally {
      stopServer(server);
    }
  }

  /**
   * Runs read and update queries from many parallel clients.
   * @throws Exception exception
   */
  @Test @Timeout(120) public void runQueries() throws Exception {
    final int cl = 50;
    final ArrayList<Callable<?>> tasks = new ArrayList<>(cl);
    for(int c = 0; c < cl; c++) {
      final String query = QUERIES[c % QUERIES.length];
      tasks.add(() -> {
        try(ClientSession session = createClient()) {
          session.query(query).execute();
        }
        return null;
      });
    }
    parallel(tasks);
  }

  /**
   * Runs parallel create and drop commands on the same database and verifies that it is
   * left in a consistent, usable state.
   * @throws Exception exception
   */
  @Test @Timeout(120) public void runCommands() throws Exception {
    final int tl = 100;
    final ArrayList<Callable<?>> tasks = new ArrayList<>(tl);
    for(int t = 0; t < tl; t++) {
      final Command cmd = t % 2 == 0 ? new CreateDB(NAME) : new DropDB(NAME);
      tasks.add(() -> {
        cmd.execute(context);
        return null;
      });
    }
    parallel(tasks);

    // the database must be left in a consistent, usable state
    new CreateDB(NAME).execute(context);
    assertEquals("true", new XQuery(_DB_EXISTS.args(NAME)).execute(context));
  }
}
