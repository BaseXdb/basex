package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.locks.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Document and collection functions.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public abstract class Docs extends DynamicFn {
  /** Query input. */
  QueryInput queryInput;

  /**
   * Converts the specified URI to a query input reference.
   * @param uri URI
   * @return query input, or {@code null} if the URI is invalid
   */
  QueryInput queryInput(final byte[] uri) {
    return queryInput != null ? queryInput :
      Uri.get(uri).isValid() ? new QueryInput(string(uri), sc) : null;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // pre-evaluate during dynamic compilation
    if(cc.dynamic && arg(0) instanceof Value) {
      // target is empty
      final Item item = arg(0).atomItem(cc.qc, info);
      if(item.isEmpty()) return value(cc.qc);
      // target is not a remote URL
      queryInput = queryInput(toToken(item));
      if(queryInput == null || !(queryInput.io instanceof IOUrl)) return value(cc.qc);
    }
    return this;
  }

  @Override
  public final boolean accept(final ASTVisitor visitor) {
    return visitor.lock(() -> {
      final ArrayList<String> list = new ArrayList<>(1);
      if(sc.withdb) {
        // lock default collection (only collection functions can have 0 arguments)
        if(defined(0)) {
          // check if input argument is a static string
          final Expr expr = arg(0);
          final byte[] uri = expr instanceof Str ? ((Str) expr).string() :
            expr instanceof Atm ? ((Atm) expr).string(null) : null;
          if(uri != null) {
            // add local lock if argument may reference a database
            queryInput = queryInput(uri);
            if(queryInput != null && queryInput.dbName != null) list.add(queryInput.dbName);
          } else {
            // empty sequence: default collection; otherwise, enforce global lock
            list.add(expr.seqType().zero() ? Locking.COLLECTION : null);
          }
        } else {
          list.add(Locking.COLLECTION);
        }
      }
      return list;
    }) && super.accept(visitor);
  }
}
