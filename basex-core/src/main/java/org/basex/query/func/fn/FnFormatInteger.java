package org.basex.query.func.fn;

import static org.basex.util.Token.*;

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
  /** Format key separator. */
  private static final byte[] SEPARATOR = token(";");
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

    byte[] key = concat(picture, SEPARATOR, language);
    synchronized(formats) {
      format = formats.get(key);
      if(format == null) {
        format = new IntFormat(picture, language, info);
        formats.put(key, format);
      }
    }
    return Str.get(Formatter.get(language).formatInt(number, format));
  }
}
