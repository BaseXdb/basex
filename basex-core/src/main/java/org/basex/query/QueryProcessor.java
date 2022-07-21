package org.basex.query;

import java.io.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.jobs.*;
import org.basex.io.serial.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;

/**
 * This class is an entry point for evaluating XQuery strings.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class QueryProcessor extends Job implements Closeable {
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
    this(query, null, ctx, null);
  }

  /**
   * Default constructor.
   * @param query query string
   * @param uri base uri (can be {@code null})
   * @param ctx database context
   * @param info query info (can be {@code null})
   */
  public QueryProcessor(final String query, final String uri, final Context ctx,
      final QueryInfo info) {
    this.query = query;
    qc = pushJob(new QueryContext(ctx, null, null, info));
    sc = new StaticContext(qc);
    sc.baseURI(uri != null && uri.isEmpty() ? "./" : uri);
  }

  /**
   * Parses the query.
   * @throws QueryException query exception
   */
  public void parse() throws QueryException {
    if(parsed) return;
    try {
      qc.parseMain(query, null, sc);
    } finally {
      parsed = true;
      updating = qc.updating;
    }
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
   * Optimizes the query.
   * @throws QueryException query exception
   */
  public void optimize() throws QueryException {
    compile();
    qc.optimize();
  }

  /**
   * Returns a memory-efficient result iterator. The query will only be fully evaluated if all items
   * of this iterator are requested.
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
    return qc.value();
  }

  /**
   * This function is called by the GUI.
   * Caches the result of the specified query. If all nodes are of the same database
   * instance, the cached value will be of type {@link DBNodes}.
   * @param cmd query command
   * @param max maximum number of items to cache (negative: return full result)
   * @throws QueryException query exception
   */
  public void cache(final AQuery cmd, final int max) throws QueryException {
    parse();
    qc.cache(cmd, max);
  }

  /**
   * Binds a value to a global variable or the context value.
   * The arguments will be ignored if a value has already been assigned.
   * @param name name of variable; context value if empty string or {@code null}
   * @param value value to be bound (object or XQuery {@link Value})
   * @param type type (may be {@code null})
   * @return self reference
   * @see QueryContext#bind(String, Object, String, StaticContext)
   * @throws QueryException query exception
   */
  public QueryProcessor variable(final String name, final Object value, final String type)
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
  public QueryProcessor variable(final String name, final Object value) throws QueryException {
    return variable(name, value, null);
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
   * Binds the context value with a specified type,
   * using the same rules as for {@link #variable binding variables}.
   * @param value value to be bound
   * @param type type (may be {@code null})
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor context(final Object value, final String type) throws QueryException {
    qc.bind(null, value, type, sc);
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
   * Optional output declarations within the query will be included in the serializer instance.
   * @param os output stream
   * @return serializer instance
   * @throws IOException query exception
   * @throws QueryException query exception
   */
  public Serializer serializer(final OutputStream os) throws IOException, QueryException {
    compile();
    try {
      return Serializer.get(os, qc.parameters()).sc(sc);
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

  @Override
  public void close() {
    qc.close();
  }

  @Override
  public void addLocks() {
    qc.addLocks();
  }

  /**
   * Returns the number of performed updates after query execution, or {@code 0}.
   * @return number of updates
   */
  public int updates() {
    return updating ? qc.updates().size() : 0;
  }

  /**
   * Returns a query plan as XML.
   * @return query plan
   */
  public FElem toXml() {
    return qc.toXml(qc.context.options.get(MainOptions.FULLPLAN));
  }

  @Override
  public String toString() {
    return query;
  }
}
