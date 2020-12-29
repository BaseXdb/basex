package org.basex.query.func.util;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class UtilWithin extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final long min = toLong(exprs[1], qc), max = exprs.length == 2 ? Long.MAX_VALUE :
      exprs[1] == exprs[2] ? min : toLong(exprs[2], qc);

    // iterative access: if the iterator size is unknown, iterate through results
    final Iter iter = exprs[0].iter(qc);
    long size = iter.size();
    if(size == -1) {
      // no check for maximum value: skip if minimum size is reached
      do ++size; while((max < Long.MAX_VALUE ? size <= max : size < min) && qc.next(iter) != null);
    }
    return Bln.get(size >= min && size <= max);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1], expr3 = exprs.length > 2 ? exprs[2] : Int.MAX;
    final long min = expr2 instanceof Value ? toLong(expr2, cc.qc) : Long.MIN_VALUE;
    final long max = expr3 instanceof Value ? toLong(expr3, cc.qc) : Long.MIN_VALUE;

    // return statically known size (ignore non-deterministic expressions, e.g. count(error()))
    if(min != Long.MIN_VALUE && max != Long.MIN_VALUE) {
      if(min > max) return Bln.FALSE;
      if(min <= 0 && max == Long.MAX_VALUE) return Bln.TRUE;

      final long size = expr1.size();
      if(size >= 0 && !expr1.has(Flag.NDT)) return Bln.get(size >= min && size <= max);

      if(max == 0) return cc.function(EMPTY, info, expr1);
      if(min == 1 && max == Long.MAX_VALUE) return cc.function(EXISTS, info, expr1);

      if(expr1.seqType().zeroOrOne()) {
        if(min < 1 && max <= 1) return Bln.TRUE;
        if(min >= 2) return Bln.FALSE;
      }
    }

    // simplify argument
    final Expr arg = FnCount.simplify(expr1, cc);
    if(arg != expr1) {
      final Expr[] args = exprs.clone();
      args[0] = arg;
      return cc.function(_UTIL_WITHIN, info, args);
    }
    return this;
  }
}
