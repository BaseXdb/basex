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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnDocAvailable extends Docs {
  /** Possible failures. */
  private static final EnumSet<QueryError> ERRORS = EnumSet.of(BASEX_DBPATH1_X, BASEX_DBPATH2_X,
      IOERR_X, WHICHRES_X, RESDIR_X, INVDOC_X, DTDVALIDATIONERR_X, XSDVALIDATIONERR_X);

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
   * Performs the {@code fn:doc} function.
   * @param qc query context
   * @return document or {@link Empty#VALUE}
   * @throws QueryException query exception
   */
  final Item doc(final QueryContext qc) throws QueryException {
    final DocOptions options = toOptions(arg(1), new DocOptions(), qc);
    check(options, false, qc);

    QueryInput qi = queryInput;
    if(qi == null) {
      final Item source = arg(0).atomItem(qc, info);
      if(source.isEmpty()) return Empty.VALUE;
      qi = queryInput(toToken(source));
      if(qi == null) throw INVDOC_X.get(info, source);
    }
    return qc.resources.doc(qi, options, qc.user, info);
  }
}
