package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.QueryError.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class FnDocAvailable extends Docs {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    try {
      return Bln.get(doc(qc) != Empty.VALUE);
    } catch(final QueryException ex) {
      final QueryError error = ex.error();
      if(error != null && error.code.matches("^.*\\d+$")) {
        final int num = Strings.toInt(error.code.replaceAll("^.*(\\d+)$", "$1"));
        if(error.is(ErrType.FODC) && (num == 2 || num == 4 || num == 5) ||
           error.is(ErrType.DB) && num == 6) return Bln.FALSE;
      }
      throw ex;
    }
  }
}
