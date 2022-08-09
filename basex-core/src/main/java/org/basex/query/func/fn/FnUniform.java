package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
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
public final class FnUniform extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Collation coll = toCollation(1, qc);
    final Expr expr = exprs[0];
    final Iter iter = expr.atomIter(qc, info);

    final Item first = iter.next();
    if(first != null) {
      for(Item item; (item = iter.next()) != null;) {
        if(!item.equiv(first, coll, info)) return Bln.FALSE;
      }
    }
    return Bln.TRUE;
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    exprs[0] = exprs[0].simplifyFor(Simplify.DATA, cc).simplifyFor(Simplify.DISTINCT, cc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0];
    if(exprs.length == 1) {
      final SeqType st = expr.seqType();
      final AtomType type = st.type.atomic();
      if(st.zero() || st.zeroOrOne() && type != null && !st.mayBeArray())
        return cc.merge(expr, Bln.TRUE, info);

      // uniform(1 to 10)  ->  false
      if(expr instanceof RangeSeq) return Bln.FALSE;
      // uniform(reverse($data))  ->  uniform($data)
      if(REVERSE.is(expr) || SORT.is(expr) || REPLICATE.is(expr) && expr.arg(1) instanceof Int) {
        final Expr[] args = exprs.clone();
        args[0] = args[0].arg(0);
        return cc.function(UNIFORM, info, args);
      }
    }
    return this;
  }
}
