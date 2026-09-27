package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnIndexOfSubstring extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc), substring = toToken(arg(1), qc);
    final int vl = value.length;

    return new Iter() {
      // byte offset and character position at which the next search starts
      int b;
      long pos = 1;

      @Override
      public Item next() {
        final int i = b > vl ? -1 : indexOf(value, substring, b);
        if(i == -1) {
          b = vl + 1;
          return null;
        }
        for(; b < i; b += cl(value, b)) pos++;
        final Item item = Itr.get(pos++);
        // continue after the first character of the match: occurrences may overlap
        b += b < vl ? cl(value, b) : 1;
        return item;
      }
    };
  }
}
