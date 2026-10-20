package org.basex;

import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.out.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * Tests the command-line arguments of the standalone starter class.
 *
* @author BaseX Team, BSD License
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

  /**
   * Terminates partial output with a newline if a query fails.
   */
  @Test public void partialOutput() {
    try(ArrayOutput ao = new ArrayOutput()) {
      System.setOut(new PrintStream(ao));
      assertThrows(BaseXException.class, () -> new BaseX("-q1, error()"));
      assertEquals("1" + Prop.NL, ao.toString());
    } finally {
      System.setOut(OUT);
    }
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
