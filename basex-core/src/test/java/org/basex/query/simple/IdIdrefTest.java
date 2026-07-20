package org.basex.query.simple;

import static org.basex.query.func.Function.*;

import java.util.stream.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

/**
 * Tests for {@code ID} and {@code IDREF}. As the queries return element or attribute nodes and are
 * evaluated with different storage options (including main memory), results are compared via their
 * node names.
 *
 * @author BaseX Team, BSD License
 * @author Jens Erat
 */
public final class IdIdrefTest extends SandboxTest {
  /**
   * Queries to test.
   * @return argument stream (expected node names, query)
   */
  private static Stream<Arguments> testQueries() {
    return Stream.of(
      Arguments.of("root1", _DB_GET.args(NAME, "1.xml") + "/id('foo')"),
      Arguments.of("root1", _DB_GET.args(NAME, "1.xml") + "/id('foo')"),
      Arguments.of("root2", _DB_GET.args(NAME, "2.xml") + "/id('batz')"),
      Arguments.of("idref", _DB_GET.args(NAME, "1.xml") + "/idref('bar')"),
      Arguments.of("idref2", _DB_GET.args(NAME, "2.xml") + "/idref('quix')"),
      Arguments.of("idref\nidref2", "collection('" + NAME + "')/idref('quix', .)")
    );
  }

  /**
   * Parameters.
   * @return argument stream
   */
  private static Stream<Arguments> generateParams() {
    return testQueries().map(Arguments::get).flatMap(q -> Stream.of(
      Arguments.of(false, false, false, q[0], q[1]),
      Arguments.of(false, false, true, q[0], q[1]),
      Arguments.of(false, true, false, q[0], q[1]),
      Arguments.of(false, true, true, q[0], q[1]),
      Arguments.of(true, false, false, q[0], q[1]),
      Arguments.of(true, false, true, q[0], q[1]),
      Arguments.of(true, true, false, q[0], q[1]),
      Arguments.of(true, true, true, q[0], q[1])
    ));
  }

  /** Resets options and drops the test database. */
  @AfterEach public void tearDown() {
    execute(new DropDB(NAME));
    set(MainOptions.MAINMEM, false);
    set(MainOptions.UPDINDEX, false);
    set(MainOptions.TOKENINDEX, false);
  }

  /**
   * Runs a query in a parameterized environment and checks the resulting node names.
   * @param mainmem main-memory flag
   * @param updindex updatable index flag
   * @param tokenindex token index flag
   * @param names expected node names (newline-separated)
   * @param qu query string
   */
  @ParameterizedTest(name = "{4}: mainmem={0}, updindex={1}, tokenindex={2}")
  @MethodSource("generateParams")
  public void test(final boolean mainmem, final boolean updindex, final boolean tokenindex,
      final String names, final String qu) {
    set(MainOptions.MAINMEM, mainmem);
    set(MainOptions.UPDINDEX, updindex);
    set(MainOptions.TOKENINDEX, tokenindex);
    execute(new CreateDB(NAME));
    execute(new Add("1.xml", "<root1 id='foo' idref='bar quix' />"));
    execute(new Add("2.xml", "<root2 id='batz' idref2='quix' />"));

    query("(" + qu + ") ! name()", names);
  }
}
