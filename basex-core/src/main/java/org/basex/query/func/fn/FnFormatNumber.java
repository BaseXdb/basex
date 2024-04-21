package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnFormatNumber extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    Item value = arg(0).atomItem(qc, info);
    final byte[] picture = toToken(arg(1), qc);
    final Item options = arg(2).item(qc, info);

    // check input
    final Type type = value.type;
    if(value.isEmpty()) value = Dbl.NAN;
    else if(type.isUntyped()) value = Dbl.get(value.dbl(info));
    else if(!type.isNumberOrUntyped()) throw numberError(this, value);

    // find decimal-format name
    DecFormatOptions dfo = null;
    final String name;
    if(options instanceof XQMap) {
      dfo = toOptions(options, new DecFormatOptions(), qc);
      name = dfo.get(DecFormatOptions.FORMAT_NAME);
    } else {
      name = toStringOrNull(options, qc);
    }

    // create formatter, based on decimal-format name
    DecFormatter df = null;
    try {
      df = sc().decFormat(name != null ? QNm.parse(trim(token(name)), sc()) : QNm.EMPTY, info);
    } catch(final QueryException ex) {
      Util.debug(ex);
    }
    if(df == null) throw FORMATWHICH_X.get(info, name);

    // enrich formatter, based on options
    if(dfo != null) {
      try {
        df = new DecFormatter(toOptions(options, df.options(), qc), info);
      } catch(final QueryException ex) {
        Util.debug(ex);
        throw FORMATINV_X.get(info, ex.getLocalizedMessage());
      }
    }

    return Str.get(df.format((ANum) value, picture, info));
  }
}
