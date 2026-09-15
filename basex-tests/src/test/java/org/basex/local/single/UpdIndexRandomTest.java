package org.basex.local.single;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

/**
 * This test class performs random incremental updates with random documents.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UpdIndexRandomTest extends SandboxTest {
  /** Number of different documents. */
  private static final int DOCS = 20;
  /** Number of runs. */
  private static final int RUNS = 500;

  /**
   * Test parameters: main memory flag, full-text index flag, mixed-content flag.
   * @return parameters
   */
  public static Stream<Arguments> params() {
    return Stream.of(
      Arguments.of(true, false, false),
      Arguments.of(false, false, false),
      Arguments.of(false, true, false),
      Arguments.of(false, true, true)
    );
  }

  /**
   * Initializes the test.
   * @param mainmem main memory flag
   * @param ftindex full-text index flag
   * @param ftmixed mixed-content flag
   */
  public void init(final boolean mainmem, final boolean ftindex, final boolean ftmixed) {
    set(MainOptions.MAINMEM, mainmem);
    set(MainOptions.UPDINDEX, true);
    set(MainOptions.ATTRINDEX, false);
    set(MainOptions.FTINDEX, ftindex);
    set(MainOptions.FTMIXED, ftmixed);
    set(MainOptions.FTINCLUDE, ftmixed ? "a" : "");
    execute(new CreateDB(NAME));
  }

  /**
   * Finishes the test.
   */
  @AfterEach public void finish() {
    set(MainOptions.FTINDEX, false);
    set(MainOptions.FTMIXED, false);
    set(MainOptions.FTINCLUDE, "");
  }

  /**
   * Incremental test.
   * @param mainmem main memory flag
   * @param ftindex full-text index flag
   * @param ftmixed mixed-content flag
   */
  @ParameterizedTest
  @MethodSource("params")
  public void insertInto(final boolean mainmem, final boolean ftindex, final boolean ftmixed) {
    init(mainmem, ftindex, ftmixed);

    final Random rnd = new Random(0);

    // create random words
    final int cap = 1000;
    final StringList words = new StringList(cap);
    for(int w = 0; w < cap; w++) {
      final int size = 1 + rnd.nextInt(10);
      final TokenBuilder tmp = new TokenBuilder(size);
      for(int s = 0; s < size; s++) {
        tmp.add('A' + rnd.nextInt(26));
      }
      words.add(tmp.toString());
    }

    for(int r = 0; r < RUNS; r++) {
      final String path = "doc" + rnd.nextInt(DOCS);
      // create random document
      final TokenBuilder doc = new TokenBuilder().add("<xml>");

      final int offset = rnd.nextInt(cap - DOCS);
      for(int d = 0; d < DOCS; d++) {
        doc.add("<a>").add(words.get(offset + d)).add("</a>");
      }
      doc.add("</xml>");
      execute(new Put(path, doc.toString()));

      for(int d = 0; d < DOCS; d++) {
        final String word = words.get(offset + d);
        final String query = _DB_GET.args(NAME, path) + "//a[text() = '" + word + "']";
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
      // compare index access and scan for a word of the current and of another document
      queryIndexScan("//a[text() = '" + words.get(offset) + "']");
      queryIndexScan("//a[text() = '" + words.get(rnd.nextInt(cap)) + "']");
      if(ftindex) {
        final String step = ftmixed ? "//a[. contains text '" : "//a[text() contains text '";
        queryIndexScan(step + words.get(offset) + "']");
        queryIndexScan(step + words.get(rnd.nextInt(cap)) + "']");
      }
    }
  }
}
