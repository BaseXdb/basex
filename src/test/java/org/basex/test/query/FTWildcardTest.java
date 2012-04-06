package org.basex.test.query;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;

import org.basex.query.*;
import org.basex.query.ft.*;
import org.junit.*;

/**
 * Wild-card parsing and matching tests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public class FTWildcardTest {
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
  private static final String[][] TEXTS_BAD = new String[][] {
    { "wll", "wel", "ell" },
    { "bite", "abite", "sit", "asit" },
    { "impro", "mprove" },
    { "witch", "wiskey", "wisdomnetts" },
    { "\\s\\i\\t\\e" },
    { "Usability", "Usab\\", "usability\\" },
    { "ueberschuss" }
  };

  /** Valid wild-card expressions. */
  private static final String[] VALIDWC = new String[] {
    "w.ll",
    ".?site",
    "improv.*",
    "wi.{4,7}s",
    "\\s\\i\\t\\e",
    "Usab.+\\\\",
    "\u00fcbersch.ss.?"
  };

  /** Valid wild card expressions. */
  private static final String[] INVALIDWC = new String[] {
    "wi.{5,7]",
    "will\\"
  };

  /** Test if wild-card expressions are correctly parsed. */
  @Test
  public void testParse() {
    for(final String wc : VALIDWC)
      try {
        new FTWildcard(token(wc), null);
      } catch(final Exception ex) {
        ex.printStackTrace();
        fail("Parsing failed: " + wc);
      }

    for(final String wc : INVALIDWC)
      try {
        new FTWildcard(token(wc), null);
        fail("Parsing did NOT fail: " + wc);
      } catch(final QueryException ex) {
      } catch(final Exception ex) {
        ex.printStackTrace();
        fail("Error while parsing: " + wc);
      }
  }

  /**
   * Test wild-card matching.
   * @throws QueryException wild-card expression is not parsed
   */
  @Test
  public void testMatch() throws QueryException {
    for(int i = 0; i < VALIDWC.length; i++) {

      final String q = VALIDWC[i];
      final FTWildcard wc = new FTWildcard(token(q), null);

      final String[] good = TEXTS_GOOD[i];
      for(final String element : good) {
        assertTrue('"' + q + "\" did NOT match \"" + element + '"',
            wc.match(token(element)));
      }

      final String[] bad = TEXTS_BAD[i];
      for(final String element : bad) {
        assertFalse('"' + q + "\" matched \"" + element + '"',
            wc.match(token(element)));
      }
    }
  }
}
