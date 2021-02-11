package org.basex.util;

import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.ft.*;
import org.basex.util.ft.*;
import org.basex.util.ft.FTBitapSearch.TokenComparator;
import org.basex.util.list.*;
import org.junit.jupiter.api.*;

/**
 * Test {@link FTBitapSearch} methods.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 */
public final class FTBitapSearchTest {
  /**
   * Test data.
   * @author Dimitar Popov
   */
  public static class TestData {
    /** Set to search in. */
    public final FTIterator haystack;
    /** Set to search for. */
    public final FTTokens needles;
    /** Indices of expected hits. */
    public final int[] expected;

    /**
     * Constructor.
     * @param hay hay stack
     * @param needle needles
     * @param expected expected hits
     */
    public TestData(final String[] hay, final String[][] needle, final int[] expected) {
      this.expected = expected;
      final int hl = hay.length;
      final byte[][] hs = new byte[hl][];
      for(int h = 0; h < hay.length; h++) hs[h] = token(hay[h]);

      haystack = new FTIterator() {
        /** Index of current element. */
        private int cnt;

        @Override
        public boolean hasNext() {
          return cnt < hl;
        }

        @Override
        public byte[] nextToken() {
          if(hasNext()) return hs[cnt++];
          throw new NoSuchElementException();
        }

        @Override
        public FTSpan next() {
          return null;
        }

        @Override
        public FTIterator init(final byte[] text) {
          return this;
        }
      };

      needles = new FTTokens();
      for(final String[] strings : needle) {
        final TokenList tl = new TokenList(strings.length);
        for(final String string : strings) tl.add(string);
        needles.add(tl);
      }
    }
  }

  /** Test data. */
  private static final TestData[] TESTS = {
      new TestData(
          new String[] { "token1", "token1", "token2", "token3", "token4",
              "token1", "token2", "token3", "token3" },
          new String[][] { { "token1", "token2" } },
          new int[] { 1, 5 }),
      new TestData(
          new String[] { "token1" },
          new String[][] { { "token1" } },
          new int[] { 0 }),
      new TestData(
          new String[] { "token1" },
          new String[][] { { "token2" } },
          new int[] { }),
      new TestData(
          new String[] { "token1" },
          new String[][] { { } },
          new int[] { }),
      new TestData(
          new String[] { "token1", "token2", "token1", "token2", "token1" },
          new String[][] { { "token1", "token2", "token1" } },
          new int[] { 0, 2 }),
      new TestData(
          new String[] { "token", "token", "token" },
          new String[][] { { "token", "token" } },
          new int[] { 0, 1}),
      new TestData(
          new String[] { },
          new String[][] { { "token", "token" } },
          new int[] { }),
      new TestData(
          new String[] { },
          new String[][] { { } },
          new int[] { }),
      new TestData(
          new String[] { "token" },
          new String[][] { },
          new int[] { }),
      new TestData(
          new String[] { "token1", "token2", "token3" },
          new String[][] { { "token2", "token3" }, { "token2" } },
          new int[] { 1, 1}),
      new TestData(
          new String[] { "token1", "token2", "token3" },
          new String[][] { { "token2" }, { "token2", "token3" } },
          new int[] { 1, 1 }),
      new TestData(
          new String[] { "token1", "token2", "token3" },
          new String[][] { { "token2" }, { "token1", "token2" } },
          new int[] { 0})};

  /** Pre-initialized {@link FTBitapSearch} objects. */
  private FTBitapSearch[] searches;

  /** Set up method. */
  @BeforeEach public void setUp() {
    final TokenComparator cmp = Token::eq;
    final int tl = TESTS.length;
    searches = new FTBitapSearch[tl];
    for(int t = 0; t < tl; t++) {
      searches[t] = new FTBitapSearch(TESTS[t].haystack, TESTS[t].needles, cmp);
    }
  }

  /** Test search. */
  @Test public void searchIter() {
    try {
      final int tl = TESTS.length;
      for(int t = 0; t < tl; t++) {
        final FTBitapSearch s = searches[t];
        final TestData test = TESTS[t];
        final int el = test.expected.length;
        for(int e = 0; e < el; e++) {
          final int exp = test.expected[e];
          if(!s.hasNext())
            fail("Test " + t + ": expected " + el + " hits, got only " + (e + 1));
          final int pos = s.next();
          if(pos != exp)
            fail("Test " + t + ", result " + e + ": expected " + exp + ", got " + pos);
        }
        if(s.hasNext())
          fail("Test " + t + ": expected " + el + " hits, got more!");
      }
    } catch(final QueryException ex) {
      fail(Util.message(ex));
    }
  }
}
