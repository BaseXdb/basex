package org.basex.query;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import org.basex.query.expr.ft.*;
import org.junit.*;

/**
 * Wild-card parsing and matching tests.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Dimitar Popov
 */
public final class FTWildcardTest {
  /** Sample texts which should be matched. */
  private static final String[][] TEXTS_GOOD = {
    { "well", "wall", "will", "w.ll", "w\u00fcll" },
    { "site", "asite" },
    { "improv", "improve", "improvement" },
    { "wisdomness", "witches" },
    { "site" },
    { "Usability\\", "Usable\\" },
    { "\u00fcbersch\u00fcsse", "\u00fcberschuss" }
  };
  /** Sample texts which should NOT be matched. */
  private static final String[][] TEXTS_BAD = {
    { "wll", "wel", "ell" },
    { "bite", "abite", "sit", "asit" },
    { "impro", "mprove" },
    { "witch", "wiskey", "wisdomnetts" },
    { "\\s\\i\\t\\e" },
    { "Usability", "Usab\\", "usability\\" },
    { "ueberschuss" }
  };

  /** Valid wild-card expressions. */
  private static final String[] VALIDWC = {
    "w.ll",
    ".?site",
    "improv.*",
    "wi.{4,7}s",
    "\\s\\i\\t\\e",
    "Usab.+\\\\",
    "\u00fcbersch.ss.?"
  };

  /** Valid wild card expressions. */
  private static final String[] INVALIDWC = {
    ".{5,7]",
    ".{2,1}",
    ".{,}",
    ".{0,}",
    ".{,0}",
    ".{-1,0}",
    "will\\"
  };

  /** Test if wild-card expressions are correctly parsed. */
  @Test
  public void testParse() {
    for(final String wc : VALIDWC) assertTrue(new FTWildcard(token(wc)).parse());
    for(final String wc : INVALIDWC) assertFalse(new FTWildcard(token(wc)).parse());
  }

  /**
   * Test wild-card matching.
   */
  @Test
  public void testMatch() {
    for(int i = 0; i < VALIDWC.length; i++) {
      final String q = VALIDWC[i];
      final FTWildcard wc = new FTWildcard(token(q));
      assertTrue(wc.parse());

      final String[] good = TEXTS_GOOD[i];
      for(final String g : good) {
        assertTrue('"' + q + "\" did NOT match \"" + g + '"', wc.match(token(g)));
      }

      final String[] bad = TEXTS_BAD[i];
      for(final String b : bad) {
        assertFalse('"' + q + "\" matched \"" + b + '"', wc.match(token(b)));
      }
    }
  }
}
