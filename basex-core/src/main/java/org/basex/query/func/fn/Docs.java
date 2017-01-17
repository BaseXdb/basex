package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Document and collection functions.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public abstract class Docs extends StandardFunc {
  /**
   * Returns a collection.
   * @param qc query context
   * @return collection
   * @throws QueryException query exception
   */
  Value collection(final QueryContext qc) throws QueryException {
    // return default collection or parse specified collection
    QueryInput qi = null;
    final Item it = exprs.length == 0 ? null : exprs[0].atomItem(qc, info);
    if(it != null) {
      final byte[] uri = toToken(it);
      if(!Uri.uri(uri).isValid()) throw INVCOLL_X.get(info, uri);
      qi = new QueryInput(string(uri), sc);
    }
    return qc.resources.collection(qi, info);
  }

  /**
   * Performs the doc function.
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  ANode doc(final QueryContext qc) throws QueryException {
    final Item it = exprs[0].item(qc, info);
    if(it == null) return null;
    final byte[] uri = toToken(it);
    if(!Uri.uri(uri).isValid()) throw INVDOC_X.get(info, uri);
    return qc.resources.doc(new QueryInput(string(uri), sc), info);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    if(exprs.length == 0) {
      // only applies to collections
      if(!visitor.lock(Locking.COLLECTION)) return false;
    } else {
      final Expr expr = exprs[0];
      if(expr instanceof Str) {
        final QueryInput qi = new QueryInput(string(((Str) expr).string()), sc);
        if(!visitor.lock(qi.dbName)) return false;
      } else if(!expr.isEmpty()) {
        if(!visitor.lock(null)) return false;
      }
    }
    return super.accept(visitor);
  }
}
