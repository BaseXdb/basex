package org.basex.query.simple;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

/**
 * Tests for {@code id} and {@code idref}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Jens Erat
 */
public final class IdIdrefTest extends QueryTest {
  /**
   * Queries to test.
   * @return argument stream
   */
  private static Stream<Arguments> testQueries() {
    return Stream.of(
      Arguments.of(new int[] { 1 }, _DB_OPEN.args(NAME, "1.xml") + "/id('foo')"),
      Arguments.of(new int[] { 1 }, _DB_OPEN.args(NAME, "1.xml") + "/id('foo')"),
      Arguments.of(new int[] { 5 }, _DB_OPEN.args(NAME, "2.xml") + "/id('batz')"),
      Arguments.of(new int[] { 3 }, _DB_OPEN.args(NAME, "1.xml") + "/idref('bar')"),
      Arguments.of(new int[] { 7 }, _DB_OPEN.args(NAME, "2.xml") + "/idref('quix')"),
      Arguments.of(new int[] { 3, 7 }, "collection('" + NAME + "')/idref('quix', .)")
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

  /**
   * Prepare test, setting up parameterized environment.
   * @param mainmem main-memory flag
   * @param updindex updatable index flag
   * @param tokenindex token index flag
   * @param expectedIds expected results
   * @param query query string
   * @throws Exception exception
   */
  @DisplayName("IdIdrefTest")
  @ParameterizedTest(name = "[{3}] {4}: mainmem={0}, updindex={1}, tokenindex={2}")
  @MethodSource("generateParams")
  public void test(final boolean mainmem, final boolean updindex, final boolean tokenindex,
      final int[] expectedIds, final String query)
      throws Exception {

    // set up environment
    execute(new Set(MainOptions.MAINMEM, mainmem));
    execute(new Set(MainOptions.UPDINDEX, updindex));
    execute(new Set(MainOptions.TOKENINDEX, tokenindex));
    execute(new CreateDB(NAME));
    execute(new Add("1.xml", "<root1 id='foo' idref='bar quix' />"));
    execute(new Add("2.xml", "<root2 id='batz' idref2='quix' />"));

    final Value expected = nodes(expectedIds);
    final Value actual = run(query);

    assertTrue(eq(actual, expected), String.format(
      "[E] %d result(s): %s\n[F] %d result(s): %s",
      expected.size(), serialize(expected),
      actual.size(), serialize(actual)));
  }

  @Override
  @Test public void test() {
    // super.test() is not needed since we use @ParameterizedTest with queries as parameters.
  }
}
