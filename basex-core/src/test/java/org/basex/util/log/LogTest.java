package org.basex.util.log;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.time.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.server.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests the writing of log entries.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class LogTest extends SandboxTest {
  /** Log directory. */
  private static final IOFile DIR = new IOFile(Prop.TEMPDIR, NAME + "-log");

  /** Removes the log directory. */
  @AfterEach public void delete() {
    DIR.delete();
  }

  /**
   * Excluded requests: the concluding entries of other clients and later requests are kept.
   * @throws IOException I/O exception
   */
  @Test public void exclude() throws IOException {
    final Log log = log("secret", "exclude");
    // two excluded requests, concluded in reverse order
    write(log, LogType.REQUEST, "secret", "A");
    write(log, LogType.REQUEST, "secret", "B");
    write(log, 200, "", "B");
    write(log, LogType.OK, "", "A");
    // an excluded concluding entry does not exclude the next request
    write(log, LogType.OK, "secret", "C");
    write(log, LogType.REQUEST, "kept", "C");
    write(log, LogType.OK, "", "C");
    log.close();

    final String[] lines = lines(log);
    assertEquals(2, lines.length);
    assertTrue(lines[0].contains("\tC\tuser\tREQUEST\tkept\t"));
    assertTrue(lines[1].contains("\tC\tuser\tOK\t"));
  }

  /**
   * Whitespaces in user names are normalized.
   * @throws IOException I/O exception
   */
  @Test public void user() throws IOException {
    final Log log = log("", "user");
    log.write(LogType.INFO, "x", null, "A", context("a\tb\nc"));
    log.close();

    final String[] lines = lines(log);
    assertEquals(1, lines.length);
    assertEquals(7, lines[0].split("\t", -1).length);
    assertTrue(lines[0].contains("\tA\ta b c\tINFO\tx\t"));
  }

  /**
   * Creates a logger that writes to the log directory.
   * @param exclude pattern of excluded entries
   * @param name name of the database directory
   * @return logger
   */
  private static Log log(final String exclude, final String name) {
    final StaticOptions sopts = new StaticOptions(false);
    sopts.set(StaticOptions.DBPATH, new IOFile(DIR, name).path());
    sopts.set(StaticOptions.LOG, "data");
    sopts.set(StaticOptions.LOGEXCLUDE, exclude);
    return new Log(sopts);
  }

  /**
   * Writes a log entry.
   * @param log logger
   * @param type type
   * @param info info string
   * @param address address
   */
  private static void write(final Log log, final Object type, final String info,
      final String address) {
    log.write(type, info, null, address, context("user"));
  }

  /**
   * Returns a context with the specified client name.
   * @param name client name
   * @return context
   */
  private static Context context(final String name) {
    return new Context(context, new ClientInfo() {
      @Override
      public String clientAddress() {
        return null;
      }
      @Override
      public String clientName() {
        return name;
      }
    });
  }

  /**
   * Returns the lines of today's log file.
   * @param log logger
   * @return lines
   * @throws IOException I/O exception
   */
  private static String[] lines(final Log log) throws IOException {
    return log.file(DateTime.DATE.format(LocalDateTime.now())).read().finish();
  }
}
