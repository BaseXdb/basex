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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnFormatNumber extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // evaluate arguments
    Item value = arg(0).atomItem(qc, info);
    final Type type = value.type;
    if(value.isEmpty()) value = Dbl.NAN;
    else if(type.isUntyped()) value = Dbl.get(value.dbl(info));
    else if(!type.isNumberOrUntyped()) throw numberError(this, value);

    // retrieve picture
    final byte[] picture = toToken(arg(1), qc);
    // retrieve format name declaration
    final Item name = arg(2).atomItem(qc, info);
    // retrieve explicit format declaration
    final Item format = arg(3).item(qc, info);
    final DecFormatter df;
    if(!format.isEmpty()) {
      if(!name.isEmpty()) throw FORMDUP_X.get(info, name);
      final DecFormatOptions options = toOptions(arg(3), new DecFormatOptions(), true, qc);
      df = new DecFormatter(options.toTokenMap(), info);
    } else {
      QNm formatQNm = QNm.EMPTY;
      if(name instanceof QNm) {
        formatQNm = toQNm(name);
      } else if(!name.isEmpty()) {
        try {
          formatQNm = QNm.parse(trim(toToken(name)), sc);
        } catch(final QueryException ex) {
          Util.debug(ex);
          throw FORMNUM_X.get(info, name);
        }
      }
      df = sc.decFormat(formatQNm.internal());
      if(df == null) throw FORMNUM_X.get(info, formatQNm.prefixId(XML));
    }

    return Str.get(df.format((ANum) value, picture, info));
  }
}
