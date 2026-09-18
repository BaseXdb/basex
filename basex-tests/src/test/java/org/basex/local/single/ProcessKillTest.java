package org.basex.local.single;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.io.random.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Kills a process that updates a database with incremental index updates, and checks that the
 * reopened database is consistent: if the update marker remains, the database must be refused;
 * otherwise, the database must pass the inspection, and all node IDs and index entries must match
 * the documents that are present.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProcessKillTest extends SandboxTest {
  /** Number of kill rounds. */
  private static final int ROUNDS = 10;
  /** Number of update commands per round (more than the process can run until it is killed). */
  private static final int COMMANDS = 10000;

  /** Creates the test database. */
  @BeforeEach public void init() {
    create();
  }

  /** Drops the test database. */
  @AfterEach public void finish() {
    execute(new DropDB(NAME));
    set(MainOptions.UPDINDEX, true);
    set(MainOptions.TOKENINDEX, false);
    set(MainOptions.FTINDEX, false);
  }

  /**
   * Kills the updating process several times.
   * @throws Exception exception
   */
  @Test @Timeout(600) public void kill() throws Exception {
    final IOFile script = new IOFile(sandbox(), "kill.bxs");
    int refused = 0, verified = 0;
    for(int round = 0; round < ROUNDS; round++) {
      writeScript(script, round);
      final Process process = start(script);
      Thread.sleep(3000 + round % 5 * 1500L);
      assertTrue(process.isAlive(), "process has finished before it was killed");
      process.destroyForcibly();
      assertTrue(process.waitFor(30, TimeUnit.SECONDS));
      // the file lock of the killed process may be released with a delay
      for(int w = 0; w < 100 && TableDiskAccess.locked(NAME, context); w++) Thread.sleep(100);

      if(new IOFile(context.soptions.dbPath(NAME), "upd" + IO.BASEXSUFFIX).exists()) {
        // killed during a commit: the database must be refused
        assertThrows(BaseXException.class, () -> new Open(NAME).execute(context));
        refused++;
        DropDB.drop(NAME, context.soptions);
        create();
      } else {
        verify();
        verified++;
      }
    }
    Util.errln("% rounds verified, % refused", verified, refused);
    assertTrue(verified > 0);
  }

  /**
   * Checks the consistency of the test database.
   */
  private static void verify() {
    final String db = _DB_GET.args(NAME);
    query(_DB_INSPECT.args(NAME) + "?issues ! (?check || ': ' || ?count)", "");
    // node IDs resolve to their nodes
    query("every $n in " + db + "//node() satisfies " +
      _DB_GET_ID.args(NAME, " " + _DB_NODE_ID.args(" $n")) + " is $n", true);
    // full-text index entries match the documents that are present
    query("count(" + _FT_SEARCH.args(NAME, "common") + ")",
      query("count(" + db + "//text()[contains-token(., 'common')])"));
    query("count(" + _FT_SEARCH.args(NAME, "w3") + ")",
      query("count(" + db + "//text()[contains-token(., 'w3')])"));
    execute(new Close());
  }

  /**
   * Creates the test database with more nodes than the segment threshold.
   */
  private static void create() {
    set(MainOptions.UPDINDEX, true);
    set(MainOptions.TOKENINDEX, true);
    set(MainOptions.FTINDEX, true);
    execute(new CreateDB(NAME));
    query(_DB_ADD.args(NAME, " <x>{ (1 to 40000) ! <a t='t{ . mod 97 } u'>v{ . mod 1000 }" +
      "</a> }</x>", "x.xml"));
    execute(new Close());
  }

  /**
   * Writes a command script with updates: replaced documents, deleted and changed base nodes.
   * @param script script file
   * @param round round
   * @throws IOException I/O exception
   */
  private static void writeScript(final IOFile script, final int round) throws IOException {
    // the database stays opened: changes of the ID-PRE map and the indexes are logged
    final StringBuilder sb = new StringBuilder("OPEN ").append(NAME).append('\n');
    for(int c = 0; c < COMMANDS; c++) {
      final int i = round * COMMANDS + c;
      switch(c % 4) {
        case 0, 1 -> sb.append("XQUERY db:put('" + NAME + "', <d t='t" + i +
          "'>{ (1 to 60) ! <e t='u{ . mod 7 }'>w{ (. + " + i + ") mod 13 } common</e> }</d>, 'd" +
          i % 40 + ".xml')");
        case 2 -> sb.append("XQUERY delete node (db:get('" + NAME + "', 'x.xml')/x/a)[" +
          (i % 500 + 1) + ']');
        default -> sb.append("XQUERY replace value of node (db:get('" + NAME +
          "', 'x.xml')/x/a)[" + (i % 700 + 1) + "] with 'r" + i % 11 + "'");
      }
      // even rounds: pause outside the commit, so that most kills leave a committed database;
      // odd rounds: most kills interrupt a commit; progress output (file kill.out in the sandbox)
      if(round % 2 == 0) sb.append(", prof:sleep(30)");
      sb.append(", message(").append(c).append(")\n");
    }
    script.write(Token.token(sb.toString()));
  }

  /**
   * Starts a BaseX process that runs a command script on the sandbox database directory.
   * @param script script file
   * @return process
   * @throws IOException I/O exception
   */
  private static Process start(final IOFile script) throws IOException {
    final ArrayList<String> args = new ArrayList<>();
    args.add(new File(System.getProperty("java.home"), "bin/java").getPath());
    args.add("-cp");
    args.add(System.getProperty("java.class.path"));
    args.add("-Dorg.basex.path=" + sandbox().path());
    args.add("-Dorg.basex.dbpath=" + context.soptions.get(StaticOptions.DBPATH));
    args.add(BaseX.class.getName());
    args.add(script.path());
    return new ProcessBuilder(args).redirectErrorStream(true).
      redirectOutput(new IOFile(sandbox(), "kill.out").file()).start();
  }
}
