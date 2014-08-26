package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Formatting functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class Format extends StandardFunc {
  /**
   * Returns a formatted number.
   * @param qc query context
   * @param tp input type
   * @return string
   * @throws QueryException query exception
   */
  Item formatDate(final AtomType tp, final QueryContext qc) throws QueryException {
    final Item it = exprs[0].atomItem(qc, info);
    final byte[] pic = toEmptyToken(exprs[1], qc);
    final byte[] lng = exprs.length == 5 ? toEmptyToken(exprs[2], qc) : EMPTY;
    final byte[] cal = exprs.length == 5 ? toEmptyToken(exprs[3], qc) : EMPTY;
    final byte[] plc = exprs.length == 5 ? toEmptyToken(exprs[4], qc) : EMPTY;
    if(it == null) return null;

    final ADate date = (ADate) checkType(it, tp);
    final Formatter form = Formatter.get(lng);
    return Str.get(form.formatDate(date, lng, pic, cal, plc, info));
  }
}
