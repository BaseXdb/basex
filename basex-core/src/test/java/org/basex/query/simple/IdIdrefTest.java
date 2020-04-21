package org.basex.query.simple;

import static org.basex.query.func.Function.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.query.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

/**
 * Tests for {@code id} and {@code idref}.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Jens Erat
 */
public final class IdIdrefTest extends QueryTest {

  public static  Stream<Arguments> generateParams() {
    return Stream.of(
      Arguments.of(false, false, false),
      Arguments.of(false, false, true),
      Arguments.of(false, true, false),
      Arguments.of(false, true, true),
      Arguments.of(true, false, false),
      Arguments.of(true, false, true),
      Arguments.of(true, true, false),
      Arguments.of(true, true, true)
    );
  }

  /**
   * Prepare test, setting up parametrized environment.
   */
  @ParameterizedTest
  @MethodSource("generateParams")
  public void test(final boolean mainmem, final boolean updindex, final boolean tokenindex) {
    // set up environment
    execute(new Set(MainOptions.MAINMEM, mainmem));
    execute(new Set(MainOptions.UPDINDEX, updindex));
    execute(new Set(MainOptions.TOKENINDEX, tokenindex));
    execute(new CreateDB(NAME));
    execute(new Add("1.xml", "<root1 id='foo' idref='bar quix' />"));
    execute(new Add("2.xml", "<root2 id='batz' idref2='quix' />"));

    queries = new Object[][] {
      { "id1", nodes(1), _DB_OPEN.args(NAME, "1.xml") + "/id('foo')" },
      { "id1", nodes(1), _DB_OPEN.args(NAME, "1.xml") + "/id('foo')" },
      { "id2", nodes(5), _DB_OPEN.args(NAME, "2.xml") + "/id('batz')" },
      { "idref1", nodes(3), _DB_OPEN.args(NAME, "1.xml") + "/idref('bar')" },
      { "idref2", nodes(7), _DB_OPEN.args(NAME, "2.xml") + "/idref('quix')" },
      { "idref2", nodes(3, 7), "collection('" + NAME + "')/idref('quix', .)" },
    };

    super.test();
  }

  @Test
  @Disabled("Test is parameterized")
  @Override
  public void test() {
  }
}
