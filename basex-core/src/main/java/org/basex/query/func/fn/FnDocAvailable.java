package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.QueryError.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnDocAvailable extends Docs {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    try {
      return Bln.get(doc(qc) != null);
    } catch(final QueryException ex) {
      final QueryError error = ex.error();
      if(error != null) {
        final String num = error.code.length() == 8 ? error.code.substring(4) : "";
        if(error.is(ErrType.FODC) && (num.equals("0002") || num.equals("0004")) ||
           error.is(ErrType.BXDB) && num.equals("0006")) return Bln.FALSE;
      }
      throw ex;
    }
  }
}
