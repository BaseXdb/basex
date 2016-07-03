package org.basex.query.func.jobs;

import java.util.*;

import org.basex.query.func.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public abstract class JobsFn extends StandardFunc {


  /**
   * Sorts a list of job ids.
   * @param list job id
   * @return sorted list
   */
  protected static TokenList sort(final TokenList list) {
    final Comparator<byte[]> cc = new Comparator<byte[]>() {
      @Override
      public int compare(final byte[] token1, final byte[] token2) {
        final byte[] t1 = Token.substring(token1, 3), t2 = Token.substring(token2, 3);
        final long diff = Token.toLong(t1) - Token.toLong(t2);
        return diff < 0 ? -1 : diff > 0 ? 1 : 0;
      }
    };
    return list.sort(cc, true);
  }
}
