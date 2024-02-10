package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Formatting functions.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
abstract class Format extends StandardFunc {
  /**
   * Returns a formatted number.
   * @param qc query context
   * @param tp input type
   * @return string or {@link Empty#VALUE}
   * @throws QueryException query exception
   */
  final Item formatDate(final AtomType tp, final QueryContext qc) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    if(value.isEmpty()) return Empty.VALUE;

    final byte[] picture = toZeroToken(arg(1), qc);
    final byte[] language = toZeroToken(arg(2), qc);
    byte[] calendar = toTokenOrNull(arg(3), qc);
    if(calendar != null) calendar = trim(calendar);
    final byte[] place = toZeroToken(arg(4), qc);

    final ADate date = toDate(value, tp, qc);
    final Formatter form = Formatter.get(language);
    return Str.get(form.formatDate(date, language, picture, calendar, place, sc, info));
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
