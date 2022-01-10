package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnMatches extends RegEx {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toZeroToken(exprs[0], qc), pttrn = toToken(exprs[1], qc);

    if(exprs.length < 3) {
      final int ch = patternChar(pttrn);
      if(ch != -1) return Bln.get(contains(value, ch));
    }
    final Pattern pattern = pattern(pttrn, exprs.length == 3 ? exprs[2] : null, qc, false);
    return Bln.get(pattern.matcher(string(value)).find());
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];

    final SeqType st1 = expr1.seqType();
    if(st1.zero() || st1.one() && st1.type.isStringOrUntyped()) {
      if(expr2 instanceof Str && exprs.length < 3) {
        if(expr2 == Str.EMPTY) return Bln.TRUE;
        for(final byte b : ((Str) expr2).string()) {
          if(contains(REGEX_CHARS, b)) return this;
        }
        return cc.function(CONTAINS, info, args());
      }
    }
    return this;
  }
}
