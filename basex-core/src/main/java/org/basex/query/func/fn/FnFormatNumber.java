package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnFormatNumber extends StandardFunc {
  @Override
  public Str value(final QueryContext qc) throws QueryException {
    final Expr value = arg(0);
    Item number = value.atomItem(qc, info);
    final byte[] picture = toToken(arg(1), qc);
    final Item options = arg(2).unwrappedItem(qc, info);

    // check input
    final Type type = number.type;
    if(number.isEmpty()) number = Dbl.NAN;
    else if(type.isUntyped()) number = Dbl.get(number.dbl(info));
    else if(!type.isNumberOrUntyped()) throw argTypeError(number, BasicType.NUMERIC, value);

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
      df = sc().decFormat(name != null ? QNm.parse(trim(token(name)), qc, sc()) : QNm.EMPTY, info);
    } catch(final QueryException ex) {
      Util.debug(ex);
    }
    if(df == null) throw FORMATWHICH_X.get(info, name);

    // enrich formatter, based on options
    if(dfo != null) {
      try {
        df = new DecFormatter(toOptions(options, new DecFormatOptions(df.options()), qc), info);
      } catch(final QueryException ex) {
        throw FORMATINV_X.get(info, ex.getLocalizedMessage()).cause(ex);
      }
    }

    return Str.get(df.format((ANum) number, picture, info));
  }
}
