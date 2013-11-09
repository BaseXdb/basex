package org.basex.util;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.ft.*;
import org.basex.util.ft.*;
import org.basex.util.ft.FTBitapSearch.TokenComparator;
import org.basex.util.list.*;
import org.junit.*;

/**
 * Test {@link FTBitapSearch} methods.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 */
public final class FTBitapSearchTest {
  /** Simple comparator. */
  private static final TokenComparator CMP = new TokenComparator() {
    @Override
    public boolean equal(final byte[] o1, final byte[] o2) {
      return eq(o1, o2);
    }
  };

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
     * @param h haystack
     * @param n needle
     * @param e expected hits
     */
    public TestData(final String[] h, final String[][] n, final int[] e) {
      expected = e;
      final byte[][] hs = new byte[h.length][];
      for(int i = 0; i < h.length; i++)
        hs[i] = token(h[i]);

      haystack = new FTIterator() {
        /** Index of current element. */
        private int cnt;

        @Override
        public boolean hasNext() {
          return cnt < hs.length;
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
      for(final String[] s : n) {
        final TokenList needle = new TokenList(s.length);
        for(final String t : s) needle.add(t);
        needles.add(needle);
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
  @Before
  public void setUp() {
    searches = new FTBitapSearch[TESTS.length];
    for(int i = 0; i < searches.length; i++) {
      searches[i] = new FTBitapSearch(TESTS[i].haystack, TESTS[i].needles, CMP);
    }
  }

  /** Test search. */
  @Test
  public void searchIter() {
    try {
      for(int i = 0; i < TESTS.length; i++) {
        final FTBitapSearch s = searches[i];
        for(int j = 0; j < TESTS[i].expected.length; j++) {
          if(!s.hasNext())
            fail("Test " + i + ": expected " + TESTS[i].expected.length +
                " hits, got only " + (j + 1));
          final int pos = s.next();
          if(pos != TESTS[i].expected[j])
            fail("Test " + i + ", result " + j + ": expected " +
                TESTS[i].expected[j] + ", got " + pos);
        }
        if(s.hasNext())
          fail("Test " + i + ": expected " + TESTS[i].expected.length +
              " hits, got more!");
      }
    } catch(final QueryException ex) {
      fail(Util.message(ex));
    }
  }
}
