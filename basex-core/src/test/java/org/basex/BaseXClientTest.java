package org.basex;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.io.out.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;

/**
 * Tests the command-line arguments of the client starter class.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BaseXClientTest extends BaseXTest {
  @Override
  protected String run(final String... args) throws IOException {
    return run(args, new String[0]);
  }

  /**
   * Test client with different port.
   * @throws IOException I/O exception
   */
  @Test public void port() throws IOException {
    equals("1", new String[] { "-p9898", "-q1" }, new String[] { "-p9898" });
  }

  /**
   * Test client with invalid port argument.
   */
  @Test public void portErr() {
    assertThrows(BaseXException.class, () -> run("-px"));
  }

  /**
   * Test client with invalid port number.
   */
  @Test public void portErr2() {
    assertThrows(BaseXException.class, () -> run("-px0"));
  }

  /**
   * Test client with different user.
   * @throws IOException I/O exception
   */
  @Test public void user() throws IOException {
    run("-cexit", "-cdrop user " + NAME);
    equals("5", new String[] { "-U" + NAME, "-P" + NAME, "-q5" },
        new String[] { "-ccreate user " + NAME + ' ' + NAME });
    run("-cexit", "-cdrop user " + NAME);
  }

  /**
   * Runs a request and compares the result with the expected result.
   * @param exp expected result
   * @param args command-line arguments
   * @param sargs server arguments
   * @throws IOException I/O exception
   */
  private static void equals(final String exp, final String[] args, final String[] sargs)
      throws IOException {
    assertEquals(exp, run(args, sargs));
  }

  /**
   * Runs a request with the specified arguments and server arguments.
   * @param args command-line arguments
   * @param sargs server arguments
   * @return result
   * @throws IOException I/O exception
   */
  private static String run(final String[] args, final String[] sargs) throws IOException {
    final BaseXServer server = createServer(sargs);
    final StringList sl = new StringList(
        "-p" + DB_PORT, "-U" + UserText.ADMIN, "-P" + UserText.ADMIN).add(args);
    try(ArrayOutput ao = new ArrayOutput()) {
      System.setOut(new PrintStream(ao));
      new BaseXClient(sl.finish());
      return ao.toString();
    } finally {
      stopServer(server);
    }
  }
}
