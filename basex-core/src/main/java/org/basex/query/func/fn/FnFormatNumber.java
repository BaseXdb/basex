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
    final Item name = arg(2).atomItem(qc, info);
    final Item format = arg(3).item(qc, info);

    final Type type = value.type;
    if(value.isEmpty()) value = Dbl.NAN;
    else if(type.isUntyped()) value = Dbl.get(value.dbl(info));
    else if(!type.isNumberOrUntyped()) throw numberError(this, value);

    final DecFormatter df;
    if(format.isEmpty()) {
      QNm qnm = QNm.EMPTY;
      if(name instanceof QNm) {
        qnm = toQNm(name);
      } else if(!name.isEmpty()) {
        try {
          qnm = QNm.parse(trim(toToken(name)), sc);
        } catch(final QueryException ex) {
          Util.debug(ex);
          throw FORMNUM_X.get(info, name);
        }
      }
      df = sc.decFormat(qnm);
      if(df == null) throw FORMNUM_X.get(info, qnm.prefixId(XML));
    } else if(name.isEmpty()) {
      df = new DecFormatter(toOptions(format, new DecFormatOptions(), true, qc), info);
    } else {
      throw FORMDUP_X.get(info, name);
    }

    return Str.get(df.format((ANum) value, picture, info));
  }
}
