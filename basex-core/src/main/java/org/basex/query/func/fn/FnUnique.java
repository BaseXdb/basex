package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnUnique extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Collation coll = toCollation(1, qc);
    final Expr expr = exprs[0];
    final Iter iter = expr.atomIter(qc, info);

    final ItemSet set = coll == null ? new HashItemSet(false) : new CollationItemSet(coll);
    for(Item item; (item = qc.next(iter)) != null;) {
      if(!set.add(item, info)) return Bln.FALSE;
    }
    return Bln.TRUE;
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    exprs[0] = exprs[0].simplifyFor(Simplify.DATA, cc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0];
    if(exprs.length == 1) {
      final SeqType st = expr.seqType();
      final AtomType type = st.type.atomic();
      if(st.zero() || st.zeroOrOne() && type != null && !st.mayBeArray())
        return cc.merge(expr, Bln.TRUE, info);

      // unique(1 to 10)  ->  true
      if(expr instanceof RangeSeq) return Bln.TRUE;
      // unique(reverse($data))  ->  unique($data)
      if(REVERSE.is(expr) || SORT.is(expr)) {
        final Expr[] args = exprs.clone();
        args[0] = args[0].arg(0);
        return cc.function(UNIQUE, info, args);
      }
    }
    return this;
  }
}
