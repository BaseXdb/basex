package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FnCharacters extends StandardFunc {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final int vl = value.length;
    if(vl == 0) return Empty.ITER;

    if(ascii(value)) {
      return new BasicIter<Str>(vl) {
        @Override
        public Str get(final long i) {
          return Str.get(new byte[] { value[(int) i] });
        }
        @Override
        public Value value(final QueryContext q, final Expr expr) {
          final TokenList tl = new TokenList((int) size);
          for(final byte b : value) tl.add(new byte[] { b });
          return StrSeq.get(tl);
        }
      };
    }

    return new Iter() {
      int t;

      @Override
      public Str next() {
        if(t == vl) return null;
        final int e = t + cl(value, t);
        final byte[] string = Arrays.copyOfRange(value, t, e);
        t = e;
        return Str.get(string);
      }
    };
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final int vl = value.length;
    if(vl == 0) return Empty.VALUE;

    final TokenList list = new TokenList(vl);
    if(ascii(value)) {
      for(final byte b : value) list.add(new byte[] { b });
    } else {
      for(int t = 0; t < vl;) {
        final int e = t + cl(value, t);
        final byte[] string = Arrays.copyOfRange(value, t, e);
        t = e;
        list.add(string);
      }
    }
    return StrSeq.get(list);
  }
}
