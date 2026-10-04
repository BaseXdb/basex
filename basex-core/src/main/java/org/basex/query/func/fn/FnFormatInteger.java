package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnFormatInteger extends StandardFunc {
  /** Pattern cache. */
  private final TokenObjectMap<IntFormat> formats = new TokenObjectMap<>();

  @Override
  public Str value(final QueryContext qc) throws QueryException {
    final Long value = toLongOrNull(arg(0), qc);
    final byte[] picture = toToken(arg(1), qc);
    final byte[] language = toZeroToken(arg(2), qc);
    if(value == null) return Str.EMPTY;

    final long number = value;
    IntFormat format;

    synchronized(formats) {
      format = formats.get(picture);
      if(format == null) {
        format = new IntFormat(picture, info);
        formats.put(picture, format);
      }
    }
    // the absolute value of the minimum integer can only be represented with digits
    if(number == Long.MIN_VALUE && !format.isDigitFormat()) throw RANGE_X.get(info, number);
    return Str.get(Formatter.get(language).formatInt(number, format));
  }
}
