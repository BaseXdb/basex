package org.basex.query.index;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.util.options.*;
import org.junit.Test;

/**
 * Full-text test queries.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FTTest extends FTData {
  /** Test all flag. */
  private static final boolean ALL = true;

  static { create(DOC); }
  static { queries = QUERIES; }

  @Test
  @Override
  public void test() {
    final MainOptions opts = context.options;
    if(ALL) {
      // test with and without index
      for(int a = 0; a < 2; ++a) {
        opts.set(MainOptions.FTINDEX, a == 0);
        super.test();
      }
    } else {
      // single test
      opts.set(MainOptions.FTINDEX, true);
      opts.set(MainOptions.STEMMING, true);
      opts.set(MainOptions.DIACRITICS, true);
      opts.set(MainOptions.CASESENS, true);
      super.test();
    }
  }

  @Override
  protected String details() {
    final MainOptions opts = context.options;
    final StringBuilder sb = new StringBuilder();
    sb.append(set(opts, MainOptions.FTINDEX)).append(';');
    sb.append(set(opts, MainOptions.STEMMING)).append(';');
    sb.append(set(opts, MainOptions.DIACRITICS)).append(';');
    sb.append(set(opts, MainOptions.CASESENS));
    return sb.toString();
  }

  /**
   * Returns a flag string.
   * @param opts options
   * @param option option
   * @return string
   */
  private static String set(final Options opts, final BooleanOption option) {
    return new Set(option, opts.get(option)).toString();
  }
}
