package org.basex.query.simple;

import static org.basex.query.func.Function.*;

import java.util.*;
import java.util.List;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.query.*;
import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.*;

/**
 * Tests for {@code id} and {@code idref}.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Jens Erat
 */
@RunWith(Parameterized.class)
public final class IdIdrefTest extends QueryTest {
  /** Test parameters. */
  private final Collection<Set> paramSet;

  /**
   * Runs tests with different parameters, like in-memory or disk databases.
   * @return collection of parameter sets
   */
  @Parameters
  public static Collection<Object[]> generateParams() {
    final List<Object[]> paramsSet = new ArrayList<>();
    paramsSet.add(paramSet(false, false, false));
    paramsSet.add(paramSet(false, false, true));
    paramsSet.add(paramSet(false, true, false));
    paramsSet.add(paramSet(false, true, true));
    paramsSet.add(paramSet(true, false, false));
    paramsSet.add(paramSet(true, false, true));
    paramsSet.add(paramSet(true, true, false));
    paramsSet.add(paramSet(true, true, true));
    return paramsSet;
  }

  /**
   * Return parameter set for parameterized execution.
   * @param mainmem {@link MainOptions#MAINMEM} option
   * @param updindex {@link MainOptions#UPDINDEX} option
   * @param tokenindex {@link MainOptions#TOKENINDEX} option
   * @return parameter set
   */
  private static Object[] paramSet(final boolean mainmem, final boolean updindex,
      final boolean tokenindex) {
    final ArrayList<Set> params = new ArrayList<>();
    params.add(new Set(MainOptions.MAINMEM, mainmem));
    params.add(new Set(MainOptions.UPDINDEX, updindex));
    params.add(new Set(MainOptions.TOKENINDEX, tokenindex));
    final ArrayList<ArrayList<Set>> paramArray = new ArrayList<>();
    paramArray.add(params);
    return paramArray.toArray();
  }

  /**
   * Apply parameter sets.
   * @param paramSet parameter set for current test run
   */
  public IdIdrefTest(final Collection<Set> paramSet) {
    this.paramSet = paramSet;
  }

  /**
   * Prepare test, setting up parametrized environment.
   */
  @Before
  public void setup() {
    // set up environment
    for(final Set option : paramSet) execute(option);
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
  }
}
