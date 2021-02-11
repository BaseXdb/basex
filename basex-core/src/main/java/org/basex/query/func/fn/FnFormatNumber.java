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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnFormatNumber extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // evaluate arguments
    Item item = exprs[0].atomItem(qc, info);
    if(item == Empty.VALUE) item = Dbl.NAN;
    else if(item.type.isUntyped()) item = Dbl.get(item.dbl(info));
    else if(!item.type.isNumberOrUntyped()) throw numberError(this, item);

    // retrieve picture
    final byte[] pic = toToken(exprs[1], qc);
    // retrieve format declaration
    QNm form = QNm.EMPTY;
    if(exprs.length == 3) {
      final byte[] qnm = toTokenOrNull(exprs[2], qc);
      if(qnm != null) {
        try {
          form = QNm.resolve(trim(qnm), sc);
        } catch(final QueryException ex) {
          throw FORMNUM_X.get(info, qnm);
        }
      }
    }
    final DecFormatter df = sc.decFormat(form.id());
    if(df == null) throw FORMNUM_X.get(info, form.prefixId(XML));

    return Str.get(df.format((ANum) item, pic, info));
  }
}
