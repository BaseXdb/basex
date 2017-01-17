package org.basex.core.locks;

import static org.basex.query.func.Function.*;

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
   */
  @Test
  public void runQueries() {
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

      // start sleeping query
      String id = "";
      while(id.isEmpty()) {
        id = execute(new XQuery(_JOBS_LIST.args() + "[. != " + _JOBS_CURRENT.args() + "]"));
      }

      // local locking: add document in parallel
      execute(new XQuery(_DB_ADD.args(NAME, "<a/>", "a.xml")));

      // global locking: add document in parallel; see GH-1400
      execute(new XQuery("let $db := <a>" + NAME + "</a> return " +
        _DB_ADD.args("$db", "<a/>", "a.xml")));

      // stop sleeping process, wait for its completion
      execute(new XQuery(_JOBS_STOP.args(id)));
      execute(new XQuery(_JOBS_WAIT.args(id)));

    } finally {
      execute(new DropDB(NAME));
    }
  }
}
