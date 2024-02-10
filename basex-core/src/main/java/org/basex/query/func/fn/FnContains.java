package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnContains extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final byte[] substring = toZeroToken(arg(1), qc);
    final Collation collation = toCollation(arg(2), qc);

    return Bln.get(collation == null ? Token.contains(value, substring) :
      collation.contains(value, substring, info));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr value = arg(0), substring = arg(1);
    // contains($a, ''), contains($a, $a)
    if(!defined(2) && value.seqType().type.isStringOrUntyped() && !value.has(Flag.NDT)) {
      if(substring == Empty.VALUE || substring == Str.EMPTY || value.equals(substring))
        return Bln.TRUE;
    }
    return this;
  }
}
