package org.basex.performance;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.util.*;
import org.basex.util.list.*;
import org.junit.*;
import org.junit.Test;

/**
 * This test class performs random incremental updates with random documents.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class UpdIndexRandomTest extends SandboxTest {
  /** Number of different documents. */
  private static final int DOCS = 20;
  /** Number of runs. */
  private static final int RUNS = 500;

  /**
   * Initializes the test.
   * @throws Exception exception
   */
  @Before
  public void init() throws Exception {
    new Set(MainOptions.UPDINDEX, true).execute(context);
    new Set(MainOptions.ATTRINDEX, false).execute(context);
    new CreateDB(NAME).execute(context);
  }

  /**
   * Incremental test.
   * @throws Exception exception
   */
  @Test
  public void insertInto() throws Exception {
    final Random rnd = new Random(0);

    // create random words
    int cap = 1000;
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
      final TokenBuilder doc = new TokenBuilder("<xml>");

      final int offset = rnd.nextInt(cap - DOCS);
      for(int i = 0; i < DOCS; i++) doc.add("<a>").add(words.get(offset + i)).add("</a>");
      doc.add("</xml>");
      new Replace(path, doc.toString()).execute(context);

      for(int d = 0; d < DOCS; d++) {
        final String word = words.get(offset + d);
        final String query = _DB_OPEN.args(NAME, path) + "//a[text() = '" + word + "']";
        final String expected = "<a>" + word + "</a>";
        final String result = new XQuery(query).execute(context);
        if(!result.startsWith(expected)) {
          fail(new TokenBuilder("\nExpected: " + expected +
              "\nResult: " + result + "\nRun: " + r + "\nDoc: " + d +
              "\nQuery: " + query + "\nDocument: " + doc).toString());
        }
      }
    }
  }
}
