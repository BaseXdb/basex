package org.basex.test;

import static org.junit.Assert.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;

/**
 * Tests the command-line arguments of the starter classes.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class MainTest extends SandboxTest {
  /** Input file. */
  static final IOFile INPUT = new IOFile(Prop.TMP + NAME + ".in");

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
  final void equals(final String exp, final String... args) throws IOException {
    assertEquals(exp, run(args));
  }

  /**
   * Runs a request and checks if the expected string is contained in the
   * result.
   * @param exp expected result
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  final void contains(final String exp, final String... args) throws IOException {
    final String result = run(args);
    if(!result.contains(exp)) fail('\'' + exp + "' not contained in '" + result + "'.");
  }
}
