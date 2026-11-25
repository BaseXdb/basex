package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnFormatNumber extends StandardFunc {
  /** Formatter for static options (can be {@code null}). */
  private volatile DecFormatter formatter;

  @Override
  public Str value(final QueryContext qc) throws QueryException {
    final Expr value = arg(0);
    Item number = atomItem(value, qc);
    final byte[] picture = toToken(arg(1), qc);
    final DecFormatOptions options = toOptions(qc);

    // check input
    number = number.isEmpty() ? Dbl.NAN : toNumber(number, value);

    DecFormatter df = formatter;
    if(df == null) {
      df = formatter(options, qc);
      if(staticOptions() != null) formatter = df;
    }
    return Str.get(df.format((ANum) number, picture, info));
  }

  /**
   * Creates a formatter.
   * @param options options
   * @param qc query context
   * @return formatter
   * @throws QueryException query exception
   */
  private DecFormatter formatter(final DecFormatOptions options, final QueryContext qc)
      throws QueryException {
    // create formatter, based on decimal-format name
    final String name = options.get(DecFormatOptions.FORMAT_NAME);
    DecFormatter df = null;
    try {
      df = sc().decFormat(name != null ? QNm.parse(trim(token(name)), qc, sc()) : QNm.EMPTY, info);
    } catch(final QueryException ex) {
      Util.debug(ex);
    }
    if(df == null) throw FORMATWHICH_X.get(info, name);

    // enrich formatter, based on supplied options
    DecFormatOptions dfo = null;
    for(final Option<?> option : options) {
      if(option == DecFormatOptions.FORMAT_NAME || !options.contains(option)) continue;
      if(dfo == null) dfo = df.options().copy();
      dfo.put(option, options.get(option));
    }
    if(dfo != null) {
      try {
        df = new DecFormatter(dfo, info);
      } catch(final QueryException ex) {
        throw FORMATINV_X.get(info, ex.getLocalizedMessage()).cause(ex);
      }
    }
    return df;
  }
}
