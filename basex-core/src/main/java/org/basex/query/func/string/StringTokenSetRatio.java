package org.basex.query.func.string;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.similarity.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StringTokenSetRatio extends StringFn {
  @Override
  protected Dbl item(final QueryContext qc) throws QueryException {
    final byte[] value1 = toToken(arg(0), qc), value2 = toToken(arg(1), qc);
    final FTOpt opt = ftOpt(2, qc);

    checkLength(Token.length(value1));
    checkLength(Token.length(value2));
    return Dbl.get(TokenRatio.set(tokens(value1, opt), tokens(value2, opt)));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    optOptions(2, StringOptions::new, cc);
    return this;
  }
}
