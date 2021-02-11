package org.basex;

import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.io.out.*;
import org.junit.jupiter.api.*;

/**
 * Tests the command-line arguments of the standalone starter class.
 *
* @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class BaseXStandaloneTest extends BaseXTest {
  /**
   * Writes back updates.
   * @throws IOException I/O exception
   */
  @Test public void writeBack() throws IOException {
    INPUT.write("<a>X</a>");
    run("-i", INPUT.toString(), "-u", "-q", "delete node //text()");
    assertEquals("<a/>", string(INPUT.read()));
  }

  @Override
  protected String run(final String... args) throws IOException {
    try(ArrayOutput ao = new ArrayOutput()) {
      System.setOut(new PrintStream(ao));
      new BaseX(args);
      return ao.toString();
    } finally {
      System.setOut(OUT);
    }
  }
}
