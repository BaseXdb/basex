package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.options.*;

/**
 * Document and collection functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class Docs extends DynamicFn {
  /** Prefix of the {@link CommonOptions#XSD_VALIDATION} value that selects a schema type. */
  private static final String TYPE = "type ";

  /** Query input (can be {@code null}). */
  QueryInput queryInput;

  /**
   * Converts the specified URI to a query input reference.
   * @param uri URI
   * @return query input, or {@code null} if the URI is invalid
   */
  QueryInput queryInput(final byte[] uri) {
    if(queryInput != null) return queryInput;
    // database paths are not checked for URI validity
    final QueryInput qi = new QueryInput(string(uri), sc());
    return qi.dbName != null || Uri.get(uri).isValid() ? qi : null;
  }

  /**
   * Returns the first argument as a constant URI string.
   * @return URI, or {@code null} if the argument is not a constant string
   */
  final byte[] staticUri() {
    final Expr source = arg(0);
    return source instanceof final Str str ? str.string() :
      source instanceof final Uri uri ? uri.string() :
      source instanceof final Atm atm ? atm.string(null) : null;
  }

  /**
   * Indicates if repeated calls of the function will return the same nodes.
   * @param qc query context
   * @return result of check
   * @throws QueryException query exception
   */
  boolean stable(final QueryContext qc) throws QueryException {
    return arg(1) instanceof Value && toOptions(arg(1), new DocOptions(), qc).
        get(DocOptions.STABLE);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // pre-evaluate during dynamic compilation
    if(cc.dynamicContext() && arg(0) instanceof final Value value && stable(cc.qc)) {
      // target is empty
      final Item item = value.atomItem(cc.qc, info);
      if(item.isEmpty()) return value(cc.qc);
      // target is not a remote URL
      queryInput = queryInput(toToken(item));
      if(queryInput == null || !(queryInput.io instanceof IOUrl)) return value(cc.qc);
    }
    return this;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitor.lock(() -> {
      final ArrayList<String> list = new ArrayList<>(1);
      if(sc().withdb) {
        // lock default collection (only collection functions can have 0 arguments)
        if(defined(0)) {
          // check if input argument is a static string
          final byte[] uri = staticUri();
          if(uri != null) {
            // add local lock if argument may reference a database
            queryInput = queryInput(uri);
            if(queryInput != null && queryInput.dbName != null) list.add(queryInput.dbName);
            // lock opened database if it will be favored
            if(sc().defaultdb) list.add(Locking.COLLECTION);
          } else {
            // empty sequence: default collection; otherwise, enforce global lock
            list.add(arg(0).seqType().zero() ? Locking.COLLECTION : null);
          }
        } else {
          list.add(Locking.COLLECTION);
        }
      }
      return list;
    }, false) && super.accept(visitor);
  }

  /**
   * Checks the validity of the chosen parsing options.
   * Handles both common and main options.
   * @param options options
   * @param fragment parse fragment
   * @throws QueryException query exception
   */
  void check(final Options options, final boolean fragment) throws QueryException {
    final Predicate<String> bool = name -> options.get(name) == Boolean.TRUE;
    final boolean intparse = fragment || bool.test(CommonOptions.INTPARSE) ||
        bool.test(MainOptions.INTPARSE.name());
    final boolean dtdVal = bool.test(CommonOptions.DTD_VALIDATION) ||
        bool.test(MainOptions.DTDVALIDATION.name());
    String xsdVal = fragment ? CommonOptions.SKIP : options.get(MainOptions.XSDVALIDATION);
    if(xsdVal == null) xsdVal = (String) options.get(CommonOptions.XSD_VALIDATION);
    final boolean skip = CommonOptions.SKIP.equals(xsdVal);
    final boolean strict = CommonOptions.STRICT.equals(xsdVal);

    if(intparse && dtdVal) throw NODTDVALIDATION.get(info);
    if(!skip) {
      // reject unknown values before reporting the missing capability
      if(!strict && !CommonOptions.LAX.equals(xsdVal) && !xsdVal.startsWith(TYPE))
        throw INVALIDXSDOPT_X.get(info, xsdVal);
      if(!strict) throw NOSCHEMAAWARENESS_X.get(info, '\'' + xsdVal + "' validation");
      if(intparse) throw NOXSDVALIDATION_X.get(info, xsdVal);
      if(dtdVal) throw NOXSDANDDTD_X.get(info, xsdVal);
    }
  }
}
