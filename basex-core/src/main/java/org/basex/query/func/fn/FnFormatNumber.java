package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
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
    if(value.isEmpty()) value = Dbl.NAN;
    else if(value.type.isUntyped()) value = Dbl.get(value.dbl(info));
    else if(!value.type.isNumberOrUntyped()) throw numberError(this, value);

    // retrieve picture
    final byte[] picture = toToken(arg(1), qc);
    // retrieve format declaration
    QNm format = QNm.EMPTY;
    final Item name = arg(2).atomItem(qc, info);
    if(name instanceof QNm) {
      format = toQNm(name);
    } else if(!name.isEmpty()) {
      try {
        format = QNm.resolve(trim(toToken(name)), sc);
      } catch(final QueryException ex) {
        Util.debug(ex);
        throw FORMNUM_X.get(info, name);
      }
    }
    final DecFormatter df = sc.decFormat(format.id());
    if(df == null) throw FORMNUM_X.get(info, format.prefixId(XML));

    return Str.get(df.format((ANum) value, picture, info));
  }
}
