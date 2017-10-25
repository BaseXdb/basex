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
 * @author BaseX Team 2005-17, BSD License
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

    final Item it = exprs[0].atomItem(qc, info);
    final byte[] pic = toEmptyToken(exprs[1], qc);

    final boolean ext = el == 5;
    final byte[] lng = ext ? toEmptyToken(exprs[2], qc) : EMPTY;
    byte[] cal = null;
    if(ext) {
      cal = toTokenOrNull(exprs[3], qc);
      if(cal != null) cal = trim(cal);
    }
    final byte[] plc = ext ? toEmptyToken(exprs[4], qc) : EMPTY;
    if(it == null) return null;

    final ADate date = (ADate) checkType(it, tp);
    final Formatter form = Formatter.get(lng);
    return Str.get(form.formatDate(date, lng, pic, cal, plc, info, sc));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return optFirst();
  }
}
