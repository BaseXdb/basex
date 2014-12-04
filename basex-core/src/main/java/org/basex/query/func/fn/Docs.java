package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.basex.util.Token.*;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Document and collection functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class Docs extends StandardFunc {
  /**
   * Returns a collection.
   * @param qc query context
   * @return collection
   * @throws QueryException query exception
   */
  Value collection(final QueryContext qc) throws QueryException {
    // return default collection
    final Item it = exprs.length == 0 ? null : exprs[0].atomItem(qc, info);
    if(it == null) return qc.resources.collection(info);

    // check if reference is valid
    final byte[] in = toToken(it);
    if(!Uri.uri(in).isValid()) throw INVCOLL_X.get(info, in);
    return qc.resources.collection(new QueryInput(string(in)), sc.baseIO(), info);
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
    final byte[] in = toToken(it);
    if(!Uri.uri(in).isValid()) throw INVDOC_X.get(info, in);
    return qc.resources.doc(new QueryInput(string(in)), sc.baseIO(), info);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    if(exprs.length == 0) {
      if(oneOf(func, COLLECTION, URI_COLLECTION) && !visitor.lock(DBLocking.COLL)) return false;
    } else if(!(exprs[0] instanceof Str)) {
      if(!visitor.lock(null)) return false;
    } else {
      final QueryInput qi = new QueryInput(string(((Str) exprs[0]).string()));
      if(qi.db == null && !visitor.lock(null)) return false;
      if(!visitor.lock(qi.db)) return false;
    }
    return super.accept(visitor);
  }
}
