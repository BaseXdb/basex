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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FnDocAvailable extends Docs {
  /** Possible failures. */
  private static final EnumSet<QueryError> ERRORS = EnumSet.of(
      BASEX_DBPATH1_X, BASEX_DBPATH2_X, IOERR_X, WHICHRES_X, RESDIR_X, INVDOC_X);

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    try {
      return Bln.get(!doc(qc).isEmpty());
    } catch(final QueryException ex) {
      if(ERRORS.contains(ex.error())) return Bln.FALSE;
      throw ex;
    }
  }

  /**
   * Performs the doc function.
   * @param qc query context
   * @return document or {@link Empty#VALUE}
   * @throws QueryException query exception
   */
  final Item doc(final QueryContext qc) throws QueryException {
    QueryInput qi = queryInput;
    if(qi == null) {
      final Item href = arg(0).atomItem(qc, info);
      if(href.isEmpty()) return Empty.VALUE;
      qi = queryInput(toToken(href));
      if(qi == null) throw INVDOC_X.get(info, href);
    }
    return qc.resources.doc(qi, info);
  }
}
