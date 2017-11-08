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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnFormatNumber extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // evaluate arguments
    Item it = exprs[0].atomItem(qc, info);
    if(it == null) it = Dbl.NAN;
    else if(it.type.isUntyped()) it = Dbl.get(it.dbl(info));
    else if(!it.type.isNumberOrUntyped()) throw numberError(this, it);

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

    return Str.get(df.format(info, (ANum) it, pic));
  }
}
