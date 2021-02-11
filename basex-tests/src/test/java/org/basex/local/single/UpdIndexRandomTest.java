package org.basex.local.single;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * This test class performs random incremental updates with random documents.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UpdIndexRandomTest extends SandboxTest {
  /** Number of different documents. */
  private static final int DOCS = 20;
  /** Number of runs. */
  private static final int RUNS = 500;

  /**
   * Initializes the test.
   * @param mainmem main memory flag
   */
  public void init(final boolean mainmem) {
    set(MainOptions.MAINMEM, mainmem);
    set(MainOptions.UPDINDEX, true);
    set(MainOptions.ATTRINDEX, false);
    execute(new CreateDB(NAME));
  }

  /**
   * Incremental test.
   * @param mainmem main memory flag
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void insertInto(final boolean mainmem) {
    init(mainmem);

    final Random rnd = new Random(0);

    // create random words
    final int cap = 1000;
    final StringList words = new StringList(cap);
    for(int w = 0; w < cap; w++) {
      final int r = 1 + rnd.nextInt(10);
      final TokenBuilder tmp = new TokenBuilder(r);
      for(int i = 0; i < r; i++) tmp.add('A' + rnd.nextInt(26));
      words.add(tmp.toString());
    }

    for(int r = 0; r < RUNS; r++) {
      final String path = "doc" + rnd.nextInt(DOCS);
      // create random document
      final TokenBuilder doc = new TokenBuilder().add("<xml>");

      final int offset = rnd.nextInt(cap - DOCS);
      for(int i = 0; i < DOCS; i++) doc.add("<a>").add(words.get(offset + i)).add("</a>");
      doc.add("</xml>");
      execute(new Replace(path, doc.toString()));

      for(int d = 0; d < DOCS; d++) {
        final String word = words.get(offset + d);
        final String query = _DB_OPEN.args(NAME, path) + "//a[text() = '" + word + "']";
        final String expected = "<a>" + word + "</a>";
        final String result = query(query);
        if(!result.startsWith(expected)) {
          fail("\nExpected: " + expected +
               "\nResult: " + result +
               "\nRun: " + r +
               "\nDoc: " + d +
               "\nQuery: " + query +
               "\nDocument: " + doc);
        }
      }
    }
  }
}
