package org.basex.index;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.List;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.index.query.*;
import org.basex.index.value.*;
import org.basex.util.hash.*;
import org.junit.*;
import org.junit.Test;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for the value index.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Jens Erat
 */
@RunWith(Parameterized.class)
public final class ValueIndexTest extends SandboxTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/test.xml";
  /** Test parameters. */
  private final Collection<Set> paramSet;

  /**
   * Runs tests with different parameters, like in-memory or disk databases.
   * @return collection of parameter sets
   */
  @Parameters
  public static Collection<Object[]> generateParams() {
    final List<Object[]> paramsSet = new ArrayList<>();
    paramsSet.add(paramSet(false, false));
    paramsSet.add(paramSet(true, false));
    return paramsSet;
  }

  /**
   * Return parameter set for parameterized execution.
   * @param mainmem MAINMEM option
   * @param updindex UPDINDEX option
   * @return parameter set
   */
  private static Object[] paramSet(final boolean mainmem,
      final boolean updindex) {
    final ArrayList<Set> params = new ArrayList<>();
    params.add(new Set(MainOptions.MAINMEM, mainmem));
    params.add(new Set(MainOptions.UPDINDEX, updindex));
    final ArrayList<ArrayList<Set>> paramArray = new ArrayList<>();
    paramArray.add(params);
    return paramArray.toArray();
  }

  /**
   * Apply parameter sets.
   * @param paramSet parameter set for current test run
   */
  public ValueIndexTest(final Collection<Set> paramSet) {
    this.paramSet = paramSet;
  }

  /** Set-up database. */
  @Before
  public void setUp() {
  }

  /** Set-up database. */
  @After
  public void setDown() {
    execute(new Set(MainOptions.TOKENINCLUDE, ""));
    execute(new DropDB(NAME));
  }

  /**
   * Tests the text index.
   */
  @Test
  public void textIndexTest() {
    final LinkedHashMap<String, Integer> tokens = new LinkedHashMap<>();
    tokens.put("3", 3);
    tokens.put("3.4", 1);
    tokens.put("text in child", 1);
    tokens.put("nonexistant", 0);
    tokens.put("", 0);

    valueIndexTest(IndexType.TEXT, tokens, paramSet);
  }

  /**
   * Tests the attribute index.
   */
  @Test
  public void attributeIndexTest() {
    final LinkedHashMap<String, Integer> tokens = new LinkedHashMap<>();
    tokens.put("context", 1);
    tokens.put("baz bar blu", 1);
    tokens.put("baz", 0);
    tokens.put("bar", 0);
    tokens.put("blu", 0);
    tokens.put("", 0);
    tokens.put("nonexistant", 0);

    valueIndexTest(IndexType.ATTRIBUTE, tokens, paramSet);
  }

  /**
   * Tests the index: fetch results for different tokens, compare whether the right node was
   * returned and verify against the expected result size.
   * @param indexType text or attribute index
   * @param tokens map of search tokens
   * @param options options to apply
   */
  private void valueIndexTest(final IndexType indexType, final LinkedHashMap<String,
      Integer> tokens, final Collection<Set> options) {
    // Set up environment
    for(final Set option : options) execute(option);
    execute(new CreateDB(NAME, FILE));

    // Fetch index reference to be tested
    final boolean text = IndexType.TEXT == indexType;
    final ValueIndex index = text ? context.data().textIndex : IndexType.TOKEN == indexType
        ? context.data().tokenIndex : context.data().attrIndex;

    // Receive, verify and count results for passed tokens
    for(final Entry<String, Integer> entry : tokens.entrySet()) {
      final byte[] token = token(entry.getKey());
      final IndexIterator it = index.iter(new IndexEntries(token, indexType));
      long count = 0;
      while(it.more()) {
        final int pre = it.pre();
        final byte[] result = context.data().text(pre, text);
        if(IndexType.TOKEN == indexType)
          assertTrue("Token '" + entry.getKey() + "' not found in match '" + string(result) + "'!",
              new TokenSet(distinctTokens(result)).contains(token(entry.getKey())));
        else
          assertEquals("Wrong result returned!", entry.getKey(), string(result));
        count++;
      }
      assertEquals("Wrong number of nodes returned: \"" + entry.getKey() + "\": ",
          (long) entry.getValue(), count);
    }

    // Reset environment
    execute(new Set(MainOptions.MAINMEM, false));
    execute(new Set(MainOptions.UPDINDEX, false));
    execute(new DropDB(NAME));

  }

}
