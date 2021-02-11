package org.basex.io;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Test class for glob patterns.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class GlobTest {
  /** Input strings. */
  private static final String[] STRINGS = {
    "ab", "ab.cd", "ef.cd", "ab.ef.cd"
  };

  /** Tests. */
  enum TEST {
    /** Test. */ FILE1("a", true, true, false, true),
    /** Test. */ FILE2("ab", true, true, false, true),
    /** Test. */ FULL("ab.cd", false, true, false, false),
    /** Test. */ SUFFIX("*.cd", false, true, true, true),
    /** Test. */ ALL("*", true, true, true, true),

    /** Test. */ ABC("ab.c", false, false, false, false),
    /** Test. */ ABCDE("ab.cde", false, false, false, false),
    /** Test. */ MULTIPLE("ab,ef", true, true, true, true),
    /** Test. */ SPACES1("ab ,ef", true, true, true, true),
    /** Test. */ SPACES2("ab, ef", true, true, true, true),
    /** Test. */ AST2("a*.cd", false, true, false, true),
    /** Test. */ QUESTION("?", false, false, false, false),
    /** Test. */ QUESTIONS("??", true, false, false, false),
    /** Test. */ NOSUFFIX("*.", true, false, false, false),
    /** Test. */ NOSUFFIX2("*..", false, false, false, false),
    /** Test. */ BACK("\\", false, false, false, false);

    /** Glob syntax. */
    final String glob;
    /** Results. */
    final boolean[] results;

    /**
     * Constructor.
     * @param g glob syntax
     * @param r results
     */
    TEST(final String g, final boolean... r) {
      glob = g;
      results = r;
    }
  }

  /**
   * Glob test.
   */
  @Test public void test() {
    for(final TEST test : TEST.values()) {
      final String regex = IOFile.regex(test.glob);
      final int sl = STRINGS.length;
      for(int s = 0; s < sl; s++) {
        final boolean expected = test.results[s];
        final boolean result = STRINGS[s].matches(regex);
        if(expected != result) fail(test + " #" + s + " failed.\n" +
            "Query: \"" + test.glob + "\" matches \"" + STRINGS[s] +
            "\" \u2192 " + result + "\nExpected: " + expected +
            "\nRegex: " + regex);
      }
    }
  }
}
