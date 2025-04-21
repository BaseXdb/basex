package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UtilValuesExcept extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter values = arg(0).atomIter(qc, info);
    final Iter except = arg(1).atomIter(qc, info);
    final Collation collation = toCollation(arg(2), qc);

    final IntSet ints = new IntSet();
    final ItemSet items = ItemSet.get(collation, info);
    for(Item item = null; (item = qc.next(except)) != null;) {
      final int v = FnDuplicateValues.toInt(item);
      if(v != Integer.MIN_VALUE) ints.add(v);
      else items.add(item);
    }
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        for(Item item = null; (item = qc.next(values)) != null;) {
          if(!items.contains(item)) {
            if(item instanceof ANum) {
              final double d = item.dbl(null);
              final int i = (int) d;
              if(d == i && ints.contains(i)) continue;
            }
            return item;
          }
        }
        return null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    super.simplifyArgs(cc);
    if(!defined(2)) {
      final QueryFunction<Expr, Expr> rewrite = arg -> arg.simplifyFor(Simplify.DISTINCT, cc);
      arg(0, rewrite);
      arg(1, rewrite);
    }
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr values = arg(0), except = arg(1);
    final SeqType st = values.seqType();
    if(st.zero()) return values;

    final AtomType type = st.type.atomic();
    if(except.seqType().zero()) {
      return type == st.type ? values : cc.function(Function.DATA, info, values);
    }
    // assign atomic type of argument
    if(type != null) exprType.assign(type);
    return this;
  }
}
