package org.basex.test;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.*;

import org.basex.*;
import org.basex.io.out.*;
import org.junit.*;

/**
 * Tests the command-line arguments of the standalone starter class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BaseXStandaloneTest extends BaseXTest {
  /**
   * Write back updates.
   * @throws IOException I/O exception
   */
  @Test
  public void writeBack() throws IOException {
    INPUT.write(token("<a>X</a>"));
    run("-i", INPUT.toString(), "-u", "-q", "delete node //text()");
    assertEquals("<a/>", string(INPUT.read()));
  }

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
