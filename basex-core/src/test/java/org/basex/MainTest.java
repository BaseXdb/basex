package org.basex;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.io.*;
import org.basex.util.*;

/**
 * Tests the command-line arguments of the starter classes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class MainTest extends SandboxTest {
  /** Input file. */
  static final IOFile INPUT = new IOFile(Prop.TEMPDIR + NAME + ".in");

  /**
   * Runs a request with the specified arguments.
   * @param args command-line arguments
   * @return result
   * @throws IOException I/O exception
   */
  protected abstract String run(String... args) throws IOException;

  /**
   * Runs a request and compares the result with the expected result.
   * @param expected expected result
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  final void equals(final String expected, final String... args) throws IOException {
    assertEquals(expected, run(args));
  }

  /**
   * Runs a request and checks if the expected string is contained in the
   * result.
   * @param expected expected result
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  final void contains(final String expected, final String... args) throws IOException {
    final String result = run(args);
    if(!result.contains(expected)) fail('\'' + expected + "' not contained in '" + result + "'.");
  }
}
