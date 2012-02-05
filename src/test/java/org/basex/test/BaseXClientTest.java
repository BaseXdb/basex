package org.basex.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintStream;

import org.basex.BaseXClient;
import org.basex.BaseXServer;
import org.basex.core.BaseXException;
import org.basex.core.Text;
import org.basex.io.out.ArrayOutput;
import org.basex.util.Token;
import org.basex.util.list.StringList;
import org.junit.Test;

/**
 * Tests the command-line arguments of the client starter class.
 *
 * @author BaseX Team 2005-12, BSD License
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
  private static void equals(final String exp, final String[] args,
      final String[] sargs) throws IOException {
    assertEquals(exp, run(args, sargs));
  }

  /**
   * Runs a request with the specified arguments and server arguments.
   * @param args command-line arguments
   * @param sargs server arguments
   * @return result
   * @throws IOException I/O exception
   */
  private static String run(final String[] args, final String[] sargs)
      throws IOException {

    System.setOut(NULL);
    System.setErr(NULL);
    final StringList sl =
        new StringList().add("-p9999").add("-e9998").add(sargs);
    final BaseXServer bxs = new BaseXServer(sl.toArray());
    final ArrayOutput ao = new ArrayOutput();
    System.setOut(new PrintStream(ao));
    try {
      sl.reset();
      sl.add("-p9999").add("-U" + Text.ADMIN).add("-P" + Text.ADMIN).add(args);
      new BaseXClient(sl.toArray());
      return ao.toString();
    } finally {
      bxs.stop();
    }
  }
}
