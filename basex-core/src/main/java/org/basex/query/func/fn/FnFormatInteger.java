package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnFormatInteger extends StandardFunc {
  /** Pattern cache. */
  private final TokenObjMap<IntFormat> formats = new TokenObjMap<>();

  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item value = arg(0).atomItem(qc, info);
    final byte[] picture = toToken(arg(1), qc);
    final byte[] language = toZeroToken(arg(2), qc);
    if(value.isEmpty()) return Str.EMPTY;

    final long number = toLong(value);
    IntFormat format;

    synchronized(formats) {
      format = formats.get(picture);
      if(format == null) {
        format = new IntFormat(picture, info);
        formats.put(picture, format);
      }
    }
    return Str.get(Formatter.get(language).formatInt(number, format));
  }
}
