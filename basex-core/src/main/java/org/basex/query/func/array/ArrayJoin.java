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
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // if possible, retrieve single item
    final Expr expr = exprs[0];
    if(expr.seqType().zeroOrOne()) {
      final Item item = expr.item(qc, info);
      return item == null ? Array.empty() : toArray(item);
    }

    final Iter iter = expr.iter(qc);
    Item item = iter.next();
    if(item == null) return Array.empty();
    final Array fst = toArray(item);
    item = iter.next();
    if(item == null) return fst;
    final Array snd = toArray(item);
    item = iter.next();
    if(item == null) return fst.concat(snd);

    final ArrayBuilder builder = new ArrayBuilder().append(fst).append(snd);
    do {
      builder.append(toArray(item));
    } while((item = qc.next(iter)) != null);
    return builder.freeze();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type t = exprs[0].seqType().type;
    if(t instanceof ArrayType) exprType.assign(t);
    return this;
  }
}
