package org.basex.query.func.array;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
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
public final class ArraySort extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Array array = toArray(exprs[0], qc);
    Collation coll = sc.collation;
    if(exprs.length > 1) {
      final byte[] tok = toTokenOrNull(exprs[1], qc);
      if(tok != null) coll = Collation.get(tok, qc, sc, info, WHICHCOLL_X);
    }

    final long sz = array.arraySize();
    final ValueList vl = new ValueList(sz);
    final FItem key = exprs.length > 2 ? checkArity(exprs[2], 1, qc) : null;
    for(final Value value : array.members()) {
      vl.add((key == null ? value : key.invokeValue(qc, info, value)).atomValue(qc, info));
    }

    final ArrayBuilder builder = new ArrayBuilder();
    for(final int order : FnSort.sort(vl, this, coll, qc)) builder.append(array.get(order));
    return builder.freeze();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final SeqType st1 = exprs[0].seqType();
    final Type t1 = st1.type;

    if(t1 instanceof ArrayType) {
      if(exprs.length == 3) coerceFunc(2, cc, SeqType.AAT_ZM, ((ArrayType) t1).declType);
      exprType.assign(t1);
    }
    return this;
  }
}
