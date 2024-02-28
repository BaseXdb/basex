package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
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
    final Item formatName = arg(2).atomItem(qc, info);
    final Item format = arg(3).item(qc, info);

    final Type type = value.type;
    if(value.isEmpty()) value = Dbl.NAN;
    else if(type.isUntyped()) value = Dbl.get(value.dbl(info));
    else if(!type.isNumberOrUntyped()) throw numberError(this, value);

    QNm qnm = QNm.EMPTY;
    if(formatName instanceof QNm) {
      qnm = toQNm(formatName);
    } else if(!formatName.isEmpty()) {
      try {
        qnm = QNm.parse(trim(toToken(formatName)), sc());
      } catch(final QueryException ex) {
        Util.debug(ex);
        throw FORMATWHICH_X.get(info, formatName);
      }
    }
    DecFormatter df = sc().decFormat(qnm, info);
    if(!format.isEmpty()) {
      final DecFormatOptions options = toOptions(format,
          df != null ? df.options() : new DecFormatOptions(), true, qc);
      try {
        df = new DecFormatter(options, info);
      } catch(final QueryException ex) {
        Util.debug(ex);
        throw FORMATINV_X.get(info, ex.getLocalizedMessage());
      }
    }

    return Str.get(df.format((ANum) value, picture, info));
  }
}
