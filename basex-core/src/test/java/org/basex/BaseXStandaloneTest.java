package org.basex;

import java.io.*;

import org.basex.io.out.*;

/**
 * Tests the command-line arguments of the standalone starter class.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class BaseXStandaloneTest extends BaseXTest {
  @Override
  protected String run(final String... args) throws IOException {
    try {
      final ArrayOutput ao = new ArrayOutput();
      System.setOut(new PrintStream(ao));
      System.setErr(NULL);
      new BaseX(args);
      return ao.toString();
    } finally {
      System.setOut(OUT);
      System.setErr(ERR);
    }
  }
}
