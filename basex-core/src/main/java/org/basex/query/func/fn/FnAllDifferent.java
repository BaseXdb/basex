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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnAllDifferent extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter values = arg(0).atomIter(qc, info);
    final Collation collation = toCollation(arg(1), qc);

    final ItemSet set = CollationItemSet.get(collation, info);
    for(Item item; (item = qc.next(values)) != null;) {
      if(!set.add(item)) return Bln.FALSE;
    }
    return Bln.TRUE;
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    arg(0, arg -> arg.simplifyFor(Simplify.DATA, cc));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr values = arg(0);
    if(!defined(1)) {
      final SeqType st = values.seqType();
      final AtomType type = st.type.atomic();
      if(st.zero() || st.zeroOrOne() && type != null && !st.mayBeArray())
        return cc.merge(values, Bln.TRUE, info);

      // all-different(1 to 10)  ->  true
      if(values instanceof RangeSeq) return Bln.TRUE;
      // all-different(reverse($data))  ->  all-different($data)
      if(REVERSE.is(values) || SORT.is(values)) {
        final Expr[] args = exprs.clone();
        args[0] = args[0].arg(0);
        return cc.function(ALL_DIFFERENT, info, args);
      }
    }
    return this;
  }
}
