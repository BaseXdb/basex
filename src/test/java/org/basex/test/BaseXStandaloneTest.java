package org.basex.test;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintStream;

import org.basex.BaseX;
import org.basex.io.out.ArrayOutput;
import org.junit.Test;

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
    IN.write(token("<a>X</a>"));
    run("-i", IN.toString(), "-u", "-q", "delete node //text()");
    assertEquals("<a/>", string(IN.read()));
  }

  @Override
  protected String run(final String... args) throws IOException {
    System.setErr(NULL);
    final ArrayOutput ao = new ArrayOutput();
    System.setOut(new PrintStream(ao));
    new BaseX(args);
    return ao.toString();
  }
}
