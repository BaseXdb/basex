package org.basex.query;

import static org.basex.core.Text.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.core.Context;
import org.basex.core.locks.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;

/**
 * This class is an entry point for evaluating XQuery strings.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class QueryProcessor extends Proc implements Closeable {
  /** Pattern for detecting library modules. */
  private static final Pattern LIBMOD_PATTERN = Pattern.compile(
  "^(xquery( version ['\"].*?['\"])?( encoding ['\"].*?['\"])? ?; ?)?module namespace.*");

  /** Static context. */
  public final StaticContext sc;
  /** Expression context. */
  public final QueryContext qc;
  /** Query. */
  private final String query;
  /** Parsed flag. */
  private boolean parsed;

  /**
   * Default constructor.
   * @param query query string
   * @param ctx database context
   */
  public QueryProcessor(final String query, final Context ctx) {
    this.query = query;
    qc = proc(new QueryContext(ctx));
    sc = new StaticContext(qc);
  }

  /**
   * Parses the query.
   * @throws QueryException query exception
   */
  public void parse() throws QueryException {
    if(parsed) return;
    parsed = true;
    qc.parseMain(query, null, sc);
    updating = qc.updating;
  }

  /**
   * Compiles the query.
   * @throws QueryException query exception
   */
  public void compile() throws QueryException {
    parse();
    qc.compile();
  }

  /**
   * Returns a memory-efficient result iterator. In most cases, the query will only be fully
   * evaluated if all items of this iterator are requested.
   * @return result iterator
   * @throws QueryException query exception
   */
  public Iter iter() throws QueryException {
    parse();
    return qc.iter();
  }

  /**
   * Evaluates the query and returns the resulting value.
   * @return result value
   * @throws QueryException query exception
   */
  public Value value() throws QueryException {
    parse();
    return qc.iter().value();
  }

  /**
   * This function is called by the GUI; use {@link #iter()} or {@link #value()} instead.
   * Caches and returns the result of the specified query. If all nodes are of the same database
   * instance, the returned value will be of type {@link DBNodes}.
   * @param max maximum number of results to cache (negative: return all values)
   * @return result of query
   * @throws QueryException query exception
   */
  public Value cache(final int max) throws QueryException {
    parse();
    return qc.cache(max);
  }

  /**
   * Binds a value with the specified type to a global variable.
   * If the value is an {@link Expr} instance, it is directly assigned.
   * Otherwise, it is first cast to the appropriate XQuery type. If {@code "json"}
   * is specified as type, the value is interpreted according to the rules
   * specified in {@link JsonMapConverter}.
   * @param name name of variable
   * @param value value to be bound
   * @param type type (may be {@code null})
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor bind(final String name, final Object value, final String type)
      throws QueryException {
    qc.bind(name, value, type, sc);
    return this;
  }

  /**
   * Binds a value to a global variable.
   * @param name name of variable
   * @param value value to be bound
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor bind(final String name, final Object value) throws QueryException {
    return bind(name, value, null);
  }

  /**
   * Binds an XQuery value to a global variable.
   * @param name name of variable
   * @param value value to be bound
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor bind(final String name, final Value value) throws QueryException {
    qc.bind(name, value, sc);
    return this;
  }

  /**
   * Binds the context value.
   * @param value value to be bound
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor context(final Object value) throws QueryException {
    return context(value, null);
  }

  /**
   * Binds the context value.
   * @param value XQuery value to be bound
   * @return self reference
   */
  public QueryProcessor context(final Value value) {
    qc.context(value, sc);
    return this;
  }

  /**
   * Binds the HTTP context to the query processor.
   * @param value HTTP context
   * @return self reference
   */
  public QueryProcessor http(final Object value) {
    qc.http(value);
    return this;
  }

  /**
   * Binds the context value with a specified type,
   * using the same rules as for {@link #bind binding variables}.
   * @param value value to be bound
   * @param type type (may be {@code null})
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor context(final Object value, final String type) throws QueryException {
    qc.context(value, type, sc);
    return this;
  }

  /**
   * Declares a namespace.
   * A namespace is undeclared if the {@code uri} is an empty string.
   * The default element namespaces is set if the {@code prefix} is empty.
   * @param prefix namespace prefix
   * @param uri namespace uri
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor namespace(final String prefix, final String uri) throws QueryException {
    sc.namespace(prefix, uri);
    return this;
  }

  /**
   * Assigns a URI resolver.
   * @param resolver resolver
   * @return self reference
   */
  public QueryProcessor uriResolver(final UriResolver resolver) {
    sc.resolver = resolver;
    return this;
  }

  /**
   * Returns a serializer for the given output stream.
   * Optional output declarations within the query will be included in the
   * serializer instance.
   * @param os output stream
   * @return serializer instance
   * @throws IOException query exception
   * @throws QueryException query exception
   */
  public Serializer getSerializer(final OutputStream os) throws IOException, QueryException {
    compile();
    try {
      return Serializer.get(os, qc.serParams()).sc(sc);
    } catch(final QueryIOException ex) {
      throw ex.getCause();
    }
  }

  /**
   * Adds a module reference. Only called from the test APIs.
   * @param uri module uri
   * @param file file name
   */
  public void module(final String uri, final String file) {
    qc.modDeclared.put(uri, file);
  }

  /**
   * Returns the query string.
   * @return query
   */
  public String query() {
    return query;
  }

  @Override
  public void close() {
    qc.close();
  }

  @Override
  public void databases(final LockResult lr) {
    qc.databases(lr);
  }

  /**
   * Returns the number of performed updates after query execution, or {@code 0}.
   * @return number of updates
   */
  public int updates() {
    return updating ? qc.resources.updates().size() : 0;
  }

  /**
   * Returns query information.
   * @return query information
   */
  public String info() {
    return qc.info();
  }

  /**
   * Checks if the specified XQuery string is a library module.
   * @param qu query string
   * @return result of check
   */
  public static boolean isLibrary(final String qu) {
    return LIBMOD_PATTERN.matcher(removeComments(qu, 80)).matches();
  }

  /**
   * Removes comments from the specified string and returns the first characters
   * of a query.
   * @param qu query string
   * @param max maximum length of string to return
   * @return result
   */
  public static String removeComments(final String qu, final int max) {
    final StringBuilder sb = new StringBuilder();
    int m = 0;
    boolean s = false;
    final int cl = qu.length();
    for(int c = 0; c < cl && sb.length() < max; ++c) {
      final char ch = qu.charAt(c);
      if(ch == 0x0d) continue;
      if(ch == '(' && c + 1 < cl && qu.charAt(c + 1) == ':') {
        if(m == 0 && !s) {
          sb.append(' ');
          s = true;
        }
        ++m;
        ++c;
      } else if(m != 0 && ch == ':' && c + 1 < cl && qu.charAt(c + 1) == ')') {
        --m;
        ++c;
      } else if(m == 0) {
        if(ch > ' ') sb.append(ch);
        else if(!s) sb.append(' ');
        s = ch <= ' ';
      }
    }
    if(sb.length() >= max) sb.append("...");
    return sb.toString().trim();
  }

  /**
   * Returns a map with variable bindings.
   * @param opts main options
   * @return bindings
   */
  public static HashMap<String, String> bindings(final MainOptions opts) {
    final HashMap<String, String> bindings = new HashMap<>();
    final String bind = opts.get(MainOptions.BINDINGS).trim();
    final StringBuilder key = new StringBuilder();
    final StringBuilder val = new StringBuilder();
    boolean first = true;
    final int sl = bind.length();
    for(int s = 0; s < sl; s++) {
      final char ch = bind.charAt(s);
      if(first) {
        if(ch == '=') {
          first = false;
        } else {
          key.append(ch);
        }
      } else {
        if(ch == ',') {
          if(s + 1 == sl || bind.charAt(s + 1) != ',') {
            bindings.put(key.toString().trim(), val.toString());
            key.setLength(0);
            val.setLength(0);
            first = true;
            continue;
          }
          // literal commas are escaped by a second comma
          s++;
        }
        val.append(ch);
      }
    }
    if(!first) bindings.put(key.toString().trim(), val.toString());
    return bindings;
  }

  /**
   * Returns a tree representation of the query plan.
   * @return root node
   */
  public FDoc plan() {
    return new FDoc().add(qc.plan());
  }

  @Override
  public String tit() {
    return PLEASE_WAIT_D;
  }

  @Override
  public String det() {
    return PLEASE_WAIT_D;
  }

  @Override
  public String toString() {
    return query;
  }
}
