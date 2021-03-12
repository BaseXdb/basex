package org.basex.query.func.array;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ArraySort extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final XQArray array = toArray(exprs[0], qc);
    Collation coll = sc.collation;
    if(exprs.length > 1) {
      final byte[] token = toTokenOrNull(exprs[1], qc);
      if(token != null) coll = Collation.get(token, qc, sc, info, WHICHCOLL_X);
    }
    final FItem key = exprs.length > 2 ? checkArity(exprs[2], 1, qc) : null;

    final ValueList values = new ValueList(array.arraySize());
    for(final Value value : array.members()) {
      values.add((key == null ? value : key.invoke(qc, info, value)).atomValue(qc, info));
    }

    final ArrayBuilder builder = new ArrayBuilder();
    for(final int order : FnSort.sort(values, this, coll, qc)) builder.append(array.get(order));
    return builder.freeze();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0];
    if(expr1 == XQArray.empty()) return expr1;

    final SeqType st1 = exprs[0].seqType();
    final Type type1 = st1.type;

    if(type1 instanceof ArrayType) {
      if(exprs.length == 3) {
        exprs[2] = coerceFunc(exprs[2], cc, SeqType.ANY_ATOMIC_TYPE_ZM,
            ((ArrayType) type1).declType);
      }
      exprType.assign(type1);
    }
    return this;
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.HOF.in(flags) && exprs.length > 2 || super.has(flags);
  }
}
