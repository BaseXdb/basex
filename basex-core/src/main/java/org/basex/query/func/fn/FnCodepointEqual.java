package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnCodepointEqual extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Boolean equal = equal(qc);
    return equal != null ? Bln.get(equal) : Empty.VALUE;
  }

  @Override
  protected boolean ebv(final QueryContext qc) throws QueryException {
    final Boolean equal = equal(qc);
    return equal != null && equal;
  }

  /**
   * Compares the two values.
   * @param qc query context
   * @return result of check, or {@code null} if one input is an empty sequence
   * @throws QueryException query exception
   */
  private Boolean equal(final QueryContext qc) throws QueryException {
    final byte[] value1 = toTokenOrNull(arg(0), qc);
    if(value1 == null) return null;
    final byte[] value2 = toTokenOrNull(arg(1), qc);
    if(value2 == null) return null;
    return eq(value1, value2);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // codepoint-equal((), $b) → (), codepoint-equal($a, ()) → ()
    return optPair(arg(0), arg(1), cc);
  }
}
