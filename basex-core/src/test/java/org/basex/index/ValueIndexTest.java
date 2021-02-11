package org.basex.index;

import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.List;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.cmd.Set;
import org.basex.index.query.*;
import org.basex.index.value.*;
import org.basex.util.hash.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for the value index.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Jens Erat
 */
public final class ValueIndexTest extends SandboxTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/test.xml";

  /**
   * Runs tests with different parameters, like in-memory or disk databases.
   * @return collection of parameter sets
   */
  public static Collection<Object[]> generateParams() {
    final List<Object[]> paramsSet = new ArrayList<>();
    paramsSet.add(paramSet(false, false));
    paramsSet.add(paramSet(true, false));
    paramsSet.add(paramSet(false, true));
    paramsSet.add(paramSet(true, true));
    return paramsSet;
  }

  /**
   * Return parameter set for parameterized execution.
   * @param mainmem MAINMEM option
   * @param updindex UPDINDEX option
   * @return parameter set
   */
  private static Object[] paramSet(final boolean mainmem, final boolean updindex) {
    final ArrayList<Set> params = new ArrayList<>();
    params.add(new Set(MainOptions.MAINMEM, mainmem));
    params.add(new Set(MainOptions.UPDINDEX, updindex));
    final ArrayList<ArrayList<Set>> paramArray = new ArrayList<>();
    paramArray.add(params);
    return paramArray.toArray();
  }

  /** Set down database. */
  @AfterEach public void setDown() {
    set(MainOptions.MAINMEM, false);
    set(MainOptions.UPDINDEX, false);
    execute(new DropDB(NAME));
  }

  /**
   * Tests the text index.
   * @param paramSet test parameters
   */
  @ParameterizedTest
  @MethodSource("generateParams")
  public void textIndexTest(final Collection<Set> paramSet) {
    final LinkedHashMap<String, Integer> tokens = new LinkedHashMap<>();
    tokens.put("3", 3);
    tokens.put("3.4", 1);
    tokens.put("text in child", 1);
    tokens.put("nonexistent", 0);
    tokens.put("", 0);

    valueIndexTest(IndexType.TEXT, tokens, paramSet);
  }

  /**
   * Tests the attribute index.
   * @param paramSet test parameters
   */
  @ParameterizedTest
  @MethodSource("generateParams")
  public void attributeIndexTest(final Collection<Set> paramSet) {
    final LinkedHashMap<String, Integer> tokens = new LinkedHashMap<>();
    tokens.put("context", 1);
    tokens.put("baz bar blu", 1);
    tokens.put("baz", 0);
    tokens.put("bar", 0);
    tokens.put("blu", 0);
    tokens.put("", 0);
    tokens.put("nonexistent", 0);
    valueIndexTest(IndexType.ATTRIBUTE, tokens, paramSet);
  }

  /**
   * Tests the token index.
   * @param paramSet test parameters
   */
  @ParameterizedTest
  @MethodSource("generateParams")
  public void tokenIndexTest(final Collection<Set> paramSet) {
    set(MainOptions.TOKENINDEX, true);

    final LinkedHashMap<String, Integer> tokens = new LinkedHashMap<>();
    tokens.put("context", 1);
    tokens.put("baz bar blu", 0);
    tokens.put("baz", 1);
    tokens.put("bar", 1);
    tokens.put("blu", 1);
    tokens.put("", 0);
    tokens.put("nonexistent", 0);
    valueIndexTest(IndexType.TOKEN, tokens, paramSet);
  }

  /**
   * Tests the index: fetch results for different tokens, compare whether the right node was
   * returned and verify against the expected result size.
   * @param indexType text or attribute index
   * @param tokens map of search tokens
   * @param options options to apply
   */
  private static void valueIndexTest(final IndexType indexType, final LinkedHashMap<String,
    Integer> tokens, final Collection<Set> options) {
    // set up environment
    for(final Set option : options) execute(option);
    execute(new CreateDB(NAME, FILE));

    // fetch index reference to be tested
    final boolean text = indexType == IndexType.TEXT;
    final ValueIndex index = (ValueIndex) context.data().index(indexType);

    // receive, verify and count results for passed tokens
    tokens.forEach((key, value) -> {
      final byte[] token = token(key);
      final IndexIterator it = index.iter(new IndexEntries(token, indexType));
      long count = 0;
      while(it.more()) {
        final int pre = it.pre();
        final byte[] result = context.data().text(pre, text);
        if(indexType == IndexType.TOKEN)
          assertTrue(new TokenSet(distinctTokens(result)).contains(token(key)),
            "Token '" + key + "' not found in match '" + string(result) + "'!");
        else
          assertEquals(key, string(result), "Wrong result returned!");
        count++;
      }
      assertEquals((int) value, count, "Wrong number of nodes returned: \"" + key + "\": ");
    });
  }

}
