package org.basex.local.single;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.*;
import org.junit.jupiter.api.Test;

/**
 * This test replaces texts in-place and checks that the database text file does not grow.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ReplaceInPlaceTest extends SandboxTest {
  /** Number of queries to be run. */
  private static final int NQUERIES = 10000;
  /** Original text: as long as the replacement values, so that updates are applied in-place. */
  private static final String TEXT = "0.000000000000000000";

  /** Runs the test. */
  @Test public void run() {
    set(MainOptions.TEXTINDEX, false);
    set(MainOptions.ATTRINDEX, false);
    set(MainOptions.AUTOFLUSH, false);

    // create test database
    execute(new CreateDB(NAME, "<X><A>" + TEXT + "</A><A>" + TEXT + "</A></X>"));
    execute(new Open(NAME));
    final IOFile file = context.data().meta.dbFile(DataText.DATATXT);
    final long size = file.length();

    // replace texts with random values of the same length
    final Random rnd = new Random();
    for(int i = 0; i < NQUERIES; i++) {
      final String value = String.format(Locale.ENGLISH, "%.18f", rnd.nextDouble());
      assertEquals(TEXT.length(), value.length());
      query("for $a in //A return replace value of node $a with '" + value + '\'');
    }
    execute(new Flush());

    // in-place replacements must not append new values to the text file
    assertEquals(size, file.length(), "Text file has grown.");

    // Drop database
    execute(new Close());
    execute(new DropDB(NAME));
  }
}
