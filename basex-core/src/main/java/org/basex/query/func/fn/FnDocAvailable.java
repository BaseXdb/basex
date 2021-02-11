package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnDocAvailable extends Docs {
  /** Possible failures. */
  private static final EnumSet<QueryError> ERRORS = EnumSet.of(
      BASEX_DBPATH1_X, BASEX_DBPATH2_X, IOERR_X, WHICHRES_X, RESDIR_X, INVDOC_X);

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    try {
      return Bln.get(doc(qc) != Empty.VALUE);
    } catch(final QueryException ex) {
      if(ERRORS.contains(ex.error())) return Bln.FALSE;
      throw ex;
    }
  }
}
