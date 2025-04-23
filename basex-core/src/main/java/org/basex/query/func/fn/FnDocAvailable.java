package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.users.*;
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
   * Performs the doc function.
   * @param qc query context
   * @return document or {@link Empty#VALUE}
   * @throws QueryException query exception
   */
  final Item doc(final QueryContext qc) throws QueryException {
    final Item options = arg(1).item(qc, info);
    final DocOptions docOpts;
    if(options.isEmpty()) {
      docOpts = null;
    } else {
      docOpts = toOptions(options, new DocOptions(), qc);
      if(docOpts.get(DocOptions.DTD) || docOpts.get(DocOptions.XINCLUDE)
          || docOpts.get(DocOptions.ALLOW_EXTERNAL_ENTITIES)) {
        checkPerm(qc, Perm.CREATE);
      }
      final boolean dtdVal = docOpts.get(DocOptions.DTD_VALIDATION);
      final String xsdVal = docOpts.get(DocOptions.XSD_VALIDATION);
      final boolean skip = MainOptions.SKIP.equals(xsdVal);
      final boolean strict = MainOptions.STRICT.equals(xsdVal);
      final boolean intparse = docOpts.get(DocOptions.INTPARSE);
      if(intparse) {
        if(dtdVal) throw NODTDVALIDATION.get(info);
        if(!skip) throw NOXSDVALIDATION_X.get(info, xsdVal);
      } else if(!skip) {
        if(!strict) throw INVALIDXSDOPT_X.get(info, xsdVal);
        if(dtdVal) throw NOXSDANDDTD_X.get(info, xsdVal);
      }
    }

    QueryInput qi = queryInput;
    if(qi == null) {
      final Item source = arg(0).atomItem(qc, info);
      if(source.isEmpty()) return Empty.VALUE;
      qi = queryInput(toToken(source));
      if(qi == null) throw INVDOC_X.get(info, source);
    }
    return qc.resources.doc(qi, docOpts, qc.user, info);
  }
}
