package org.basex.core.locks;

import static org.basex.query.func.Function.*;

import java.io.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.junit.Test;

/**
 * This class checks if a reading process blocks an updating process.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class ReadWriteTest extends SandboxTest {
  /**
   * Test.
   * @throws IOException I/O exception
   */
  @Test
  public void runQueries() throws IOException {
    execute(new CreateDB(NAME));
    try {
      new Thread() {
        @Override
        public void run() {
          try {
            new XQuery(_PROF_SLEEP.args(1000000)).execute(context);
          } catch(final Exception ignored) { }
        }
      }.start();

      // wait until job has been started
      while(query(_JOBS_LIST.args() + "[. != " + _JOBS_CURRENT.args() + "]").isEmpty())
        Thread.yield();

      // local locking: add document in parallel
      new XQuery(_DB_ADD.args(NAME, "<a/>", "a.xml")).execute(context);

      /* global locking: add document in parallel; see GH-1400
      new XQuery("let $db := <a>" + NAME + "</a> return " + _DB_ADD.args("$a", "<a/>", "a.xml")).
        execute(context);
      */

      // kill sleeping process
      query(_JOBS_STOP.args(_JOBS_LIST.args() + "[. != " + _JOBS_CURRENT.args() + "]"));

    } finally {
      execute(new DropDB(NAME));
    }
  }
}
