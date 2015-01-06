package org.basex.core;

import static org.junit.Assert.*;

import java.io.*;

import org.basex.*;
import org.basex.io.out.*;
import org.junit.*;

/**
 * This class tests the completeness and correctness of the language files.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class LangTest extends SandboxTest {
  /**
   * Tests files.
   */
  @Test
  public void test() {
    try {
      final ArrayOutput ao = new ArrayOutput();
      System.setErr(new PrintStream(ao));
      Lang.check();
      if(ao.size() != 0) fail(ao.toString());
    } finally {
      System.setErr(ERR);
    }
  }
}
