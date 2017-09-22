package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.core.locks.*;
import org.basex.io.*;
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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class Docs extends StandardFunc {
  /** Query input. */
  private QueryInput queryInput;

  /**
   * Returns a collection.
   * @param qc query context
   * @return collection
   * @throws QueryException query exception
   */
  Value collection(final QueryContext qc) throws QueryException {
    // return default collection or parse specified collection
    QueryInput qi = queryInput;
    if(qi == null) {
      final Item it = exprs.length == 0 ? null : exprs[0].atomItem(qc, info);
      if(it != null) {
        final byte[] uri = toToken(it);
        qi = queryInput(uri);
        if(qi == null) throw INVCOLL_X.get(info, uri);
      }
    }
    return qc.resources.collection(qi, info);
  }

  /**
   * Performs the doc function.
   * @param qc query context
   * @return document
   * @throws QueryException query exception
   */
  ANode doc(final QueryContext qc) throws QueryException {
    QueryInput qi = queryInput;
    if(qi == null) {
      final Item it = exprs[0].atomItem(qc, info);
      if(it == null) return null;
      final byte[] uri = toToken(it);
      qi = queryInput(uri);
      if(qi == null) throw INVDOC_X.get(info, uri);
    }
    return qc.resources.doc(qi, info);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    if(exprs.length == 0) {
      // lock default collection (only collection functions can have 0 arguments)
      visitor.lock(Locking.COLLECTION);
    } else {
      // check if input argument is a static string
      final Expr expr = exprs[0];
      if(expr instanceof Str) {
        // add local lock if argument may reference a database
        queryInput = queryInput(((Str) expr).string());
        if(queryInput != null) visitor.lock(queryInput.dbName);
      } else if(!expr.isEmpty()) {
        // otherwise, database cannot be locked statically
        if(!visitor.lock(null)) return false;
      }
    }
    return super.accept(visitor);
  }

  @Override
  public boolean has(final Flag flag) {
    if(flag == Flag.NDT && exprs.length > 0) {
      final Expr expr = exprs[0];
      if(expr instanceof Str) {
        queryInput = queryInput(((Str) expr).string());
        // do not pre-evaluate URL input
        if(queryInput != null && queryInput.io instanceof IOUrl) return true;
      }
    }
    return super.has(flag);
  }

  /**
   * Converts the specified URI to a query input reference.
   * @param uri URI
   * @return query input, or {@code null} if the URI is invalid.
   */
  private QueryInput queryInput(final byte[] uri) {
    return queryInput != null ? queryInput :
      Uri.uri(uri).isValid() ? new QueryInput(string(uri), sc) : null;
  }
}
