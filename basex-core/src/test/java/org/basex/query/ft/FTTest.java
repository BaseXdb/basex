package org.basex.query.ft;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.util.options.*;
import org.junit.jupiter.api.Test;

/**
 * Full-text test queries.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FTTest extends FTData {
  /** Test all flag. */
  private static final boolean ALL = true;

  static { create(DOC); }
  static { queries = QUERIES; }

  @Test @Override
  public void test() {
    if(ALL) {
      // test with and without index
      for(int a = 0; a < 2; ++a) {
        set(MainOptions.FTINDEX, a == 0);
        super.test();
      }
    } else {
      // single test
      set(MainOptions.FTINDEX, true);
      set(MainOptions.STEMMING, true);
      set(MainOptions.DIACRITICS, true);
      set(MainOptions.CASESENS, true);
      super.test();
    }
  }

  @Override
  protected String details() {
    final MainOptions opts = context.options;
    return toString(opts, MainOptions.FTINDEX) + ';' +
      toString(opts, MainOptions.STEMMING) + ';' +
      toString(opts, MainOptions.DIACRITICS) + ';' +
      toString(opts, MainOptions.CASESENS);
  }

  /**
   * Returns a flag string.
   * @param opts options
   * @param option option
   * @return string
   */
  private static String toString(final Options opts, final BooleanOption option) {
    return new Set(option, opts.get(option)).toString();
  }
}
