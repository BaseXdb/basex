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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ArrayJoin extends ArrayFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // if possible, retrieve single item
    final Expr expr = exprs[0];
    if(expr.seqType().zeroOrOne()) {
      final Item item = expr.item(qc, info);
      return item == Empty.VALUE ? XQArray.empty() : toArray(item);
    }

    final Iter iter = expr.iter(qc);
    Item item = iter.next();
    if(item == null) return XQArray.empty();
    final XQArray fst = toArray(item);
    item = iter.next();
    if(item == null) return fst;
    final XQArray snd = toArray(item);
    item = iter.next();
    if(item == null) return fst.concat(snd);

    final ArrayBuilder builder = new ArrayBuilder();
    builder.append(fst);
    builder.append(snd);
    do {
      builder.append(toArray(item));
    } while((item = qc.next(iter)) != null);
    return builder.freeze();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    if(exprs[0].seqType().type instanceof ArrayType) {
      // remove empty entries
      if(exprs[0] instanceof List &&
          ((Checks<Expr>) arg -> arg == XQArray.empty()).any(exprs[0].args())) {
        final ExprList list = new ExprList();
        for(final Expr arg : exprs[0].args()) if(arg != XQArray.empty()) list.add(arg);
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
