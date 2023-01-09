package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class ArrayJoin extends ArrayFn {
  @Override
  public XQArray item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // if possible, retrieve single item
    final Expr arrays = exprs[0];
    if(arrays.seqType().zeroOrOne()) {
      final Item item = arrays.item(qc, info);
      return item == Empty.VALUE ? XQArray.empty() : toArray(item);
    }

    final Iter iter = arrays.iter(qc);
    Item item = iter.next();
    if(item == null) return XQArray.empty();

    final XQArray first = toArray(item);
    item = iter.next();
    if(item == null) return first;

    final XQArray second = toArray(item);
    item = iter.next();
    if(item == null) return first.concat(second);

    final ArrayBuilder ab = new ArrayBuilder().append(first).append(second);
    do {
      ab.append(toArray(item));
    } while((item = qc.next(iter)) != null);
    return ab.array(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr arrays = exprs[0];
    if(arrays.seqType().type instanceof ArrayType) {
      // remove empty entries
      final Expr[] args = arrays.args();
      if(arrays instanceof List && ((Checks<Expr>) arg -> arg == XQArray.empty()).any(args)) {
        final ExprList list = new ExprList();
        for(final Expr arg : args) {
          if(arg != XQArray.empty()) list.add(arg);
        }
        exprs[0] = List.get(cc, info, list.finish());
      }
      // return simple arguments
      final SeqType st = exprs[0].seqType();
      if(st.one()) return exprs[0];

      exprType.assign(st.type);
    }
    return this;
  }
}
