package org.basex.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintStream;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.IOFile;
import org.basex.io.out.NullOutput;
import org.basex.util.Util;
import org.junit.*;

/**
 * Tests the command-line arguments of the starter classes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class MainTest {
  /** Null output stream. */
  static final PrintStream NULL = new PrintStream(new NullOutput());
  /** Test database name. */
  static final String NAME = Util.name(MainTest.class);
  /** Input file. */
  static final IOFile IN = new IOFile(Prop.TMP + NAME + ".in");

  /** Drop test database. */
  @AfterClass
  public static void cleanUp() {
    final Context ctx = new Context();
    try {
      new DropDB(NAME).execute(ctx);
    } catch(final BaseXException ex) {
      Util.debug(ex);
    } finally {
      ctx.close();
    }
  }

  /**
   * Runs a request with the specified arguments.
   * @param args command-line arguments
   * @return result
   * @throws IOException I/O exception
   */
  protected abstract String run(final String... args) throws IOException;

  /**
   * Runs a request and compares the result with the expected result.
   * @param exp expected result
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  final void equals(final String exp, final String... args)
      throws IOException {
    assertEquals(exp, run(args));
  }

  /**
   * Runs a request and checks if the expected string is contained in the
   * result.
   * @param exp expected result
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  final void contains(final String exp, final String... args)
      throws IOException {

    final String result = run(args);
    if(!result.contains(exp)) {
      fail('\'' + exp + "' not contained in '" + result + "'.");
    }
  }
}
