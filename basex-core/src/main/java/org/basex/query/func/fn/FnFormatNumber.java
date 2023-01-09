package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
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
    Item value = exprs[0].atomItem(qc, info);
    if(value == Empty.VALUE) value = Dbl.NAN;
    else if(value.type.isUntyped()) value = Dbl.get(value.dbl(info));
    else if(!value.type.isNumberOrUntyped()) throw numberError(this, value);

    // retrieve picture
    final byte[] picture = toToken(exprs[1], qc);
    // retrieve format declaration
    QNm form = QNm.EMPTY;
    if(exprs.length == 3) {
      final byte[] name = toTokenOrNull(exprs[2], qc);
      if(name != null) {
        try {
          form = QNm.resolve(trim(name), sc);
        } catch(final QueryException ex) {
          Util.debug(ex);
          throw FORMNUM_X.get(info, name);
        }
      }
    }
    final DecFormatter df = sc.decFormat(form.id());
    if(df == null) throw FORMNUM_X.get(info, form.prefixId(XML));

    return Str.get(df.format((ANum) value, picture, info));
  }
}
