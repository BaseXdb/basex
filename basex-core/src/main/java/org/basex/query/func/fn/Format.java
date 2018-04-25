package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.list.*;

/**
 * Formatting functions.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
abstract class Format extends StandardFunc {
  /**
   * Returns a formatted number.
   * @param qc query context
   * @param tp input type
   * @return string or {@code null}
   * @throws QueryException query exception
   */
  Item formatDate(final AtomType tp, final QueryContext qc) throws QueryException {
    final int el = exprs.length;
    if(el == 3 || el == 4) throw Functions.wrongArity(sig, el, new IntList(), info);

    final Item item = exprs[0].atomItem(qc, info);
    final byte[] picture = toEmptyToken(exprs[1], qc);

    final boolean more = el == 5;
    final byte[] language = more ? toEmptyToken(exprs[2], qc) : EMPTY;
    byte[] calendar = null;
    if(more) {
      calendar = toTokenOrNull(exprs[3], qc);
      if(calendar != null) calendar = trim(calendar);
    }
    final byte[] place = more ? toEmptyToken(exprs[4], qc) : EMPTY;
    if(item == null) return null;

    final ADate date = (ADate) checkType(item, tp);
    final Formatter form = Formatter.get(language);
    return Str.get(form.formatDate(date, language, picture, calendar, place, info, sc));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
