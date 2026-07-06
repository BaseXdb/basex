package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnCollection extends Docs {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return collection(qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = super.opt(cc);
    if(expr == this) {
      // sharpen the type
      final byte[] uri = staticUri();
      final QueryInput qi = uri != null ? queryInput(uri) : null;
      if(qi != null && qi.dbName != null) exprType.assign(Types.DOCUMENT_ZM);
    }
    return expr;
  }

  @Override
  public final boolean ddo() {
    return true;
  }

  /**
   * Returns a collection.
   * @param qc query context
   * @return collection
   * @throws QueryException query exception
   */
  final Value collection(final QueryContext qc) throws QueryException {
    // return default collection or parse specified collection
    QueryInput qi = queryInput;
    if(qi == null) {
      final Item uri = arg(0).atomItem(qc, info);
      if(!uri.isEmpty()) {
        qi = queryInput(toToken(uri));
        if(qi == null) throw INVCOLL_X.get(info, uri);
      }
    }
    return qc.resources.collection(qi, qc.user, info);
  }
}
