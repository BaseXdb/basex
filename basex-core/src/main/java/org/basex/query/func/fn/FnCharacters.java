package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public class FnCharacters extends StandardFunc {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final byte[] token = toZeroToken(exprs[0], qc);
    final int tl = token.length;
    if(tl == 0) return Empty.ITER;

    if(ascii(token)) {
      return new BasicIter<Str>(tl) {
        @Override
        public Str get(final long i) {
          return Str.get(new byte[] { token[(int) i] });
        }
      };
    }

    return new Iter() {
      int t;

      @Override
      public Str next() {
        if(t == tl) return null;
        final int e = t + cl(token, t);
        final byte[] string = Arrays.copyOfRange(token, t, e);
        t = e;
        return Str.get(string);
      }
    };
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final byte[] token = toZeroToken(exprs[0], qc);
    final int tl = token.length;
    if(tl == 0) return Empty.VALUE;

    final TokenList list = new TokenList(tl);
    if(ascii(token)) {
      for(final byte b : token) list.add(new byte[] { b });
    } else {
      for(int t = 0; t < tl;) {
        final int e = t + cl(token, t);
        final byte[] string = Arrays.copyOfRange(token, t, e);
        t = e;
        list.add(string);
      }
    }
    return StrSeq.get(list);
  }
}
