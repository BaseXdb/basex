package org.basex.query.func.prof;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProfHuman extends StandardFunc {
  @Override
  public Str value(final QueryContext qc) throws QueryException {
    final Item value = toAtomItem(arg(0), qc);
    if(value instanceof final DTDur dur) {
      return Str.get(Performance.formatTime(dur.dtd().doubleValue()));
    }
    final double size = toDouble(value);
    return Str.get(Double.isFinite(size) ? Performance.formatHuman(size) :
      Token.string(Token.token(size)));
  }
}
