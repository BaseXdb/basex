package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.array.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class ArrayJoin extends ArrayFn {
  /** Item evaluation flag. */
  private boolean item;

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // if possible, retrieve single item
    final Expr ex = exprs[0];
    if(item) {
      final Item it = ex.item(qc, info);
      return it == null ? Array.empty() : toArray(it);
    }

    final Iter iter = qc.iter(ex);
    Item it = iter.next();
    if(it == null) return Array.empty();
    final Array fst = toArray(it);
    it = iter.next();
    if(it == null) return fst;
    final Array snd = toArray(it);
    it = iter.next();
    if(it == null) return fst.concat(snd);

    final ArrayBuilder builder = new ArrayBuilder().append(fst).append(snd);
    do {
      qc.checkStop();
      builder.append(toArray(it));
    } while((it = iter.next()) != null);
    return builder.freeze();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final SeqType st = exprs[0].seqType();
    final Type t = st.type;
    if(t instanceof ArrayType) seqType = t.seqType();
    item = st.zeroOrOne();
    return this;
  }
}
