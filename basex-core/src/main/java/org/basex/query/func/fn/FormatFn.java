package org.basex.query.func.fn;

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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class FormatFn extends StandardFunc {
  /**
   * Returns a formatted date.
   * @param tp input type
   * @param qc query context
   * @return string or {@link Empty#VALUE}
   * @throws QueryException query exception
   */
  final Item formatDate(final BasicType tp, final QueryContext qc) throws QueryException {
    final Expr value = arg(0);
    final Item item = atomItem(value, qc);
    if(item == Empty.VALUE) return Empty.VALUE;

    final byte[] picture = toToken(arg(1), qc);
    final byte[] language = toZeroToken(arg(2), qc);
    final byte[] calendar = toTokenOrNull(arg(3), qc);
    final byte[] place = toZeroToken(arg(4), qc);

    final ADate date = (ADate) coerce(item, tp, value, qc);
    final DatePicture pic = qc.globalCaches.datePicture(picture, info);
    return Str.get(Formatter.get(language).formatDate(date, language, pic, calendar, place, info));
  }

  @Override
  protected final Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
