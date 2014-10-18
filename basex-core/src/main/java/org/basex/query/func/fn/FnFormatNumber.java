package org.basex.query.func.fn;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.format.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnFormatNumber extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // evaluate arguments
    Item it = exprs[0].atomItem(qc, info);
    if(it == null) it = Dbl.NAN;
    else if(it.type.isUntyped()) it = Dbl.get(it.dbl(ii));
    else if(!it.type.isNumberOrUntyped()) throw numberError(this, it);

    // retrieve picture
    final byte[] pic = toToken(exprs[1], qc);
    // retrieve format declaration
    final QNm frm = exprs.length == 3 ? new QNm(trim(toEmptyToken(exprs[2], qc)), sc) :
      new QNm(EMPTY);
    final DecFormatter df = sc.decFormats.get(frm.id());
    if(df == null) throw FORMNUM_X.get(info, frm.prefixId(XML));

    return Str.get(df.format(info, (ANum) it, pic));
  }
}
