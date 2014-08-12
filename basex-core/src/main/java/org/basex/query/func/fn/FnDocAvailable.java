package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.Err.ErrType;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnDocAvailable extends Docs {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    try {
      return Bln.get(doc(qc) != null);
    } catch(final QueryException ex) {
      final Err err = ex.err();
      if(err != null) {
        final String num = err.code.length() == 8 ? err.code.substring(4) : "";
        if(err.is(ErrType.FODC) && (num.equals("0002") || num.equals("0004")) ||
           err.is(ErrType.BXDB) && num.equals("0006")) return Bln.FALSE;
      }
      throw ex;
    }
  }
}
