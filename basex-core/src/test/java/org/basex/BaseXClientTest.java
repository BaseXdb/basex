package org.basex;

import static org.junit.Assert.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.out.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.junit.*;

/**
 * Tests the command-line arguments of the client starter class.
 *
 * @author BaseX Team 2005-13, BSD License
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
  @Test
  public void port() throws IOException {
    equals("1", new String[] { "-p9898", "-q1" }, new String[] { "-p9898" });
  }

  /**
   * Test client with invalid port argument.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public void portErr() throws IOException {
    run("-px");
  }

  /**
   * Test client with invalid port number.
   * @throws IOException I/O exception
   */
  @Test(expected = BaseXException.class)
  public void portErr2() throws IOException {
    run("-px0");
  }

  /**
   * Test client with different user.
   * @throws IOException I/O exception
   */
  @Test
  public void user() throws IOException {
    run("-cexit", "-cdrop user " + NAME);
    equals("5", new String[] { "-U" + NAME, "-P" + NAME, "-q5" },
        new String[] {  "-ccreate user " + NAME + ' ' + Token.md5(NAME) });
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
    final ArrayOutput ao = new ArrayOutput();
    System.setOut(new PrintStream(ao));
    System.setErr(NULL);

    final StringList sl = new StringList();
    sl.add("-p9999").add("-U" + Text.ADMIN).add("-P" + Text.ADMIN).add(args);
    try {
      new BaseXClient(sl.toArray());
      return ao.toString();
    } finally {
      System.setErr(ERR);
      stopServer(server);
    }
  }
}
