package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Runs maintenance operations of the Database Module while a database is read by other clients.
 * Readers must never observe a database in an intermediate state.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbModuleConcurrencyTest extends SandboxTest {
  /** Number of items in the test database. */
  private static final int ITEMS = 1000;
  /** Number of runs of the maintenance operation. */
  private static final int RUNS = 25;
  /** Number of readers. */
  private static final int READERS = 10;

  /**
   * Creates the test database.
   */
  @BeforeEach public void init() {
    final StringBuilder input = new StringBuilder("<root>");
    for(int i = 0; i < ITEMS; i++) input.append("<item>").append(i).append("</item>");
    execute(new CreateDB(NAME, input.append("</root>").toString()));
    // maintenance operations require an unpinned database
    execute(new Close());
  }

  /**
   * Drops the test database.
   */
  @AfterEach public void finish() {
    execute(new DropDB(NAME));
  }

  /**
   * Optimizes a database while it is read.
   * @throws Exception exception
   */
  @Test @Timeout(120) public void optimize() throws Exception {
    run(_DB_OPTIMIZE.args(NAME));
  }

  /**
   * Rebuilds a database from scratch while it is read.
   * @throws Exception exception
   */
  @Test @Timeout(120) public void optimizeAll() throws Exception {
    run(_DB_OPTIMIZE.args(NAME, true));
  }

  /**
   * Backs a database up and discards the backup while it is read.
   * @throws Exception exception
   */
  @Test @Timeout(120) public void backup() throws Exception {
    run(_DB_CREATE_BACKUP.args(NAME), _DB_DROP_BACKUP.args(NAME));
  }

  /**
   * Restores a database while it is read.
   * @throws Exception exception
   */
  @Test @Timeout(120) public void restore() throws Exception {
    query(_DB_CREATE_BACKUP.args(NAME));
    try {
      run(_DB_RESTORE.args(NAME));
    } finally {
      query(_DB_DROP_BACKUP.args(NAME));
    }
  }

  /**
   * Copies a database while it is read.
   * @throws Exception exception
   */
  @Test @Timeout(120) public void copy() throws Exception {
    final String copy = NAME + "copy";
    try {
      run(_DB_COPY.args(NAME, copy), _DB_DROP.args(copy));
    } finally {
      query("if(" + _DB_EXISTS.args(copy) + ") { " + _DB_DROP.args(copy) + " }");
    }
  }

  /**
   * Runs maintenance queries in a loop while other clients read the database.
   * @param maintenance maintenance queries, run one after another
   * @throws Exception exception
   */
  private static void run(final String... maintenance) throws Exception {
    final ArrayList<Callable<?>> tasks = new ArrayList<>(READERS + 1);
    tasks.add(() -> {
      for(int r = 0; r < RUNS; r++) {
        for(final String mt : maintenance) query(mt);
      }
      return null;
    });
    for(int c = 0; c < READERS; c++) {
      tasks.add(() -> {
        for(int r = 0; r < RUNS; r++) {
          // the database is never modified, so a reader always sees all of its items
          query("count(" + _DB_GET.args(NAME) + "//item)", ITEMS);
        }
        return null;
      });
    }
    parallel(tasks);
    assertEquals(Integer.toString(ITEMS), query("count(" + _DB_GET.args(NAME) + "//item)"));
  }
}
