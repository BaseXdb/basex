package org.basex.query.func.util;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class UtilIntersperse extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final Value sep = exprs[1].value(qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    Item item = iter.next();
    if(item != null) {
      vb.add(item);
      while((item = iter.next()) != null) vb.add(sep).add(item);
    }
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    if(st1.zeroOrOne() || expr2 == Empty.VALUE) return expr1;

    final long size1 = expr1.size(), size2 = expr2.size();
    final long sz = size1 != -1 && size2 != -1 ? size1 + (size2 * (size1 - 1)) : -1;
    exprType.assign(st1.union(st2), st1.occ, sz);

    final Data data1 = expr1.data(), data2 = expr2.data();
    if(data1 != null && data1 == data2) data(data1);

    return this;
  }
}
