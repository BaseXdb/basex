package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.core.Context;
import org.basex.data.*;
import org.basex.io.serial.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.json.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;

/**
 * This class is an entry point for evaluating XQuery implementations.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class QueryProcessor extends Progress {
  /** Expression context. */
  public final QueryContext ctx;
  /** Query. */
  private final String query;
  /** Parsed flag. */
  private boolean parsed;
  /** Compilation flag. */
  private boolean compiled;

  /**
   * Default constructor.
   * @param qu query to process
   * @param cx database context
   */
  public QueryProcessor(final String qu, final Context cx) {
    query = qu;
    ctx = new QueryContext(cx);
    progress(ctx);
  }

  /**
   * Parses the query.
   * @throws QueryException query exception
   */
  public void parse() throws QueryException {
    if(parsed) return;
    parsed = true;
    ctx.parse(query);
    updating = ctx.updating;
  }

  /**
   * Compiles the query.
   * @throws QueryException query exception
   */
  public void compile() throws QueryException {
    if(compiled) return;
    compiled = true;
    parse();
    ctx.compile();
  }

  /**
   * Returns a result iterator.
   * @return result iterator
   * @throws QueryException query exception
   */
  public Iter iter() throws QueryException {
    compile();
    return ctx.iter();
  }

  /**
   * Returns a result value.
   * @return result value
   * @throws QueryException query exception
   */
  public Value value() throws QueryException {
    compile();
    return ctx.value();
  }

  /**
   * Evaluates the specified query and returns the result.
   * @return result of query
   * @throws QueryException query exception
   */
  public Result execute() throws QueryException {
    compile();
    return ctx.execute();
  }

  /**
   * Binds a value with the specified data type to a global variable.
   * If the value is an {@link Expr} instance, it is directly assigned.
   * Otherwise, it is first cast to the appropriate XQuery type. If {@code "json"}
   * is specified as data type, the value is interpreted according to the rules
   * specified in {@link JsonMapConverter}.
   * @param name name of variable
   * @param value value to be bound
   * @param type data type (may be {@code null})
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor bind(final String name, final Object value, final String type)
      throws QueryException {
    ctx.bind(name, value, type);
    return this;
  }

  /**
   * Binds a value to a global variable.
   * @param name name of variable
   * @param value value to be bound
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor bind(final String name, final Object value)
      throws QueryException {
    return bind(name, value, null);
  }

  /**
   * Binds a value to the context item.
   * @param value value to be bound
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor context(final Object value) throws QueryException {
    return context(value, null);
  }

  /**
   * Binds a value with the specified data type to the context item,
   * using the same rules as for {@link #bind binding variables}.
   * @param value value to be bound
   * @param type data type (may be {@code null})
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryProcessor context(final Object value, final String type)
      throws QueryException {
    ctx.context(value, type);
    return this;
  }

  /**
   * Binds an initial nodeset to the context item.
   * @param nodes node set
   * @return self reference
   */
  public QueryProcessor context(final Nodes nodes) {
    ctx.nodes = nodes;
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
  public QueryProcessor namespace(final String prefix, final String uri)
      throws QueryException {
    ctx.sc.namespace(prefix, uri);
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
  public Serializer getSerializer(final OutputStream os)
      throws IOException, QueryException {

    compile();
    try {
      return Serializer.get(os, ctx.serParams(true));
    } catch(final SerializerException ex) {
      throw ex.getCause();
    }
  }

  /**
   * Evaluates the specified query and returns the result nodes.
   * @return result nodes
   * @throws QueryException query exception
   */
  public Nodes queryNodes() throws QueryException {
    final Result res = execute();
    if(!(res instanceof Nodes)) {
      // convert empty result to node set
      if(res.size() == 0) return new Nodes(ctx.nodes.data);
      // otherwise, throw error
      BXDB_DBRETURN.thrw(null);
    }
    return (Nodes) res;
  }

  /**
   * Adds a module reference. Only called from the test APIs.
   * @param uri module uri
   * @param file file name
   */
  public void module(final String uri, final String file) {
    ctx.modDeclared.add(token(uri), token(file));
  }

  /**
   * Returns the query string.
   * @return query
   */
  public String query() {
    return query;
  }

  /**
   * Closes the processor.
   */
  public void close() {
    ctx.close();
  }

  /**
   * Returns the number of performed updates after query execution, or {@code 0}.
   * @return number of updates
   */
  public int updates() {
    return updating ? ctx.updates.size() : 0;
  }

  /**
   * Returns query background information.
   * @return background information
   */
  public String info() {
    return ctx.info();
  }

  /**
   * Removes comments from the specified string.
   * @param qu query string
   * @param max maximum string length
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
   * Returns a tree representation of the query plan.
   * @return root node
   */
  public FDoc plan() {
    final FDoc doc = new FDoc(EMPTY);
    ctx.plan(doc);
    return doc;
  }

  @Override
  public String tit() {
    return PLEASE_WAIT_D;
  }

  @Override
  public String det() {
    return PLEASE_WAIT_D;
  }
}
