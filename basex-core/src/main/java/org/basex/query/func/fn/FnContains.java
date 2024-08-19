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
public class FnContains extends StandardFunc {
  @Override
  public final Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(test(qc, ii, 0));
  }

  @Override
  public final boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final byte[] substring = toZeroToken(arg(1), qc);
    final Collation collation = toCollation(arg(2), qc);
    return test(value, substring, collation);
  }

  /**
   * Performs the test.
   * @param value value
   * @param substring substring
   * @param collation collation (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  boolean test(final byte[] value, final byte[] substring, final Collation collation)
      throws QueryException {
    return collation == null ? Token.contains(value, substring) :
      collation.contains(value, substring, info);
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    final Expr value = arg(0), substring = arg(1);
    // contains($a, ''), contains($a, $a)
    if(!defined(2) && value.seqType().type.isStringOrUntyped() && !value.has(Flag.NDT)) {
      if(substring == Empty.VALUE || substring == Str.EMPTY || value.equals(substring))
        return Bln.TRUE;
    }
    return this;
  }
}
