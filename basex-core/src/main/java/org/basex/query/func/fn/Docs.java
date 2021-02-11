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
import org.basex.query.value.seq.*;

/**
 * Document and collection functions.
 *
 * @author BaseX Team 2005-21, BSD License
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
  final Value collection(final QueryContext qc) throws QueryException {
    // return default collection or parse specified collection
    QueryInput qi = queryInput;
    if(qi == null) {
      final Item item = exprs.length == 0 ? Empty.VALUE : exprs[0].atomItem(qc, info);
      if(item != Empty.VALUE) {
        final byte[] uri = toToken(item);
        qi = queryInput(uri);
        if(qi == null) throw INVCOLL_X.get(info, uri);
      }
    }
    return qc.resources.collection(qi, info);
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
      final byte[] uri = toTokenOrNull(exprs[0], qc);
      if(uri == null) return Empty.VALUE;
      qi = queryInput(uri);
      if(qi == null) throw INVDOC_X.get(info, uri);
    }
    return qc.resources.doc(qi, info);
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    if(sc.withdb) {
      if(exprs.length == 0) {
        // lock default collection (only collection functions can have 0 arguments)
        visitor.lock(Locking.COLLECTION, false);
      } else {
        // check if input argument is a static string
        final Expr expr = exprs[0];
        if(expr instanceof Str) {
          // add local lock if argument may reference a database
          queryInput = queryInput(((Str) expr).string());
          if(queryInput != null) visitor.lock(queryInput.dbName, false);
        } else if(expr != Empty.VALUE) {
          // otherwise, database cannot be locked statically
          if(!visitor.lock(null, false)) return false;
        }
      }
    }
    return super.accept(visitor);
  }

  @Override
  public final boolean has(final Flag... flags) {
    // remote URLs: return non-deterministic flag to suppress pre-evaluation
    if(Flag.NDT.in(flags) && exprs.length > 0) {
      final Expr expr = exprs[0];
      if(expr instanceof Str) {
        queryInput = queryInput(((Str) expr).string());
        if(queryInput != null && queryInput.io instanceof IOUrl) return true;
      }
    }
    return super.has(flags);
  }

  /**
   * Converts the specified URI to a query input reference.
   * @param uri URI
   * @return query input, or {@code null} if the URI is invalid
   */
  private QueryInput queryInput(final byte[] uri) {
    return queryInput != null ? queryInput :
      Uri.uri(uri).isValid() ? new QueryInput(string(uri), sc) : null;
  }
}
