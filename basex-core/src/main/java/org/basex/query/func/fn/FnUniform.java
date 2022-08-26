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
    final Iter values = exprs[0].atomIter(qc, info);
    final Collation coll = toCollation(1, qc);

    final Item first = values.next();
    if(first != null) {
      for(Item item; (item = qc.next(values)) != null;) {
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
    final Expr values = exprs[0];
    if(exprs.length == 1) {
      final SeqType st = values.seqType();
      final AtomType type = st.type.atomic();
      if(st.zero() || st.zeroOrOne() && type != null && !st.mayBeArray())
        return cc.merge(values, Bln.TRUE, info);

      // uniform(1 to 10)  ->  false
      if(values instanceof RangeSeq) return Bln.FALSE;
      // uniform(reverse($data))  ->  uniform($data)
      if(REVERSE.is(values) || SORT.is(values) ||
          REPLICATE.is(values) && values.arg(1) instanceof Int) {
        final Expr[] args = exprs.clone();
        args[0] = args[0].arg(0);
        return cc.function(UNIFORM, info, args);
      }
    }
    return this;
  }
}
