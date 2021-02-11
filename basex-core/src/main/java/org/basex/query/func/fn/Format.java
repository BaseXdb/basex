package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.list.*;

/**
 * Formatting functions.
 *
 * @author BaseX Team 2005-21, BSD License
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
    final int el = exprs.length;
    if(el == 3 || el == 4) throw Functions.wrongArity(definition, el, new IntList(), info);

    final Item item = exprs[0].atomItem(qc, info);
    if(item == Empty.VALUE) return Empty.VALUE;

    final byte[] picture = toZeroToken(exprs[1], qc);

    final boolean more = el == 5;
    final byte[] language = more ? toZeroToken(exprs[2], qc) : EMPTY;
    byte[] calendar = null;
    if(more) {
      calendar = toTokenOrNull(exprs[3], qc);
      if(calendar != null) calendar = trim(calendar);
    }
    final byte[] place = more ? toZeroToken(exprs[4], qc) : EMPTY;

    final ADate date = toDate(item, tp, qc);
    final Formatter form = Formatter.get(language);
    return Str.get(form.formatDate(date, language, picture, calendar, place, sc, info));
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
