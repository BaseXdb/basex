package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class FnCount extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // if possible, retrieve single item
    final Expr expr = exprs[0];
    if(expr.seqType().zeroOrOne()) return expr.item(qc, info) == null ? Int.ZERO : Int.ONE;

    // iterative access: if the iterator size is unknown, iterate through all results
    final Iter iter = expr.iter(qc);
    long size = iter.size();
    if(size == -1) {
      do ++size; while(qc.next(iter) != null);
    }
    return Int.get(size);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    Expr expr = exprs[0];

    // rewrite count(map:keys(...)) to map:size(...)
    if(Function._MAP_KEYS.is(expr))
      return cc.function(Function._MAP_SIZE, info, args(expr));
    // rewrite count(string-to-codepoints(...)) to string-length(...)
    if(Function.STRING_TO_CODEPOINTS.is(expr) || Function._UTIL_CHARS.is(expr))
      return cc.function(Function.STRING_LENGTH, info, args(expr));
    // rewrite count(reverse(...)) to count(...)
    if(Function.REVERSE.is(expr))
      expr = args(expr)[0];

    // return statically known size (ignore non-deterministic expressions, e.g. count(error()))
    if(!expr.has(Flag.NDT)) {
      final long size = expr.size();
      if(size >= 0) return Int.get(size);
    }

    exprs[0] = expr;
    return this;
  }
}
