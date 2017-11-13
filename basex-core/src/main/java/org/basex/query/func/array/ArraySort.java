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
    final ValueList vl = new ValueList((int) Math.min(Integer.MAX_VALUE, sz));
    final FItem key = exprs.length > 2 ? checkArity(exprs[2], 1, qc) : null;
    for(final Value value : array.members()) {
      vl.add((key == null ? value : key.invokeValue(qc, info, value)).atomValue(info));
    }

    final ArrayBuilder builder = new ArrayBuilder();
    for(final int order : FnSort.sort(vl, this, coll)) builder.append(array.get(order));
    return builder.freeze();
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Type t = exprs[0].seqType().type;
    if(t instanceof ArrayType) exprType.assign(t);
    return this;
  }
}
